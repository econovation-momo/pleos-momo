package app.momo.engine

import app.momo.car.CarSource
import app.momo.core.EndingCard
import app.momo.core.Phase
import app.momo.core.Player
import app.momo.core.StoryBot
import app.momo.voice.Voice
import app.momo.voice.VoiceStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** 주행 화면이 그리는 상태 전부. 화면은 이 값만 보고 그린다. */
data class PlayState(
    val bot: StoryBot? = null,
    val players: List<Player> = emptyList(),
    val destination: String = "",
    val phase: Phase = Phase.OPENING,
    val line: String = "",              // 캐릭터의 현재 대사 (주행 중 120자 이내)
    val prevLine: String = "",          // 직전 대사 (흐리게 표시)
    val turnOf: Player? = null,         // 지금 말할 차례
    val lastHeard: String? = null,      // 방금 들은 말
    val lastHeardBy: String? = null,    // 방금 말한 사람 이름 (호명 기준 추정)
    val chapter: Int = 0,               // bot.chapters 진행 위치 (0부터)
    val sceneIndex: Int = 0,
    val sceneCount: Int = 0,            // 목표 장면 수 = 남은 분 ÷ 2
    val budgetSec: Int = 0,             // 출발 때 남은 시간
    val remainSec: Int = 0,
    val status: VoiceStatus = VoiceStatus.IDLE,
    val llmName: String = "",           // "Pleos LLM" / "대본"
    val ending: EndingCard? = null,     // 도착하면 채워진다
) {
    val running get() = bot != null && phase != Phase.DONE
}

/**
 * 세션 엔진: 페이싱(장면 수·단계), 차례 넘기기, 프롬프트 조립, 안전 필터, 도착 엔딩.
 * TODO(엔진 세션): 아래는 화면 작업용 임시 구현이다. 공개 API(state, start, say, skipToEnding, arrive, stop)는 유지한다.
 */
class StoryEngine(
    private val llm: LlmClient,
    private val voice: Voice,
    private val car: CarSource,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(PlayState())
    val state: StateFlow<PlayState> = _state.asStateFlow()

    fun start(bot: StoryBot, players: List<Player>, destination: String, budgetSec: Int) {
        val first = players.firstOrNull { it.role != null }
        _state.value = PlayState(
            bot = bot, players = players, destination = destination,
            budgetSec = budgetSec, remainSec = budgetSec, sceneCount = (budgetSec / 120).coerceAtLeast(3),
            line = bot.startFor(budgetSec / 60).prologue, turnOf = first, llmName = llm.name,
        )
    }

    /** 사용자 발화. STT 결과와 시연 버튼이 모두 이리로 온다. */
    fun say(text: String) = _state.update { it.copy(prevLine = it.line, lastHeard = text, lastHeardBy = it.turnOf?.name, sceneIndex = it.sceneIndex + 1) }

    /** 시연용: 도착 5분 전으로 건너뛰기 */
    fun skipToEnding() = _state.update { it.copy(phase = Phase.ENDING, remainSec = 300) }

    fun arrive() = _state.update {
        val bot = it.bot ?: return@update it
        it.copy(
            phase = Phase.DONE,
            ending = EndingCard(bot.id, bot.endings.first().title, bot.endings.first().grade, "오늘의 이야기를 함께 끝냈어요.", it.destination, it.budgetSec / 60, it.players.filter { p -> p.role != null }.map { p -> p.name }, "11/14"),
        )
    }

    fun stop() { _state.value = PlayState() }
}
