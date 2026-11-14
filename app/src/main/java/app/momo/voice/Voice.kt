package app.momo.voice

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class VoiceStatus(val label: String) { IDLE(""), LISTENING("듣고 있어요"), THINKING("생각 중"), SPEAKING("말하는 중") }

/** 음성 입출력. 실제 구현은 Pleos STT·TTS(PleosVoice), 시연 백업은 TextVoice. */
interface Voice {
    val status: StateFlow<VoiceStatus>
    /** 확정된 사용자 발화. STT 결과, adb 주입, 시연 버튼이 모두 여기로 들어온다. */
    val heard: SharedFlow<String>
    /** 인식 중간 결과 (화면에 '듣는 중' 표시용) */
    val partial: StateFlow<String>
    fun start()
    fun release()
    fun listen()
    fun stopListening()
    /** 한 문장을 말하고, 끝날 때까지 기다린다. 순서대로 부르면 문장 단위 스트리밍 재생이 된다. */
    suspend fun speak(text: String)
    fun stopSpeaking()
    fun setStatus(status: VoiceStatus)
    /** 시연·테스트용 입력: adb broadcast, 화면 버튼 */
    fun inject(text: String)
}

/** 소리 없이 동작하는 백업. 말하는 시간만 흉내 낸다. */
class TextVoice : Voice {
    private val _status = MutableStateFlow(VoiceStatus.IDLE)
    override val status = _status.asStateFlow()
    private val _heard = MutableSharedFlow<String>(extraBufferCapacity = 8)
    override val heard = _heard
    override val partial = MutableStateFlow("")

    override fun start() = Unit
    override fun release() = Unit
    override fun listen() { _status.value = VoiceStatus.LISTENING }
    override fun stopListening() { _status.value = VoiceStatus.IDLE }
    override suspend fun speak(text: String) {
        _status.value = VoiceStatus.SPEAKING
        delay(400L + text.length * 60L)
        _status.value = VoiceStatus.IDLE
    }
    override fun stopSpeaking() { _status.value = VoiceStatus.IDLE }
    override fun setStatus(status: VoiceStatus) { _status.value = status }
    override fun inject(text: String) { _heard.tryEmit(text) }
}
