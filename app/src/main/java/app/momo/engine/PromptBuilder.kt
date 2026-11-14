package app.momo.engine

import app.momo.core.Phase
import app.momo.core.Turn

/**
 * 크랙식 프롬프트 조립: 시스템 규칙 → 캐릭터·말투 예시 → 세계관·설정집 → 탑승자 역할 → 요약 기억·최근 대화 → 이번 지시.
 * Pleos LLM은 프롬프트 하나만 받으므로 PleosLlm이 system과 prompt를 이어 붙여 보낸다.
 * 차 안 온디바이스 모델을 가정해서 짧게 유지한다.
 */
object PromptBuilder {
    const val RECENT = 6
    private const val BOOK_MAX = 3

    fun story(c: Cue, history: List<Turn>): Pair<String, String> {
        val b = c.bot
        val name = b.character.name
        val chapterTitle = b.chapters.getOrNull(c.chapter)
        val recent = history.takeLast(RECENT)
        val older = history.dropLast(RECENT)
        val keyText = (recent.map { it.text } + listOfNotNull(c.heard, chapterTitle)).joinToString(" ")
        val book = b.keywordBook.filterKeys { it in keyText }.entries.take(BOOK_MAX)
        val target = c.next?.let { "${it.name}(${it.role})" } ?: "탑승자 모두"

        val system = buildString {
            appendLine("너는 '${b.title}' 이야기의 캐릭터 '$name'이다. 차 안의 탑승자들과 목소리로 함께 이야기를 만든다.")
            appendLine("[규칙]")
            appendLine("- 한 번에 3문장 이내, 모두 합쳐 120자 이내로 말한다. 해설·지문·괄호·이모지 없이 대사만 말한다.")
            appendLine("- 먼저 탑승자가 방금 한 말을 받아서 이야기에 반영한다. 엉뚱한 말도 이야기 속 사건으로 살린다.")
            if (!c.final) appendLine("- 마지막 문장에서 $target 을 이름으로 부르며 질문해서 차례를 넘긴다.")
            appendLine("- 지금 단계는 '${c.phase.label}'이다. ${guide(c.phase)} 도착까지 약 ${(c.remainSec + 59) / 60}분 남았다.")
            if (c.kids) appendLine("- 듣는 사람은 어린이다. 쉬운 말을 쓰고, 무섭거나 폭력적이거나 어른용인 내용은 말하지 않는다.")
            appendLine("- 차 밖으로 손이나 머리 내밀기, 창문·차 문 열기, 안전벨트 풀기, 운전자 방해 같은 위험한 행동은 절대 시키지 않는다.")
            appendLine("[캐릭터] ${b.character.persona} 목소리: ${b.character.voice}")
            if (b.exampleDialogues.isNotEmpty()) appendLine("[말투 예시]\n" + b.exampleDialogues.take(3).joinToString("\n"))
            appendLine("[세계관] ${b.world}")
            book.forEach { appendLine("[설정] ${it.key}: ${it.value}") }
            val prologue = b.startFor((c.remainSec + 59) / 60).prologue
            appendLine("[진행] 시작 설정: $prologue 지금 장: ${chapterTitle ?: "-"} (${c.chapter + 1}/${b.chapters.size.coerceAtLeast(1)}), 장면 ${c.sceneIndex + 1}/${c.sceneCount}")
            append("[탑승자와 역할]\n" + c.players.joinToString("\n") { "- ${it.name}: ${it.role ?: "듣기만 함(차례를 주지 않는다)"}" })
        }

        val prompt = buildString {
            if (older.isNotEmpty()) appendLine("[지난 이야기] " + memory(older, name))
            if (recent.isNotEmpty()) appendLine("[최근 대화]\n" + recent.joinToString("\n") { "${it.speaker}: ${it.text}" })
            appendLine(
                when {
                    c.final -> "[방금] 목적지 ${c.destination.ifBlank { "목적지" }}에 도착했다."
                    c.redirect -> "[방금] 이야기에 어울리지 않는 말이 나왔다. 그 말을 따라 하지 말고 자연스럽게 화제를 이야기로 돌린다."
                    c.heard != null -> "[방금] ${c.heardBy ?: "탑승자"}: ${c.heard}"
                    c.turn == 0 -> "[방금] 이야기를 시작한다. 인사하고 사건을 소개한다."
                    else -> "[방금] 아무도 대답하지 않았다. 이야기를 조금 진행한다."
                },
            )
            append(
                if (c.final) "[지시] 이야기를 따뜻하게 끝맺고 작별 인사를 한다. 질문하지 않는다. $name 의 대사만 말한다."
                else "[지시] 다음 차례는 $target 이다. $name 의 다음 대사만 말한다.",
            )
        }
        return system to prompt
    }

    private fun guide(p: Phase) = when (p) {
        Phase.OPENING -> "세계와 사건을 소개하고 첫 선택을 묻는다."
        Phase.RISING -> "사건을 키우고 작은 문제를 탑승자가 풀게 한다."
        Phase.CLIMAX -> "가장 큰 문제를 탑승자들이 힘을 모아 해결하게 한다."
        Phase.ENDING -> "새 사건을 만들지 말고 이야기를 마무리 쪽으로 이끈다."
        Phase.DONE -> "이야기를 끝낸다."
    }

    /** 요약 기억: LLM 요약 대신 오래된 턴에서 탑승자의 말만 추린다. */
    private fun memory(older: List<Turn>, name: String): String {
        val said = older.filter { it.speaker != name }.takeLast(5).joinToString(", ") { "${it.speaker} “${it.text.take(20)}”" }
        val lastBot = older.lastOrNull { it.speaker == name }?.text?.take(40)
        return listOfNotNull(said.ifEmpty { null }?.let { "탑승자들이 한 말: $it." }, lastBot?.let { "$name 의 이전 대사: $it" }).joinToString(" ")
    }

    /** 음성 제작: 자유 발화에서 고정 칸 4개를 JSON으로 뽑게 한다. */
    fun slots(draft: BotDraft, text: String): Pair<String, String> {
        val system = """
            너는 어린이와 함께 이야기 캐릭터를 만드는 도우미다. 사용자의 말에서 아래 칸에 해당하는 내용만 뽑아 JSON 한 줄로 답한다.
            말에 없는 칸은 null 로 둔다. 설명이나 다른 글 없이 JSON만 출력한다.
            {"name": "캐릭터 이름", "character": "생김새와 성격을 짧게", "tone": "말투", "world": "세계관(어디서 모험하는지)", "voice": "목소리 느낌"}
        """.trimIndent()
        val filled = listOfNotNull(
            draft.character?.let { "캐릭터=$it" }, draft.tone?.let { "말투=$it" }, draft.world?.let { "세계관=$it" }, draft.voice?.let { "목소리=$it" },
        ).joinToString(", ").ifEmpty { "없음" }
        return system to "이미 채운 칸: $filled\n사용자의 말: \"$text\""
    }
}
