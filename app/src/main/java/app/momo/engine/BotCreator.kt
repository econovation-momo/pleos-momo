package app.momo.engine

import app.momo.core.StoryBot
import app.momo.core.Turn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** 음성 제작의 고정 칸 4개. LLM이 자유 발화에서 채운다(JSON). */
data class BotDraft(
    val character: String? = null,  // 이름·생김새·성격
    val tone: String? = null,       // 말투
    val world: String? = null,      // 세계관
    val voice: String? = null,      // 목소리 느낌
) {
    val missing get() = listOfNotNull("캐릭터".takeIf { character == null }, "말투".takeIf { tone == null }, "세계관".takeIf { world == null }, "목소리".takeIf { voice == null })
    val complete get() = missing.isEmpty()
}

enum class ImageState { NONE, GENERATING, DONE }

data class CreateState(
    val dialog: List<Turn> = emptyList(),   // "AI" / "나"
    val question: String = FIRST_QUESTION,  // 지금 AI가 묻는 말
    val draft: BotDraft = BotDraft(),
    val busy: Boolean = false,              // LLM이 칸을 채우는 중
    val image: ImageState = ImageState.NONE,
    val bot: StoryBot? = null,              // 확정되면 역할·시작 설정까지 자동 생성된 봇
) {
    companion object { const val FIRST_QUESTION = "어떤 주인공을 만들고 싶어요? 자유롭게 말해 주세요." }
}

/**
 * 말로 봇 만들기 (F5).
 * TODO(엔진 세션): LLM으로 칸 채우기(JSON), 빈 칸만 다시 질문, 확정 시 역할·시작 설정 자동 생성, 이미지 선택을 구현한다.
 * 공개 API(state, reset, answer, confirm, regenerateImage)는 유지한다.
 */
class BotCreator(private val llm: LlmClient, private val scope: CoroutineScope) {
    private val _state = MutableStateFlow(CreateState())
    val state: StateFlow<CreateState> = _state.asStateFlow()

    fun reset() { _state.value = CreateState() }
    fun answer(text: String) = _state.update { it.copy(dialog = it.dialog + Turn("AI", it.question) + Turn("나", text)) }
    /** 요약 확인 후 '이대로 만들기': 이미지 생성 + 봇 확정 */
    fun confirm() = Unit
    fun regenerateImage() = Unit
}
