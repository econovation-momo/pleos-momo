package app.momo.engine

import android.util.Log
import app.momo.car.CarEvent
import app.momo.car.CarSource
import app.momo.core.AppStore
import app.momo.core.Ending
import app.momo.core.EndingCard
import app.momo.core.Phase
import app.momo.core.Player
import app.momo.core.StoryBot
import app.momo.core.Turn
import app.momo.voice.Voice
import app.momo.voice.VoiceStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

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

/** PRD 8장 페이싱 규칙. 장면 하나는 약 2분이다. */
object Pacing {
    const val SCENE_SEC = 120
    const val ENDING_SEC = 300      // 남은 5분 이하면 결말
    const val RESYNC_SEC = 180      // 내비와 3분 넘게 차이 나면 다시 계산
    const val TURNS_PER_CHAPTER = 3 // 시간과 별개로 3턴마다 한 장씩 앞서 갈 수 있다 (시연이 짧아도 장이 넘어가게)

    fun sceneCount(remainSec: Int) = (remainSec / 60 / 2).coerceAtLeast(1)

    fun sceneIndex(sceneCount: Int, remainSec: Int) =
        (sceneCount - (remainSec + SCENE_SEC - 1) / SCENE_SEC).coerceIn(0, (sceneCount - 1).coerceAtLeast(0))

    fun needsResync(localSec: Int, naviSec: Int) = abs(localSec - naviSec) > RESYNC_SEC

    /** 재탐색·정체로 남은 시간이 바뀌면 지금 장면 뒤로 남은 장면 수만 다시 잡는다. */
    // sceneIndex와 같은 올림 기준을 써야 재계산 뒤에도 장면 번호가 시간에 따라 올라간다
    fun resyncedCount(currentIndex: Int, newRemainSec: Int) = currentIndex + (newRemainSec + SCENE_SEC - 1) / SCENE_SEC

    /** 시간 기준 장에, 턴 수 기준으로 한 장까지 앞서 가는 것을 허용한다. */
    fun chapter(chapters: Int, sceneIndex: Int, sceneCount: Int, turns: Int, ending: Boolean): Int {
        if (chapters <= 0) return 0
        if (ending) return chapters - 1
        val byTime = sceneIndex * chapters / sceneCount.coerceAtLeast(1)
        return maxOf(byTime, minOf(byTime + 1, turns / TURNS_PER_CHAPTER)).coerceAtMost(chapters - 1)
    }

    fun phase(remainSec: Int, sceneIndex: Int, sceneCount: Int, chapter: Int, chapters: Int): Phase {
        if (remainSec <= ENDING_SEC) return Phase.ENDING
        val p = maxOf(sceneIndex.toFloat() / sceneCount.coerceAtLeast(1), if (chapters > 0) chapter.toFloat() / chapters else 0f)
        return when {
            p < 0.2f -> Phase.OPENING
            p < 0.7f -> Phase.RISING
            else -> Phase.CLIMAX
        }
    }

    /** 참여가 많을수록 높은 등급 */
    fun grade(heardCount: Int) = when {
        heardCount >= 6 -> "SR"
        heardCount >= 3 -> "R"
        else -> "N"
    }
}

/**
 * 세션 엔진: 페이싱(장면 수·단계), 차례 넘기기, 프롬프트 조립, 안전 필터, 도착 엔딩.
 * 턴 루프: LLM 스트리밍 → 문장 단위로 voice.speak → voice.listen → 들은 말 → 다음 턴.
 * Pleos LLM이 실패하거나 첫 조각이 4초 안에 안 오면 그 턴부터 이 세션이 끝날 때까지 대본(ScriptedLlm)을 쓴다.
 */
class StoryEngine(
    private val llm: LlmClient,
    private val voice: Voice,
    private val car: CarSource,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(PlayState())
    val state: StateFlow<PlayState> = _state.asStateFlow()

    private val script = ScriptedLlm()
    private var useScript = false
    private var fromScript = false // 이번 턴이 대본에서 나왔는지. 대본은 문장 수를 이미 맞춰 두었으므로 그대로 다 말한다.
    private var session: Job? = null
    private var turns: Job? = null
    private var inputs = Channel<String>(Channel.CONFLATED)
    private val history = mutableListOf<Turn>()
    private var turnNo = 0
    private var heardCount = 0
    private var bestHeard = ""
    private var naviMode = false
    private var arriving = false
    private var startedAt = 0L

    fun start(bot: StoryBot, players: List<Player>, destination: String, budgetSec: Int) {
        stop()
        script.reset()
        useScript = llm is ScriptedLlm
        inputs = Channel(Channel.CONFLATED)
        history.clear(); turnNo = 0; heardCount = 0; bestHeard = ""; arriving = false
        // 남은 1분 이하인 목적지(도착 직후 남은 값 등)는 내비 목적지로 보지 않고 고른 길이 타이머를 쓴다
        naviMode = (car.state.value.remainSec ?: 0) > 60
        startedAt = System.currentTimeMillis()
        val budget = budgetSec.coerceAtLeast(60)
        _state.value = repace(
            PlayState(
                bot = bot, players = players, destination = destination,
                budgetSec = budget, remainSec = budget, sceneCount = Pacing.sceneCount(budget),
                line = bot.startFor(budget / 60).prologue, turnOf = rolePlayers(players).firstOrNull(),
                llmName = if (useScript) script.name else llm.name,
            ),
        )
        Log.i(TAG, "시작 ${bot.id} ${budget}s 내비=$naviMode 장면=${_state.value.sceneCount} llm=${_state.value.llmName}")
        session = scope.launch {
            launch { clock() }
            launch { car.events.collect { if (it == CarEvent.Arrived) arrive() else car.refreshDestination() } }
            launch { car.state.map { it.remainSec }.distinctUntilChanged().collect { r -> if (r != null && r > 0) { naviMode = true; resync(r) } } }
            launch { voice.status.collect { s -> _state.update { it.copy(status = s) } } }
            launch { voice.heard.collect { inputs.trySend(it) } }
        }
        turns = scope.launch { loop() }
    }

    /** 사용자 발화. STT 결과와 시연 버튼이 모두 이리로 온다. */
    fun say(text: String) {
        if (_state.value.running && text.isNotBlank()) inputs.trySend(text.trim())
    }

    /** 시연용: 도착 5분 전으로 건너뛰기 */
    fun skipToEnding() = _state.update { if (it.running && it.remainSec > Pacing.ENDING_SEC) repace(it.copy(remainSec = Pacing.ENDING_SEC)) else it }

    /** 도착: 진행 중인 턴을 끊고 마무리 대사를 한 뒤 엔딩 카드를 만든다. */
    fun arrive() {
        val s = _state.value
        if (!s.running || arriving) return
        arriving = true
        turns?.cancel()
        voice.stopSpeaking(); voice.stopListening()
        scope.launch {
            turns?.join()
            val bot = s.bot ?: return@launch
            runCatching { speakTurn(heard = null, next = null, final = true) }.onFailure { if (it is CancellationException) throw it }
            val card = endingCard(bot, _state.value)
            AppStore.addEndingCard(card)
            _state.update { it.copy(phase = Phase.DONE, ending = card, turnOf = null, remainSec = 0) }
            voice.setStatus(VoiceStatus.IDLE)
            session?.cancel()
            Log.i(TAG, "도착: ${card.title} ${card.grade}")
        }
    }

    fun stop() {
        session?.cancel(); turns?.cancel()
        session = null; turns = null
        voice.stopSpeaking(); voice.stopListening()
        _state.value = PlayState()
    }

    private suspend fun clock() {
        var sec = 0
        var zeroFor = 0
        while (true) {
            delay(1000)
            sec++
            if (sec % 60 == 0) car.refreshDestination()
            _state.update { if (it.running) repace(it.copy(remainSec = (it.remainSec - 1).coerceAtLeast(0))) else it }
            // 내비 목적지가 없으면 시간이 다 되는 즉시, 내비가 있으면 도착 이벤트를 60초 더 기다린 뒤 도착으로 본다
            zeroFor = if (_state.value.remainSec == 0) zeroFor + 1 else 0
            if (zeroFor > 0 && (!naviMode || zeroFor >= 60)) arrive()
        }
    }

    private fun resync(naviSec: Int) = _state.update {
        if (!it.running || !Pacing.needsResync(it.remainSec, naviSec)) return@update it
        Log.i(TAG, "남은 시간 재계산 ${it.remainSec}s → ${naviSec}s")
        repace(it.copy(remainSec = naviSec, sceneCount = Pacing.resyncedCount(it.sceneIndex, naviSec)))
    }

    private fun repace(s: PlayState): PlayState {
        val n = s.bot?.chapters?.size ?: 0
        val idx = Pacing.sceneIndex(s.sceneCount, s.remainSec)
        val ending = s.remainSec <= Pacing.ENDING_SEC
        val ch = maxOf(s.chapter, Pacing.chapter(n, idx, s.sceneCount, turnNo, ending))
        val phase = if (s.phase == Phase.DONE) Phase.DONE else Pacing.phase(s.remainSec, idx, s.sceneCount, ch, n)
        return s.copy(sceneIndex = idx, chapter = ch, phase = phase)
    }

    private fun rolePlayers(players: List<Player>) = players.filter { it.role != null }

    private suspend fun loop() {
        var heard: String? = null
        while (_state.value.running) {
            val roles = rolePlayers(_state.value.players)
            val next = roles.getOrNull(turnNo % roles.size.coerceAtLeast(1))
            speakTurn(heard, next, final = false)
            turnNo++
            _state.update { repace(it) }

            voice.listen()
            val input = withTimeoutOrNull(SILENCE_MS) { inputs.receive() }
            voice.stopListening()
            voice.setStatus(VoiceStatus.THINKING) // 입력 직후 0.25초 안에 '생각 중'
            heard = input
            if (input != null) {
                heardCount++
                if (input.length > bestHeard.length) bestHeard = input
                _state.update { it.copy(lastHeard = input, lastHeardBy = next?.name) }
                Log.i(TAG, "들음 ${next?.name}: $input")
            }
        }
    }

    /** 한 턴을 만들고 말한다. 첫 문장은 바로 말하고, 마지막 문장은 끝까지 받아 본 뒤 호명을 확인하고 말한다. */
    private suspend fun speakTurn(heard: String?, next: Player?, final: Boolean): String {
        val s = _state.value
        val bot = s.bot ?: return ""
        val kids = AppStore.state.value.kids
        val redirect = heard != null && SafetyFilter.unsafe(heard, kids)
        val cue = Cue(
            bot = bot, players = s.players, next = next, phase = s.phase, chapter = s.chapter,
            sceneIndex = s.sceneIndex, sceneCount = s.sceneCount, remainSec = s.remainSec, turn = turnNo,
            heard = heard.takeUnless { redirect }, heardBy = s.turnOf?.name, redirect = redirect, kids = kids,
            destination = s.destination, final = final,
        )
        script.cue = cue
        val (system, prompt) = PromptBuilder.story(cue, history)
        _state.update { it.copy(prevLine = it.line, line = "", turnOf = next ?: it.turnOf) }
        voice.setStatus(VoiceStatus.THINKING)

        val spoken = mutableListOf<String>()
        var pending: String? = null
        val name = bot.character.name
        suspend fun out(sentence: String) {
            val safe = SafetyFilter.cleanOutput(sentence, kids)
            spoken += safe
            _state.update { it.copy(line = spoken.joinToString(" ").take(MAX_LINE)) }
            voice.speak(safe)
        }
        suspend fun take(raw: String) {
            val sentence = cleanLine(raw, name).ifBlank { return }
            when {
                fromScript || spoken.isEmpty() && pending == null -> out(sentence)
                else -> { pending?.let { if (spoken.size < 2) out(it) }; pending = sentence }
            }
        }

        val buf = StringBuilder()
        turnStream(system, prompt).collect { chunk ->
            buf.append(chunk)
            while (true) {
                val cut = sentenceEnd(buf)
                if (cut < 0) break
                val sentence = buf.substring(0, cut)
                buf.delete(0, cut)
                take(sentence)
            }
        }
        take(buf.toString())

        // 이름을 불러 차례를 넘기지 않았으면 마지막 문장 앞에 호명을 붙인다
        val who = cue.who(honor = bot.id != "dino")
        val called = next == null || final || (spoken + listOfNotNull(pending)).any { next.name in it }
        when {
            pending != null -> out(if (called) pending!! else "$who, $pending")
            spoken.isEmpty() -> splitSentences(script.line(cue)).forEach { out(it) } // 아무것도 못 받았으면 대본으로 메운다
            !called -> out(if (bot.id == "dino") "$who, 어떻게 생각해?" else "$who, 어떻게 생각해요?")
        }
        val line = spoken.joinToString(" ")
        heard?.let { history += Turn(s.turnOf?.name ?: "탑승자", it) }
        history += Turn(name, line)
        Log.i(TAG, "턴 $turnNo [${_state.value.llmName}] ${s.phase} ch${s.chapter}: $line")
        return line
    }

    /** Pleos LLM 스트림. 실패하거나 첫 조각이 늦으면 대본으로 바꾸고, 그 뒤로는 계속 대본을 쓴다. */
    private fun turnStream(system: String, prompt: String): Flow<String> = flow {
        fromScript = false
        if (!useScript) {
            var got = false
            try {
                coroutineScope {
                    val ch = produce(capacity = Channel.UNLIMITED) { llm.stream(system, prompt).collect { send(it) } }
                    var wait = FIRST_CHUNK_MS
                    while (true) {
                        val r = withTimeoutOrNull(wait) { ch.receiveCatching() }
                        if (r == null) { Log.w(TAG, if (got) "조각 사이가 ${wait}ms 넘게 끊김" else "첫 조각이 ${wait}ms 안에 안 옴"); break }
                        if (r.isClosed) { r.exceptionOrNull()?.let { throw it }; break }
                        val v = r.getOrThrow()
                        if (v.isNotEmpty()) { got = true; emit(v) }
                        wait = NEXT_CHUNK_MS
                    }
                    ch.cancel()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Log.w(TAG, "LLM 실패: ${e.message}")
            }
            if (got) return@flow
            useScript = true
            _state.update { it.copy(llmName = script.name) }
            Log.w(TAG, "대본으로 전환")
        }
        fromScript = true
        emitAll(script.stream(system, prompt))
    }

    private fun endingCard(bot: StoryBot, s: PlayState): EndingCard {
        val grade = Pacing.grade(heardCount)
        val ending = bot.endings.firstOrNull { it.grade == grade } ?: bot.endings.firstOrNull() ?: Ending("${bot.character.name.josa("과", "와")} 함께한 모험", grade)
        val crew = rolePlayers(s.players).map { "${it.name} ${it.role}" }
        val who = if (crew.isEmpty()) "우리" else crew.joinToString(", ")
        val chapter = bot.chapters.getOrNull(s.chapter)
        val summary = buildString {
            append("${who.josa("이", "가")} ${bot.character.name.josa("과", "와")} 함께 ")
            append(if (chapter != null) "‘$chapter’까지 이야기를 만들었어요." else "이야기를 끝까지 만들었어요.")
            if (bestHeard.isNotBlank()) append(" “${bestHeard.take(20)}” 한마디가 이야기를 바꿨어요.")
        }
        // 실제로 탄 시간(시연에서 도착 버튼을 일찍 눌러도 출발 예산이 아니라 탄 만큼)
        val minutes = ((System.currentTimeMillis() - startedAt) / 60_000).toInt().coerceAtLeast(1)
        return EndingCard(
            botId = bot.id, title = ending.title, grade = grade, summary = summary,
            destination = s.destination.ifBlank { "목적지" }, minutes = minutes,
            players = rolePlayers(s.players).map { it.name }, date = SimpleDateFormat("MM/dd", Locale.KOREA).format(Date()),
        )
    }

    private companion object {
        const val TAG = "Momo.Engine"
        const val FIRST_CHUNK_MS = 4_000L
        const val NEXT_CHUNK_MS = 6_000L
        const val SILENCE_MS = 30_000L
        const val MAX_LINE = 120
    }
}
