package app.momo.voice

import ai.pleos.playground.stt.SpeechToText
import ai.pleos.playground.stt.listener.ResultListener
import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.listener.EventListener
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import ai.pleos.playground.stt.constant.ErrorType as SttError
import ai.pleos.playground.stt.constant.Mode as SttMode
import ai.pleos.playground.stt.listener.OnInitializeListener as SttInit
import ai.pleos.playground.tts.constant.ErrorType as TtsError
import ai.pleos.playground.tts.constant.Mode as TtsMode
import ai.pleos.playground.tts.listener.OnInitializeListener as TtsInit

/**
 * Pleos STT·TTS 3.3.1(ON_DEVICE)로 만든 Voice.
 * - speak는 한 문장씩 순서대로 말하고, TTS가 onDone/onStop/onError를 줄 때까지 기다린다.
 * - TTS 초기화가 실패하면 TextVoice처럼 말하는 시간만 흉내 내고, STT가 실패하면 inject 입력만 받는다.
 * - 오디오 포커스: 내비 안내(duck)는 그대로 두고, Gleo 음성 명령·통화처럼 포커스를 잃으면 멈췄다가 되찾으면 그 문장부터 다시 말한다.
 */
class PleosVoice(context: Context) : Voice {
    private val app = context.applicationContext
    private val stt = SpeechToText(app, SttMode.ON_DEVICE)
    private val tts = TextToSpeech(app, TtsMode.ON_DEVICE)
    private val audio = app.getSystemService(AudioManager::class.java)
    private val scope = MainScope()

    private val _status = MutableStateFlow(VoiceStatus.IDLE)
    override val status = _status.asStateFlow()
    private val _heard = MutableSharedFlow<String>(extraBufferCapacity = 8)
    override val heard = _heard
    override val partial = MutableStateFlow("")

    @Volatile private var sttReady = false
    @Volatile private var ttsReady = false

    private val order = Mutex()
    private val waiting = AtomicInteger(0)
    /** stopSpeaking을 부를 때마다 올린다. 그 전에 줄을 선 문장은 말하지 않고 버린다. */
    @Volatile private var generation = 0
    @Volatile private var current: CompletableDeferred<String>? = null
    // 이전 발화를 멈출 때 생긴 onStop이 늦게 와서 새 문장을 끝난 것으로 착각하지 않도록, onStart를 받은 뒤의 완료만 인정한다
    @Volatile private var started = false

    private val paused = MutableStateFlow(false)
    @Volatile private var hasFocus = false
    private var abandonJob: Job? = null

    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        .setAudioAttributes(attributes)
        .setAcceptsDelayedFocusGain(true)
        .setOnAudioFocusChangeListener { change ->
            when (change) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    log("포커스 되찾음 → 이어서 말함")
                    hasFocus = true
                    paused.value = false
                }
                // 내비 안내처럼 잠깐 겹치는 소리: 시스템이 볼륨을 줄여 주므로 그대로 말한다
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> log("포커스 duck (계속 말함)")
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    log("포커스 잠깐 잃음 (Gleo·통화) → 일시정지")
                    pause()
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    log("포커스 잃음 → 일시정지, 다음 문장에서 다시 요청")
                    hasFocus = false
                    pause()
                }
            }
        }
        .build()

    override fun start() {
        try {
            stt.initialize(object : SttInit {
                override fun onSuccess() {
                    stt.addListener(sttListener)
                    stt.setLocale(Locale.KOREA)
                    sttReady = true
                    log("STT 초기화 성공")
                }
                override fun onFailure(errorType: SttError) = log("STT 초기화 실패: $errorType → inject 입력만 받음")
            })
        } catch (e: Exception) {
            Log.w(TAG, "STT 초기화 예외 → inject 입력만 받음", e)
        }
        try {
            tts.initialize(object : TtsInit {
                override fun onSuccess() {
                    tts.addEventListener(ttsListener)
                    tts.setLocale(Locale.KOREA)
                    runCatching { tts.setAudioAttributes(attributes) }.onFailure { Log.w(TAG, "TTS setAudioAttributes 실패", it) }
                    ttsReady = true
                    log("TTS 초기화 성공")
                }
                override fun onFailure(errorType: TtsError) = log("TTS 초기화 실패: $errorType → 소리 없이 시간만 흉내 냄")
            })
        } catch (e: Exception) {
            Log.w(TAG, "TTS 초기화 예외 → 소리 없이 시간만 흉내 냄", e)
        }
    }

    override fun release() {
        stopSpeaking()
        runCatching { stt.removeListener(sttListener) }
        runCatching { tts.removeEventListener(ttsListener) }
        runCatching { stt.release() }
        runCatching { tts.release() }
        abandonFocus()
    }

    // ---------- 말하기 ----------

    override suspend fun speak(text: String) {
        if (text.isBlank()) return
        val gen = generation
        waiting.incrementAndGet()
        abandonJob?.cancel()
        try {
            order.withLock {
                if (gen != generation) return
                _status.value = VoiceStatus.SPEAKING
                if (!ttsReady) {
                    delay(400L + text.length * 60L)
                    return
                }
                while (true) {
                    waitForFocus()
                    if (gen != generation) return
                    val result = say(text)
                    // 포커스를 잃어 끊긴 문장은 되찾은 뒤 처음부터 다시 말한다
                    if (result == "stop" && paused.value && gen == generation) continue
                    break
                }
            }
        } finally {
            if (waiting.decrementAndGet() == 0) {
                if (_status.value == VoiceStatus.SPEAKING) _status.value = VoiceStatus.IDLE
                // 문장 사이에 LLM을 기다리는 짧은 틈마다 포커스를 놓으면 음악 볼륨이 출렁이므로 조금 기다렸다 놓는다
                abandonJob = scope.launch {
                    delay(1500)
                    if (waiting.get() == 0) abandonFocus()
                }
            }
        }
    }

    private suspend fun say(text: String): String {
        val done = CompletableDeferred<String>()
        current = done
        started = false
        try {
            log("TTS 말하기: $text")
            tts.speak(text)
            // 콜백이 오지 않는 경우를 대비해, 글자 수에 비례한 시간이 지나면 끝난 것으로 본다
            return withTimeoutOrNull(5_000L + text.length * 250L) { done.await() } ?: "timeout".also { log("TTS 콜백 시간 초과") }
        } catch (e: CancellationException) {
            runCatching { tts.stop() }
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "TTS speak 실패", e)
            return "error"
        } finally {
            current = null
        }
    }

    override fun stopSpeaking() {
        generation++
        runCatching { if (ttsReady) tts.stop() }
        current?.complete("stop")
        if (_status.value == VoiceStatus.SPEAKING) _status.value = VoiceStatus.IDLE
    }

    private val ttsListener = object : EventListener {
        override fun onReady() = Unit
        override fun onStart() { started = true; log("TTS onStart") }
        override fun onUpdatedRms(rms: Double) = Unit
        override fun onDone() { log("TTS onDone"); if (started) current?.complete("done") }
        override fun onStop() { log("TTS onStop"); if (started) current?.complete("stop") }
        override fun onError(errorType: TtsError) { log("TTS onError $errorType"); current?.complete("error") }
    }

    // ---------- 오디오 포커스 ----------

    private suspend fun waitForFocus() {
        if (!hasFocus) requestFocus()
        while (paused.value) {
            // 영구 상실(다른 앱 미디어)은 GAIN 콜백이 오지 않으므로 3초마다 다시 요청한다
            withTimeoutOrNull(3000) { paused.first { !it } }
            if (paused.value && !hasFocus) requestFocus()
        }
    }

    private fun requestFocus() {
        when (val r = audio.requestAudioFocus(focusRequest)) {
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> { hasFocus = true; paused.value = false }
            AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> { hasFocus = true; paused.value = true; log("포커스 지연 → 되찾을 때까지 대기") }
            else -> { hasFocus = false; paused.value = true; log("포커스 요청 실패($r)") }
        }
    }

    private fun abandonFocus() {
        if (!hasFocus) return
        audio.abandonAudioFocusRequest(focusRequest)
        hasFocus = false
        paused.value = false
    }

    private fun pause() {
        paused.value = true
        runCatching { if (ttsReady) tts.stop() }
        current?.complete("stop")
    }

    // ---------- 듣기 ----------

    override fun listen() {
        partial.value = ""
        _status.value = VoiceStatus.LISTENING
        if (sttReady) runCatching { stt.request() }.onFailure { Log.w(TAG, "STT request 실패", it) }
    }

    override fun stopListening() {
        if (sttReady) runCatching { stt.stop() }
        if (_status.value == VoiceStatus.LISTENING) _status.value = VoiceStatus.IDLE
    }

    private val sttListener = object : ResultListener {
        override fun onReady() = log("STT onReady")
        override fun onStartedRecognition() = log("STT 인식 시작")
        override fun onUpdated(stt: String, completed: Boolean) {
            if (!completed) { partial.value = stt; return }
            log("STT 최종: $stt")
            partial.value = ""
            if (stt.isNotBlank()) _heard.tryEmit(stt)
        }
        override fun onUpdatedEpdData(on: Long, off: Long) = Unit
        override fun onUpdatedRms(rms: Double) = Unit
        override fun onEndedRecognition() {
            log("STT 인식 끝")
            if (_status.value == VoiceStatus.LISTENING) _status.value = VoiceStatus.IDLE
        }
        override fun onError(errorType: SttError) {
            log("STT 오류 $errorType")
            if (_status.value == VoiceStatus.LISTENING) _status.value = VoiceStatus.IDLE
        }
    }

    override fun setStatus(status: VoiceStatus) {
        _status.value = status
    }

    override fun inject(text: String) {
        log("주입된 발화: $text")
        partial.value = ""
        _heard.tryEmit(text)
    }

    private fun log(msg: String) { Log.i(TAG, msg) }

    private companion object {
        const val TAG = "MomoVoice"
    }
}
