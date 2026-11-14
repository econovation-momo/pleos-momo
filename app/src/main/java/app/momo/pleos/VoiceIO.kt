package app.momo.pleos

import ai.pleos.playground.stt.SpeechToText
import ai.pleos.playground.stt.constant.ErrorType as SttError
import ai.pleos.playground.stt.constant.Mode as SttMode
import ai.pleos.playground.stt.listener.ResultListener
import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.constant.ErrorType as TtsError
import ai.pleos.playground.tts.constant.Mode as TtsMode
import ai.pleos.playground.tts.listener.EventListener
import android.content.Context
import java.util.Locale
import ai.pleos.playground.stt.listener.OnInitializeListener as SttInit
import ai.pleos.playground.tts.listener.OnInitializeListener as TtsInit

/**
 * STT·TTS 3.3.1. 문서상 "Coming Soon"이라 동작 여부를 이 화면에서 확인한다.
 * ON_DEVICE 모드는 서버 등록(registerApp)이 필요 없다.
 */
class VoiceIO(context: Context) {
    private val stt = SpeechToText(context.applicationContext, SttMode.ON_DEVICE)
    private val tts = TextToSpeech(context.applicationContext, TtsMode.ON_DEVICE)

    private val sttListener = object : ResultListener {
        override fun onReady() = SignalHub.put("Voice/STT 상태", "준비")
        override fun onStartedRecognition() = SignalHub.put("Voice/STT 상태", "듣는 중")
        override fun onUpdated(stt: String, completed: Boolean) =
            SignalHub.put("Voice/STT 결과", if (completed) "[최종] $stt" else stt)
        override fun onUpdatedEpdData(on: Long, off: Long) = Unit
        override fun onUpdatedRms(rms: Double) {
            // 로그가 넘치지 않도록 값만 갱신한다 (dB 아님)
            lastRms = rms
            onRms?.invoke(rms)
        }
        override fun onEndedRecognition() = SignalHub.put("Voice/STT 상태", "종료")
        override fun onError(errorType: SttError) = SignalHub.put("Voice/STT 오류", errorType)
    }

    private val ttsListener = object : EventListener {
        override fun onReady() = SignalHub.put("Voice/TTS 상태", "준비")
        override fun onStart() = SignalHub.put("Voice/TTS 상태", "말하는 중")
        override fun onUpdatedRms(rms: Double) = Unit
        override fun onDone() = SignalHub.put("Voice/TTS 상태", "완료")
        override fun onStop() = SignalHub.put("Voice/TTS 상태", "중지")
        override fun onError(errorType: TtsError) = SignalHub.put("Voice/TTS 오류", errorType)
    }

    var lastRms = 0.0
        private set
    var onRms: ((Double) -> Unit)? = null

    fun start() {
        try {
            stt.initialize(object : SttInit {
                override fun onSuccess() {
                    SignalHub.put("Voice/STT SDK", "초기화 성공")
                    stt.addListener(sttListener)
                    stt.setLocale(Locale.KOREA)
                }
                override fun onFailure(errorType: SttError) = SignalHub.put("Voice/STT SDK", "초기화 실패: $errorType")
            })
        } catch (e: Exception) {
            SignalHub.fail("Voice/STT SDK", e)
        }
        try {
            tts.initialize(object : TtsInit {
                override fun onSuccess() {
                    SignalHub.put("Voice/TTS SDK", "초기화 성공")
                    tts.addEventListener(ttsListener)
                    tts.setLocale(Locale.KOREA)
                }
                override fun onFailure(errorType: TtsError) = SignalHub.put("Voice/TTS SDK", "초기화 실패: $errorType")
            })
        } catch (e: Exception) {
            SignalHub.fail("Voice/TTS SDK", e)
        }
    }

    fun listen() = runCatching { stt.request() }.onFailure { SignalHub.fail("Voice/STT 요청", it) }
    fun stopListening() = runCatching { stt.stop() }.onFailure { SignalHub.fail("Voice/STT 중지", it) }
    fun speak(text: String) = runCatching { tts.speak(text) }.onFailure { SignalHub.fail("Voice/TTS 요청", it) }

    fun stop() {
        runCatching { stt.removeListener(sttListener) }
        runCatching { tts.removeEventListener(ttsListener) }
        runCatching { stt.release() }
        runCatching { tts.release() }
    }
}
