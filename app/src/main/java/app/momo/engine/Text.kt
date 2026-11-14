package app.momo.engine

/** 한글 마지막 글자에 받침이 있는지. 조사(이/가, 은/는, 와/과)를 고르는 데 쓴다. */
internal fun hasBatchim(word: String): Boolean {
    val c = word.lastOrNull { it.isLetterOrDigit() } ?: return false
    return c in '가'..'힣' && (c - '가') % 28 != 0
}

internal fun String.josa(withBatchim: String, without: String) = this + if (hasBatchim(this)) withBatchim else without

/** '으로/로': ㄹ 받침 뒤에는 '로'를 쓴다. */
internal fun String.ro(): String {
    val c = lastOrNull { it.isLetterOrDigit() } ?: return this + "로"
    val jong = if (c in '가'..'힣') (c - '가') % 28 else 0
    return this + if (jong == 0 || jong == 8) "로" else "으로"
}

private val SENTENCE_END = Regex("""[.!?…]+["”’']*(?=\s|$)|\n""")

/** 버퍼 앞쪽의 완결된 문장 끝 위치. 아직 문장이 끝나지 않았으면 -1. */
internal fun sentenceEnd(buf: CharSequence): Int =
    SENTENCE_END.find(buf)?.let { m -> if (m.range.last + 1 < buf.length || m.value == "\n") m.range.last + 1 else -1 } ?: -1

/** 스트림이 끝났을 때처럼 전체 텍스트를 문장으로 나눈다. */
internal fun splitSentences(text: String): List<String> {
    val out = mutableListOf<String>()
    var rest = "$text "
    while (true) {
        val cut = sentenceEnd(rest)
        if (cut < 0) break
        out += rest.substring(0, cut).trim()
        rest = rest.substring(cut)
    }
    out += rest.trim()
    return out.filter { it.isNotBlank() }
}

/** LLM이 붙이는 "포포:" 머리말, 괄호 지문, 따옴표, 마크다운 기호를 걷어 낸다. */
internal fun cleanLine(text: String, speaker: String): String = text
    .replace(Regex("""^\s*["“]?\s*${Regex.escape(speaker)}\s*[:：]\s*"""), "")
    .replace(Regex("""\([^)]*\)|\*+|#+"""), "")
    .trim()
    .let { if (Regex("""^["“][^"“”]*["”]$""").matches(it)) it.substring(1, it.length - 1).trim() else it }
