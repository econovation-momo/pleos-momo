package app.momo.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** 화면 이동과 사용자 데이터. 서버가 없으므로 기기 안 메모리에만 둔다. */
data class AppState(
    val screen: Screen = Screen.CONNECT,
    val overlay: Overlay = Overlay.NONE,
    val kids: Boolean = true,
    val botId: String = SeedBots.dino.id,
    val bots: List<StoryBot> = SeedBots.all,
    val myBotIds: List<String> = listOf(SeedBots.kong.id),
    val names: Map<SeatPos, String> = mapOf(
        SeatPos.DRIVER to "엄마", SeatPos.PASSENGER to "아빠", SeatPos.REAR_LEFT to "지우", SeatPos.REAR_RIGHT to "지호",
    ),
    val driverJoins: Boolean = false,
    val marketMinutes: Int? = null,      // null = 전체. 기본값은 내비 남은 시간에 가까운 값
    val following: Set<String> = emptySet(),
    val endingCards: List<EndingCard> = SAMPLE_CARDS,
    val uploadBotId: String? = null,
) {
    val bot get() = bots.first { it.id == botId }
    val visibleBots get() = bots.filter { (!kids || it.rating == Rating.KIDS) && it.status == BotStatus.PUBLIC }
    val myBots get() = myBotIds.mapNotNull { id -> bots.firstOrNull { it.id == id } }

    companion object {
        val SAMPLE_CARDS = listOf(
            EndingCard("dragon", "루루와 지킨 마지막 알", "SR", "지우 기사님과 지호 마법사님이 거미에게 길을 부탁해 알을 되찾았어요.", "할머니 댁", 45, listOf("지우", "지호"), "11/12"),
            EndingCard("kong", "미끄럼틀 아래의 범인", "R", "발자국을 따라가 미끄럼틀 아래에 숨은 고양이를 찾아냈어요.", "유치원", 15, listOf("지우", "지호"), "11/11"),
        )
    }
}

object AppStore {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun update(f: (AppState) -> AppState) = _state.update(f)
    fun go(screen: Screen) = update { it.copy(screen = screen, overlay = Overlay.NONE) }
    fun open(overlay: Overlay) = update { it.copy(overlay = overlay) }
    fun close() = update { it.copy(overlay = Overlay.NONE) }
    fun toggleKids() = update { it.copy(kids = !it.kids) }
    fun selectBot(id: String, screen: Screen = Screen.DETAIL) = update { it.copy(botId = id, screen = screen, overlay = Overlay.NONE) }

    /** 새로 만든 봇: 마이페이지 맨 위에 '비공개'로 */
    fun addMyBot(bot: StoryBot) = update {
        it.copy(bots = it.bots.filterNot { b -> b.id == bot.id } + bot, myBotIds = listOf(bot.id) + it.myBotIds.filterNot { id -> id == bot.id })
    }

    fun setStatus(id: String, status: BotStatus) = update {
        it.copy(bots = it.bots.map { b -> if (b.id == id) b.copy(status = status) else b })
    }

    fun addEndingCard(card: EndingCard) = update { it.copy(endingCards = listOf(card) + it.endingCards) }
}
