package app.momo.engine

import app.momo.core.Phase
import app.momo.core.Player
import app.momo.core.StoryBot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** 이번 턴을 만드는 데 필요한 것 전부. PromptBuilder(LLM용)와 ScriptedLlm(대본용)이 같이 쓴다. */
data class Cue(
    val bot: StoryBot,
    val players: List<Player>,
    val next: Player?,          // 이번 대사 끝에서 이름을 부를 사람
    val phase: Phase,
    val chapter: Int,
    val sceneIndex: Int,
    val sceneCount: Int,
    val remainSec: Int,
    val turn: Int,              // 이 세션에서 캐릭터가 말한 횟수 (0 = 첫 인사)
    val heard: String?,         // 방금 들은 말. 안전 필터에 걸렸으면 null
    val heardBy: String?,
    val redirect: Boolean,      // 유해 입력: 화제를 돌린다
    val kids: Boolean,
    val destination: String,
    val final: Boolean = false, // 도착: 마무리 인사
)

/** 탑승자를 부르는 말. 포포는 "지우 탐험대원", 루루·콩이는 "지우 기사님"처럼 부른다. */
internal fun Cue.who(honor: Boolean = true): String =
    next?.let { "${it.name} ${it.role}" + if (honor) "님" else "" } ?: "모두"

internal val Cue.everyone get() = players.filter { it.role != null }.joinToString(", ") { it.name }.ifEmpty { "모두" }

/**
 * Pleos LLM이 안 될 때 쓰는 대본. LlmClient 모양을 그대로 따르지만 프롬프트는 읽지 않고, 엔진이 넣어 준 [cue]로 대사를 고른다.
 * dino·dragon·kong은 웹 데모처럼 갈래가 있는 대본, 그 밖의 봇은 이름과 역할을 끼워 넣는 범용 대본을 쓴다.
 * 예상하지 못한 말은 따옴표로 되받아 다음 대사에 넣는다.
 */
class ScriptedLlm : LlmClient {
    override val name = "대본"

    @Volatile var cue: Cue? = null

    private var scriptBotId: String? = null
    private var script: Script? = null
    private val used = mutableMapOf<Int, Int>()
    private var fillerPos = 0
    private var endingPos = 0
    private var echoPos = 0
    private var lastAnswers: List<Answer> = emptyList()

    fun reset() {
        used.clear(); fillerPos = 0; endingPos = 0; echoPos = 0; lastAnswers = emptyList(); scriptBotId = null
    }

    override fun stream(system: String, prompt: String): Flow<String> = flow {
        val c = cue ?: return@flow
        // 문장 단위로 흘려서 LLM과 같은 경로(문장마다 TTS)를 탄다
        splitSentences(line(c)).forEach { emit("$it "); delay(60) }
    }

    @Synchronized
    fun line(c: Cue): String {
        if (scriptBotId != c.bot.id) { reset(); scriptBotId = c.bot.id; script = scriptFor(c.bot) }
        val s = script!!
        if (c.final) return s.fill(s.final, c)

        val beat = when {
            c.phase == Phase.ENDING -> s.endings[minOf(endingPos++, s.endings.lastIndex)]
            else -> {
                val inChapter = s.beats.filter { it.ch == c.chapter }
                val i = used[c.chapter] ?: 0
                if (i < inChapter.size) inChapter[i].also { used[c.chapter] = i + 1 }
                else s.fillers[fillerPos++ % s.fillers.size]
            }
        }
        val heard = c.heard?.trim()?.takeIf { it.isNotEmpty() }
        val matched = heard?.let { h -> lastAnswers.firstOrNull { a -> a.keys.any { it in h } } }
        lastAnswers = beat.answers
        val react = when {
            c.redirect -> s.redirect
            matched != null -> matched.react
            heard != null && !trivial(heard) -> s.echo[echoPos++ % s.echo.size]
            else -> beat.react
        }
        val event = matched?.event ?: beat.event
        return s.fill(listOf(react, event, beat.ask).filter { it.isNotBlank() }.joinToString(" "), c)
    }

    private fun trivial(h: String) = h.length <= 3 || TRIVIAL.any { h.startsWith(it) } && h.length <= 6

    private companion object {
        val TRIVIAL = listOf("응", "어", "네", "예", "좋아", "그래", "가자", "오케이", "알았어", "몰라")
    }
}

internal class Beat(val ch: Int, val event: String, val ask: String, val react: String = "", val answers: List<Answer> = emptyList())

/** 이 대사의 질문에 나올 법한 대답. 키워드가 들리면 다음 대사의 첫 반응(과 사건)을 바꾼다. */
internal class Answer(val keys: List<String>, val react: String, val event: String? = null)

internal class Script(
    val honor: Boolean,
    val echo: List<String>,
    val redirect: String,
    val beats: List<Beat>,
    val fillers: List<Beat>,
    val endings: List<Beat>,
    val final: String,
) {
    fun fill(t: String, c: Cue) = t
        .replace("{who}", c.who(honor))
        .replace("{all}", c.everyone)
        .replace("{dest}", c.destination.ifBlank { "목적지" })
        .replace("{heard}", short(c.heard.orEmpty()))

    private fun short(h: String): String {
        val t = h.trim().trimEnd('.', '!', '?', '~', ' ')
        return if (t.length <= 16) t else t.take(16).substringBeforeLast(' ') + "…"
    }
}

private fun a(vararg keys: String, react: String, event: String? = null) = Answer(keys.toList(), react, event)

internal fun scriptFor(bot: StoryBot): Script = when (bot.id) {
    "dino" -> DINO
    "dragon" -> DRAGON
    "kong" -> KONG
    else -> generic(bot)
}

// 장: 공룡이 나타났다?! / 마주한 공룡의 왕 '티라노' / 박물관의 시간 문 / 하늘 길잡이 피코 / 공룡 숲으로
private val DINO = Script(
    honor = false,
    echo = listOf("헉, “{heard}”? 그거 좋은 생각이야!", "우와, “{heard}”라니 대단한데!", "“{heard}”? 좋아, 그것도 탐험에 넣자!"),
    redirect = "헉, 그 얘기는 잠깐 접어 두고 공룡을 찾아보자!",
    beats = listOf(
        Beat(0, "우와! 도시 한복판에 커다란 공룡 발자국이 쿵쿵 찍혀 있어!", "{who}, 발자국을 따라가 볼까?",
            answers = listOf(
                a("따라", "가자", "좋아", "응", react = "좋아, 살금살금 따라가자!"),
                a("무서", "싫", react = "헉, 나도 조금 무서워… 그래도 같이 가면 괜찮아!"),
                a("피자", "밥", "간식", "먹", react = "헉, 공룡도 배가 고플지 몰라! 간식을 챙겨 가자!"),
            )),
        Beat(0, "발자국 끝에 엄청 커다란 꼬리가 보여… 쿵!", "{who}, 무슨 공룡인지 맞혀 볼래?", react = "좋아, 출발!",
            answers = listOf(a("티라노", "렉스", react = "딩동댕! 티라노사우루스야!"))),
        Beat(0, "쿵쿵 소리에 길가 신호등이 흔들흔들해!", "{who}, 공룡한테 살살 걸으라고 말해 줄래?", react = "헉!"),
        Beat(1, "공룡의 왕 티라노 ‘렉스’가 나타났는데, 훌쩍훌쩍 울고 있어!", "{who}, 왜 우는지 물어봐 줄래?", react = "헉!",
            answers = listOf(
                a("배고", "밥", "먹", "피자", react = "맞아, 렉스는 배가 고파서 우는 거래!"),
                a("엄마", "집", "길", react = "맞아, 렉스는 집에 가는 길을 잃었대!"),
            )),
        Beat(1, "렉스는 배도 고프고 집도 잃어버렸대.", "{who}, 렉스한테 뭘 나눠 줄까?", react = "그렇구나!",
            answers = listOf(a("고기", "사과", "과자", "피자", "밥", "간식", "바나나", react = "냠냠, 렉스가 엄청 좋아해!"))),
        Beat(2, "렉스가 시립 자연사 박물관 쪽을 가리켜, 거기서 왔대!", "{who}, 박물관으로 가 볼까?", react = "어?"),
        Beat(2, "박물관 안에 빙글빙글 도는 시간 문이 있어!", "{who}, 시간 문을 열 주문을 외쳐 줄래?", react = "도착!",
            answers = listOf(a("열려", "수리", "주문", "얍", "짠", "열어", react = "우와, 시간 문이 반짝반짝 빛나!"))),
        Beat(3, "하늘에서 익룡 ‘피코’가 내려왔어, 길을 알려 주겠대!", "{who}, 피코한테 인사해 줄래?", react = "어? 저 위를 봐!",
            answers = listOf(a("안녕", "반가", react = "피코도 날개를 흔들며 인사해!"))),
        Beat(3, "피코가 공룡 숲으로 가는 구름 길을 보여 줬어.", "{who}, 왼쪽 구름이야, 오른쪽 구름이야?", react = "좋아!",
            answers = listOf(a("왼", react = "왼쪽 구름 길로 출발!"), a("오른", react = "오른쪽 구름 길로 출발!"))),
        Beat(4, "저기 초록빛 공룡 숲이 보여, 렉스가 꼬리를 흔들어!", "{who}, 렉스한테 응원 한마디 해 줄래?", react = "우와!"),
    ),
    fillers = listOf(
        Beat(0, "저쪽에서 아기 공룡이 쿵쿵 뛰어와!", "{who}, 아기 공룡 이름을 지어 줄래?", react = "헉!"),
        Beat(0, "공룡 발자국이 두 갈래로 나뉘었어!", "{who}, 어느 쪽으로 갈까?", react = "어?"),
        Beat(0, "공룡이 재채기를 했더니 바람이 휘잉 불어!", "{who}, 이 장면을 기록해 줄래?", react = "우와!"),
    ),
    endings = listOf(
        Beat(0, "이제 렉스를 집에 보내 줄 시간이야!", "{all}, 다 같이 ‘공룡 숲으로!’ 외쳐 줄래?", react = "대원들!"),
        Beat(0, "렉스가 고맙다고 크게 울었어, 크아앙!", "{who}, 렉스한테 마지막으로 하고 싶은 말 있어?", react = "우와!"),
        Beat(0, "곧 도착이야, 오늘 탐험 정말 멋졌어!", "{who}, 오늘 제일 좋았던 장면은 뭐야?", react = "고마워!"),
    ),
    final = "렉스가 무사히 공룡 숲으로 돌아갔어! {dest}에 도착했네, 대원들 정말 멋졌어. 다음 모험에서 또 만나!",
)

// 장: 사라진 알 / 구름 숲 / 깜깜한 동굴 / 거미줄 너머 / 알이 깨어나다
private val DRAGON = Script(
    honor = true,
    echo = listOf("“{heard}”? 우와, 그거 좋다~", "“{heard}”라니, 나 꼬리 흔들고 있어~", "“{heard}”? 좋아, 그렇게 해 보자~"),
    redirect = "음~ 그 얘기는 접어 두고 내 알을 찾으러 가자~",
    beats = listOf(
        Beat(0, "안녕, 나는 아기용 루루야~ 내 알이 사라졌어…", "{who}, 먼저 어느 길로 갈까?",
            answers = listOf(
                a("동굴", react = "좋아, 동굴 쪽으로 가자~"),
                a("피자", "먹", "배고", react = "피자? 사실 나 피자 진짜 좋아해~", event = "어, 치즈 자국이 구름 숲으로 이어져 있어!"),
                a("날아", "등에", "타고", "하늘", react = "꽉 잡아, 날아오른다~", event = "구름 숲 위에서 보니 저 아래 뭔가 반짝여!"),
            )),
        Beat(0, "구름 숲 입구에 반짝이는 알 껍데기 조각이 떨어져 있어!", "{who}, 우리 따라가 볼까?", react = "좋아~"),
        Beat(0, "숲속 어딘가에서 작은 울음소리가 들려…", "{who}, 무슨 소리인지 같이 들어 볼래?", react = "쉿~"),
        Beat(1, "구름 숲은 안개가 자욱하고, 작은 발자국이 보여!", "{who}, 길을 밝힐 주문을 외워 줄래?", react = "좋아, 출발이야~",
            answers = listOf(a("반짝", "빛", "주문", "수리", "얍", react = "우와, 주문이 통했어~"))),
        Beat(2, "환해지니까 깜깜한 동굴 입구가 보여.", "{who}, 동굴에 들어가 볼까?", react = "고마워~",
            answers = listOf(
                a("무서", react = "괜찮아, 내가 꼬리로 꼭 잡아 줄게~"),
                a("응", "좋아", "가자", "들어", react = "좋아, 살금살금~"),
            )),
        Beat(3, "저 끝에 내 알이 보이는데 거미줄이 막고 있어…", "{who}, 어떻게 할까?", react = "앗!",
            answers = listOf(
                a("자르", "잘라", "칼", "베", react = "앗, 거미가 놀라지 않게 살살 걷어 내자~"),
                a("부탁", "말", "친구", react = "거미야, 길 좀 비켜 줄래~?", event = "거미가 웃으면서 길을 비켜 줬어!"),
            )),
        Beat(3, "거미가 길을 비켜 줘서 알이 따뜻하게 빛나고 있어.", "{who}, 알을 살살 안아 줄래?", react = "휴~"),
        Beat(4, "알이 꿈틀꿈틀 움직여!", "{who}, 아기 용한테 노래를 불러 줄래?", react = "어, 어?"),
    ),
    fillers = listOf(
        Beat(0, "저기 반딧불이가 날아가!", "{who}, 반딧불이한테 길을 물어볼까?", react = "어?"),
        Beat(0, "바람이 휘잉 불어서 내 날개가 떨려~", "{who}, 나 좀 응원해 줄래?", react = "으으~"),
        Beat(0, "길이 두 갈래로 나뉘었어!", "{who}, 왼쪽이야, 오른쪽이야?", react = "앗!"),
    ),
    endings = listOf(
        Beat(0, "이제 알을 품을 시간이야~", "{all}, 다 같이 하나 둘 셋 외쳐 줄래?", react = "모두 모여~"),
        Beat(0, "알에 금이 가기 시작했어!", "{who}, 아기 용 이름을 지어 줄래?", react = "우와~"),
        Beat(0, "곧 도착이야, 오늘 정말 고마웠어~", "{who}, 오늘 제일 재미있었던 건 뭐야?", react = "헤헤~"),
    ),
    final = "알이 톡 깨지고 아기 용이 태어났어! {dest}에 도착했네, 다음에 또 만나~",
)

// 장: 사건 발생 / 단서 찾기 / 용의자 / 추리 / 사건 해결
private val KONG = Script(
    honor = true,
    echo = listOf("멍멍! “{heard}”, 좋은 생각이에요!", "“{heard}”요? 수첩에 적어 둘게요, 멍!", "멍! “{heard}”, 그게 단서일지도 몰라요!"),
    redirect = "멍멍! 그 얘기는 잠깐 접어 두고 사건에 집중해요!",
    beats = listOf(
        Beat(0, "멍멍! 큰일이에요, 놀이터 그네 줄이 사라졌어요!", "{who}, 어디부터 찾아볼까요?",
            answers = listOf(
                a("미끄럼", react = "미끄럼틀 밑이요? 좋아요!", event = "미끄럼틀 밑에 작은 발자국이 있어요!"),
                a("냄새", "킁", "맡아", react = "킁킁… 맡아 볼게요!", event = "모래밭 쪽에서 실 냄새가 나요!"),
            )),
        Beat(0, "놀이터 바닥에 수상한 실 한 가닥이 떨어져 있어요!", "{who}, 단서를 따라가 볼까요?", react = "좋아요!"),
        Beat(0, "그네 옆 모래에 이상한 무늬가 그려져 있어요.", "{who}, 이게 무슨 모양 같아요?", react = "멍?"),
        Beat(1, "모래밭에 작은 발자국이 찍혀 있어요!", "{who}, 증거 사진 찍어 줄래요?", react = "좋아요!",
            answers = listOf(a("찰칵", "찍", react = "찰칵! 사진 잘 나왔어요!"))),
        Beat(2, "사진에 줄을 물고 가는 다람쥐가 찍혔어요!", "{who}, 다람쥐한테 뭐라고 할까요?", react = "멍멍!",
            answers = listOf(
                a("놀자", "친구", react = "좋아요, 같이 놀자고 해 봐요!"),
                a("돌려", react = "다람쥐야, 줄 돌려줄래?"),
            )),
        Beat(3, "다람쥐가 둥지를 만들려고 줄을 가져갔대요.", "{who}, 대신 어떤 재료를 찾아 줄까요?", react = "아하!",
            answers = listOf(a("나뭇잎", "나뭇가지", "풀", "솜", "낙엽", react = "좋아요, 그걸로 둥지를 만들어 봐요!"))),
        Beat(4, "다람쥐 둥지가 거의 다 완성됐어요!", "{who}, 마지막 재료를 올려 줄래요?", react = "멍멍!"),
    ),
    fillers = listOf(
        Beat(0, "벤치 밑에서 반짝이는 단서를 찾았어요!", "{who}, 이게 뭘까요?", react = "멍?"),
        Beat(0, "고양이 한 마리가 수상하게 쳐다보고 있어요.", "{who}, 고양이한테 물어볼까요?", react = "쉿!"),
        Beat(0, "킁킁, 새로운 냄새가 나요!", "{who}, 냄새를 따라가 볼까요?", react = "멍!"),
    ),
    endings = listOf(
        Beat(0, "다람쥐 둥지가 완성됐어요!", "{all}, 다 같이 ‘사건 해결!’ 외쳐 줄래요?", react = "멍멍!"),
        Beat(0, "다람쥐가 고맙다고 도토리를 줬어요.", "{who}, 오늘 사건 이름을 지어 줄래요?", react = "우와!"),
        Beat(0, "곧 도착이에요, 오늘도 멋진 수사였어요!", "{who}, 제일 결정적인 단서는 뭐였어요?", react = "멍!"),
    ),
    final = "그네 줄도 돌아오고 다람쥐 친구도 생겼어요. {dest}에 도착했네요, 다음 사건에서 만나요! 멍멍!",
)

/** 사용자가 만든 봇을 포함한 나머지 봇: 이름·시작 설정·장 제목을 끼워 넣는다. */
private fun generic(bot: StoryBot): Script {
    val n = bot.character.name
    val chapters = bot.chapters.ifEmpty { listOf("모험의 시작", "첫 번째 문제", "커다란 위기", "힘을 모아", "집으로") }
    val prologue = bot.starts.firstOrNull()?.prologue?.trimEnd('.') ?: "오늘도 신나는 모험이 기다리고 있어요"
    return Script(
        honor = true,
        echo = listOf("“{heard}”, 좋아요, 그 생각도 이야기에 넣을게요!", "우와, “{heard}”라니 멋진데요!", "“{heard}”? 그렇게 해 봐요!"),
        redirect = "그 얘기는 잠깐 접어 두고 이야기로 돌아가요!",
        beats = listOf(Beat(0, "안녕하세요, 저는 ${n.josa("이에요", "예요")}! $prologue.", "{who}, 우리 뭐부터 해 볼까요?")) +
            chapters.drop(1).mapIndexed { i, t -> Beat(i + 1, "이번 이야기는 ‘$t’, 새로운 일이 기다리고 있어요.", "{who}, 여기서 무엇을 해 볼까요?", react = "좋아요!") },
        fillers = listOf(
            Beat(0, "앗, 저기 수상한 게 보여요!", "{who}, 가까이 가 볼까요?", react = "어?"),
            Beat(0, "갑자기 길이 막혀 버렸어요.", "{who}, 어떻게 지나가면 좋을까요?", react = "이런!"),
            Beat(0, "누군가 도움을 청하고 있어요!", "{who}, 뭐라고 말해 줄까요?", react = "쉿!"),
            Beat(0, "좋은 생각이 떠올랐어요.", "{who}, 다음엔 뭘 해 볼까요?", react = "음…"),
        ),
        endings = listOf(
            Beat(0, "드디어 마지막 순간이에요!", "{all}, 다 같이 힘을 모아 외쳐 줄래요?", react = "모두 모여요!"),
            Beat(0, "곧 도착이에요, 오늘 정말 즐거웠어요.", "{who}, 오늘 모험에서 뭐가 제일 좋았어요?", react = "고마워요!"),
        ),
        final = "{dest}에 도착했어요! 오늘 함께해서 정말 즐거웠어요, 다음에 또 만나요!",
    )
}
