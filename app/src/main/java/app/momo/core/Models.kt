package app.momo.core

import androidx.annotation.DrawableRes

/** 크랙의 '스토리' 구조를 차량용으로 바꾼 봇. 엔딩 조건 대신 도착, 유저 페르소나 1명 대신 탑승자 역할 슬롯. */
data class StoryBot(
    val id: String,
    val title: String,
    val oneLiner: String,                 // 마켓 노출용 30자, AI에는 제목만 전달
    val description: String,
    val creator: String,
    val character: Character,
    val exampleDialogues: List<String>,   // 말투 예시 최대 3개
    val world: String,
    val keywordBook: Map<String, String> = emptyMap(), // 설정집: 키워드 등장 시 최대 3개 주입
    val starts: List<StartSetting>,       // 경로 길이별 시작 설정 → 마켓 n분 필터
    val roles: List<String>,              // 탑승자가 맡을 역할 1~4개
    val endings: List<Ending>,
    val rating: Rating,
    val tags: List<String> = emptyList(),
    val genre: String = "어드벤처",
    val chapters: List<String> = emptyList(), // 주행 화면 상단 진행 막대의 장 제목
    @DrawableRes val cover: Int? = null,      // 대표 이미지(주행 중 고정 표시)
    @DrawableRes val scenes: List<Int> = emptyList(), // 장면 그림(정차 중에만 표시)
    val status: BotStatus = BotStatus.PUBLIC,
    val plays: Int = 0,
) {
    val minPlayers get() = 1
    val maxPlayers get() = roles.size
    val minutes get() = starts.map { it.minutes }
    fun startFor(remainMin: Int): StartSetting = starts.minByOrNull { kotlin.math.abs(it.minutes - remainMin) } ?: starts.first()
}

data class Character(val name: String, val persona: String, val voice: String)
data class StartSetting(val minutes: Int, val prologue: String)
data class Ending(val title: String, val grade: String) // N / R / SR

enum class Rating(val label: String) { KIDS("키즈"), GENERAL("일반") }
enum class BotStatus(val label: String) { PRIVATE("비공개"), LINK("링크 공개"), REVIEW("검수 중"), PUBLIC("공개 중") }

enum class Phase(val label: String) { OPENING("시작"), RISING("전개"), CLIMAX("절정"), ENDING("결말"), DONE("끝") }

enum class SeatPos(val label: String, val short: String) {
    DRIVER("운전석", "운전"), PASSENGER("조수석", "조수"), REAR_LEFT("뒷좌석 왼쪽", "뒤 왼쪽"), REAR_RIGHT("뒷좌석 오른쪽", "뒤 오른쪽")
}

data class SeatState(val occupied: Boolean, val belted: Boolean)

/** 좌석 → 이름·역할. role이 null이면 듣기만 한다. */
data class Player(val seat: SeatPos, val name: String, val role: String?)

data class Turn(val speaker: String, val text: String)

data class EndingCard(
    val botId: String,
    val title: String,
    val grade: String,
    val summary: String,
    val destination: String,
    val minutes: Int,
    val players: List<String>,
    val date: String,
)

enum class Screen { CONNECT, HOME, DETAIL, MARKET, PLAYERS, PLAY, ENDING, MY, UPLOAD }

/** 화면 위에 뜨는 모달. 한 번에 하나만. */
enum class Overlay { NONE, CREATE, REPORT, KIDS_PIN, SHARE }
