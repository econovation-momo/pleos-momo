package app.momo.engine

import android.util.Log
import app.momo.R
import app.momo.core.AppStore
import app.momo.core.BotStatus
import app.momo.core.Character
import app.momo.core.Ending
import app.momo.core.Rating
import app.momo.core.StartSetting
import app.momo.core.StoryBot
import app.momo.core.Turn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

/** 음성 제작의 고정 칸 4개. LLM이 자유 발화에서 채운다(JSON). */
data class BotDraft(
    val character: String? = null,  // 이름·생김새·성격
    val tone: String? = null,       // 말투
    val world: String? = null,      // 세계관
    val voice: String? = null,      // 목소리 느낌
) {
    val missing get() = listOfNotNull("캐릭터".takeIf { character == null }, "말투".takeIf { tone == null }, "세계관".takeIf { world == null }, "목소리".takeIf { voice == null })
    val complete get() = missing.isEmpty()
    /** "콩이 · 하얀 강아지, 겁 많음" → "콩이" */
    val name get() = character?.substringBefore(" · ")?.substringBefore(",")?.trim()?.ifEmpty { null }

    /** 빈 칸만 다른 초안의 값으로 채운다. */
    fun merge(o: BotDraft) = BotDraft(character ?: o.character, tone ?: o.tone, world ?: o.world, voice ?: o.voice)
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
 * 한 발화에서 여러 칸을 채울 수 있고, 빈 칸만 다시 묻는다. LLM이 실패하면 키워드 휴리스틱으로 채워서 흐름이 끊기지 않게 한다.
 * 확정하면 표지를 프리셋에서 고르고 역할·시작 설정을 만들어 AppStore.addMyBot 까지 여기서 한다.
 */
class BotCreator(private val llm: LlmClient, private val scope: CoroutineScope) {
    private val answerLock = Mutex()
    private val _state = MutableStateFlow(CreateState())
    val state: StateFlow<CreateState> = _state.asStateFlow()
    private var llmFailed = false // 한 번 실패하면 앱이 꺼질 때까지 휴리스틱만 쓴다 (매 답마다 기다리지 않게)

    fun reset() { _state.value = CreateState() }

    /** LLM을 기다리는 동안 들어온 답도 버리지 않고 들어온 순서대로 처리한다. */
    fun answer(text: String) {
        if (text.isBlank()) return
        scope.launch { answerLock.withLock { handle(text) } }
    }

    private suspend fun handle(text: String) {
        if (_state.value.draft.complete) return
        _state.update { it.copy(dialog = it.dialog + Turn("AI", it.question) + Turn("나", text), busy = true) }
        run {
            if (SafetyFilter.unsafe(text, AppStore.state.value.kids)) {
                _state.update { it.copy(busy = false, question = "음, 그건 이야기에 넣기 어려워요. 다른 걸 말해 줄래요?") }
                return
            }
            val before = _state.value.draft
            val filled = before.merge(fromLlm(before, text) ?: BotDraft()).merge(Slots.heuristic(text, before.missing.firstOrNull()))
            _state.update { it.copy(draft = filled, busy = false, question = Slots.question(filled, before)) }
        }
    }

    private suspend fun fromLlm(draft: BotDraft, text: String): BotDraft? {
        if (llmFailed || llm is ScriptedLlm) return null
        return try {
            val (system, prompt) = PromptBuilder.slots(draft, text)
            val raw = withTimeoutOrNull(LLM_TIMEOUT_MS) { llm.stream(system, prompt).fold("") { acc, s -> acc + s } }
            if (raw.isNullOrBlank()) llmFailed = true
            raw?.let { Slots.parseJson(it) }.also { Log.i(TAG, "LLM 칸 채우기: $raw → $it") }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Log.w(TAG, "LLM 칸 채우기 실패, 휴리스틱으로: ${e.message}")
            llmFailed = true
            null
        }
    }

    /** 요약 확인 후 '이대로 만들기': 이미지 생성 + 봇 확정 */
    fun confirm() {
        val s = _state.value
        if (!s.draft.complete || s.image == ImageState.GENERATING) return
        val bot = Slots.buildBot(s.draft, System.currentTimeMillis())
        showImage(bot)
    }

    fun regenerateImage() {
        val bot = _state.value.bot ?: return
        if (_state.value.image == ImageState.GENERATING) return
        val next = COVERS[(COVERS.indexOf(bot.cover) + 1) % COVERS.size]
        showImage(bot.copy(cover = next, scenes = listOf(next)))
    }

    /** 이미지 생성 API가 없어서 프리셋 표지를 고르는 동안 '생성 중'을 보여 준다. */
    private fun showImage(bot: StoryBot) {
        _state.update { it.copy(image = ImageState.GENERATING) }
        scope.launch {
            delay(IMAGE_MS)
            AppStore.addMyBot(bot)
            _state.update { it.copy(image = ImageState.DONE, bot = bot) }
        }
    }

    private companion object {
        const val TAG = "Momo.Creator"
        const val LLM_TIMEOUT_MS = 4_000L
        const val IMAGE_MS = 2_500L
        val COVERS = listOf(R.drawable.cover_kong, R.drawable.cover_dragon, R.drawable.cover_dino, R.drawable.cover_space, R.drawable.cover_bugs)
    }
}

/** 칸 채우기: LLM JSON 파싱, 키워드 휴리스틱, 다음 질문, 봇 조립. 안드로이드 의존이 없어 단위 테스트한다. */
object Slots {
    private val CREATURES = listOf(
        "강아지", "고양이", "다람쥐", "햄스터", "토끼", "곰", "여우", "펭귄", "고래", "돌고래", "공룡", "드래곤", "용",
        "로봇", "외계인", "요정", "마법사", "공주", "왕자", "해적", "기사", "풍뎅이", "나비", "개미", "벌", "곤충", "유령", "괴물",
    )
    private val COLORS = mapOf("하얗" to "하얀", "하얀" to "하얀", "흰" to "하얀", "까맣" to "까만", "까만" to "까만", "검은" to "까만", "노랗" to "노란", "노란" to "노란", "빨간" to "빨간", "파란" to "파란", "갈색" to "갈색", "초록" to "초록")
    private val TRAITS = linkedMapOf(
        "겁 많" to "겁 많음", "겁많" to "겁 많음", "겁이 많" to "겁 많음", "용감" to "용감함", "씩씩" to "씩씩함", "수줍" to "수줍음",
        "장난" to "장난꾸러기", "호기심" to "호기심 많음", "똑똑" to "똑똑함", "귀여" to "귀여움", "먹보" to "먹보", "느긋" to "느긋함", "다정" to "다정함",
    )
    private val SOUNDS = listOf("멍멍", "야옹", "삐빅", "어흥", "크앙", "꽥꽥", "짹짹", "꿀꿀", "삐약", "찍찍")
    private val WORLDS = listOf(
        "놀이터", "학교", "유치원", "동네", "공원", "숲", "바다", "우주", "별", "성", "왕국", "마을", "도시", "섬", "정글", "사막",
        "동굴", "하늘", "사무소", "세계", "나라", "기지", "박물관", "도서관", "농장",
    )
    private val VOICES = listOf("목소리", "음성", "목청")

    /** LLM 응답에서 첫 JSON 객체의 문자열 칸만 꺼낸다. org.json 대신 정규식을 써서 JVM 단위 테스트에서도 돈다. */
    fun parseJson(text: String): BotDraft? {
        val obj = Regex("""\{[^{}]*\}""").find(text)?.value ?: return null
        fun get(k: String) = Regex(""""$k"\s*:\s*"((?:[^"\\]|\\.)*)"""").find(obj)?.groupValues?.get(1)
            ?.replace("\\\"", "\"")?.trim()?.takeIf { it.isNotEmpty() && it != "null" }
        val name = get("name")
        val ch = get("character")
        val character = if (name != null && ch != null) "$name · $ch" else name ?: ch
        return BotDraft(character, get("tone"), get("world"), get("voice")).takeIf { it != BotDraft() }
    }

    /** 키워드로 칸을 채운다. 지금 묻는 칸(target)이 끝까지 비면 발화 전체를 다듬어 넣어서 질문이 되풀이되지 않게 한다. */
    fun heuristic(text: String, target: String?): BotDraft {
        val t = text.trim()
        val creature = CREATURES.firstOrNull { t.has(it) }
        val name = Regex("""이름은\s*([가-힣A-Za-z]{1,6}?)(?:이야|야|이고|고|이에요|예요|이에|[\s,.!]|$)""").find(t)?.groupValues?.get(1)
            ?: creature?.let { c -> Regex(Regex.escape(c) + """\s+([가-힣]{1,4})(?=[!,.~\s]|$)""").find(t)?.groupValues?.get(1) }
                ?.takeUnless { n -> n.last() in "고서며게데니" || COLORS.keys.any { n.startsWith(it) } }
        val color = COLORS.entries.firstOrNull { it.key in t }?.value
        val traits = TRAITS.entries.filter { it.key in t }.map { it.value }.distinct()
        var character = if (creature != null || name != null) {
            val body = (listOf(listOfNotNull(color, creature).joinToString(" ")).filter { it.isNotEmpty() } + traits).joinToString(", ")
            if (name != null) listOf(name, body).filter { it.isNotEmpty() }.joinToString(" · ") else body
        } else null

        val polite = "존댓말" in t || "높임말" in t || "존대" in t
        val casual = "반말" in t
        val sound = SOUNDS.firstOrNull { it in t }
        var tone = when {
            sound != null && casual -> "“$sound, 내가 찾아볼게!” 반말"
            sound != null -> "“$sound, 제가 찾아볼게요!” 존댓말"
            polite -> "존댓말"
            casual -> "반말"
            "사투리" in t -> "사투리"
            else -> null
        }
        var voice = if (VOICES.any { it in t }) tidy(t) else null
        var world = if (voice == null && WORLDS.any { t.has(it) } && (creature == null || target == "세계관")) tidy(t) else null

        // 목소리 설명으로 분류된 말은 다른 칸에 억지로 넣지 않는다("밝은 아이 목소리로!"가 세계관에 들어가지 않게)
        if (voice == null || target == "목소리") when (target) {
            "캐릭터" -> if (character == null) character = tidy(t)
            "말투" -> if (tone == null) tone = tidy(t)
            "세계관" -> if (world == null) world = tidy(t)
            "목소리" -> if (voice == null) voice = tidy(t)
        }
        return BotDraft(character, tone, world, voice)
    }

    /** "우리 동네 놀이터가 탐정 사무소야." → "놀이터 탐정 사무소", "밝은 아이 목소리로!" → "밝은 아이 목소리" */
    fun tidy(text: String): String = text.trim().trimEnd('.', '!', '?', '~', ' ')
        .removePrefix("우리 동네 ").removePrefix("우리 ")
        .replace(Regex("""(?<=[가-힣])가\s"""), " ")
        .replace(Regex("""(으로요|이에요|입니다|이야|예요|이요|으로|로요|이지|야|요|로)$"""), "")
        .trim()

    fun question(d: BotDraft, before: BotDraft): String {
        val miss = d.missing
        val name = d.name ?: "주인공"
        if (miss.isEmpty()) return "정리해 볼게요. ${d.character}, 말투는 ${d.tone}, ${d.world}에서 모험하고 ${d.voice!!.ro()} 말해요. 이대로 만들까요?"
        val ask = when (miss.first()) {
            "캐릭터" -> "주인공은 누구예요? 이름이랑 어떤 친구인지 말해 주세요."
            "말투" -> "${name.josa("은", "는")} 어떤 말투로 말해요? 존댓말인지 반말인지도 알려 주세요."
            "세계관" -> "${name.josa("은", "는")} 어떤 세계에서 모험할까요?"
            else -> "목소리는 어떤 느낌이 좋아요?"
        }
        if (miss.size == 1) return "마지막으로, $ask"
        val ack = when {
            d.tone != null && before.tone == null -> "좋아요, 말투까지 알았어요! "
            d.character != null && before.character == null -> "좋아요, ${name}! "
            d.world != null && before.world == null -> "멋진 곳이네요! "
            d.voice != null && before.voice == null -> "좋아요! "
            else -> "조금만 더 알려 줄래요? "
        }
        return ack + ask
    }

    /** 키워드 포함 여부. 한 글자 낱말(용·성·별)은 '용감', '성격'에 걸리지 않게 낱말(+조사)로만 찾는다. */
    private fun String.has(vararg keys: String) = keys.any { k ->
        if (k.length > 1) k in this else Regex("""(?<![가-힣])$k(?:[이가은는을를의도와과랑]|(?![가-힣]))""").containsMatchIn(this)
    }

    fun cover(text: String): Int = when {
        text.has("강아지", "멍멍", "탐정") -> R.drawable.cover_kong
        text.has("용", "아기용", "드래곤", "마법", "왕국", "성") -> R.drawable.cover_dragon
        text.has("공룡", "티라노") -> R.drawable.cover_dino
        text.has("로봇", "우주", "별", "외계", "행성") -> R.drawable.cover_space
        text.has("곤충", "벌레", "풍뎅이", "나비", "개미", "벌", "숲") -> R.drawable.cover_bugs
        else -> R.drawable.cover_kong
    }

    private fun roles(text: String): List<String> = when {
        text.has("탐정") -> listOf("조수 탐정", "사진 담당")
        text.has("우주", "로봇", "별", "외계") -> listOf("조종사", "항해사", "정비사")
        text.has("용", "아기용", "드래곤", "마법", "왕국", "성") -> listOf("기사", "마법사", "길잡이 요정")
        text.has("공룡", "티라노") -> listOf("탐험대원", "기록 담당", "공룡 박사")
        text.has("바다", "해적", "섬") -> listOf("선장", "항해사", "보물 지도 담당")
        text.has("곤충", "풍뎅이", "나비", "개미") -> listOf("코치", "응원단장")
        else -> listOf("모험 대원", "길잡이", "기록 담당")
    }

    private fun genre(text: String) = when {
        text.has("탐정", "사건") -> "추리"
        text.has("우주", "로봇", "외계") -> "SF"
        text.has("용", "아기용", "드래곤", "마법", "요정", "왕국") -> "판타지"
        else -> "어드벤처"
    }

    /** 칸 4개로 봇 하나를 만든다. 역할 2~3개, 시작 설정 15·45분, 장 제목은 장르에 맞춰 자동으로 넣는다. */
    fun buildBot(d: BotDraft, now: Long): StoryBot {
        val name = d.name ?: "주인공"
        val character = d.character.orEmpty()
        val world = d.world ?: "우리 동네"
        val all = "$character ${d.tone} $world"
        val g = genre(all)
        val quote = Regex("""“([^”]+)”""").find(d.tone.orEmpty())?.groupValues?.get(1)
        val cover = cover(all)
        return StoryBot(
            id = "my_$now",
            title = "${name}의 $world".take(20),
            oneLiner = "방금 말로 만든 봇",
            description = "${character.replace(" · ", ", ")}. ${world}에서 모험해요.",
            creator = "나",
            character = Character(name, "$character. 말투: ${d.tone}.", d.voice ?: "밝은 아이 목소리"),
            exampleDialogues = listOf("$name: " + (quote ?: "안녕, 나는 ${name.josa("이야", "야")}! 같이 모험 떠나 볼까?")),
            world = world,
            starts = listOf(
                StartSetting(15, "${world}에 작은 사건이 생겼다. 짧고 굵은 모험."),
                StartSetting(45, "${world}에 커다란 사건이 생겼다. 단서를 모아 끝까지 해결하자."),
            ),
            roles = roles(all),
            endings = listOf(Ending("${name.josa("과", "와")} 함께한 첫 모험", "R"), Ending("${name}의 비밀", "SR"), Ending("다음에 또 만나", "N")),
            rating = Rating.KIDS,
            tags = listOfNotNull(CREATURES.firstOrNull { character.has(it) }, WORLDS.firstOrNull { world.has(it) }, "내가 만든 봇"),
            genre = g,
            chapters = if (g == "추리") listOf("사건 발생", "단서 찾기", "용의자", "추리", "사건 해결")
            else listOf("모험의 시작", "첫 번째 문제", "커다란 위기", "힘을 모아", "집으로"),
            cover = cover,
            scenes = listOf(cover),
            status = BotStatus.PRIVATE,
        )
    }
}
