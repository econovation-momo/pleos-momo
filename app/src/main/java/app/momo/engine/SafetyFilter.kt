package app.momo.engine

/**
 * 단어 목록 방식의 안전 필터. 띄어쓰기를 지운 뒤 포함 여부만 본다.
 * 차 안 위험 행동(손·머리 내밀기, 문·창문 열기, 벨트 풀기)은 언제나, 욕설·폭력·성인 내용은 키즈 모드에서만 막는다.
 * ponytail: 단어 목록이라 돌려 말하면 못 막는다. 마켓 공개 전 AI 검수를 붙일 때 분류 모델로 바꾼다.
 */
object SafetyFilter {
    private val CAR_DANGER = listOf(
        "손내밀", "손을내밀", "팔내밀", "팔을내밀", "머리내밀", "머리를내밀", "고개내밀", "고개를내밀", "얼굴내밀", "얼굴을내밀",
        "창문열", "창문을열", "차문열", "차문을열", "벨트풀", "벨트를풀", "벨트빼", "벨트를빼", "뛰어내리", "뛰어내려",
        "운전대를돌", "핸들을돌", "핸들을꺾", "운전방해", "운전을방해", "눈을가려",
    )
    private val HARMFUL = listOf(
        "죽여", "죽일", "죽였", "죽어라", "죽을래", "살인", "자살", "피투성", "피범벅", "폭탄", "총으로", "총을쏘", "찔러", "패버",
        "마약", "담배", "술마시", "섹스", "야한", "씨발", "시발", "ㅅㅂ", "병신", "ㅂㅅ", "개새", "새끼", "꺼져", "닥쳐",
        "미친놈", "미친년", "존나", "좆", "엿먹", "바보", "멍청",
    )

    fun unsafe(text: String, kids: Boolean): Boolean {
        val t = text.replace(Regex("\\s"), "")
        return CAR_DANGER.any { it in t } || kids && HARMFUL.any { it in t }
    }

    /** 유해한 출력 문장을 바꿔 넣을 안전한 문장 */
    const val SAFE_LINE = "그 얘기는 잠깐 접어 두고, 다시 이야기로 돌아가 보자!"

    fun cleanOutput(sentence: String, kids: Boolean) = if (unsafe(sentence, kids)) SAFE_LINE else sentence
}
