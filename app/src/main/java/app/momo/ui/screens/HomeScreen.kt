package app.momo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.momo.car.CarState
import app.momo.core.AppState
import app.momo.core.AppStore
import app.momo.core.Overlay
import app.momo.core.Screen
import app.momo.core.StoryBot
import app.momo.ui.Momo
import app.momo.ui.components.*
import app.momo.ui.components.MicIcon
import app.momo.ui.components.PillButton
import app.momo.ui.components.destLabel
import app.momo.ui.components.metaLine
import app.momo.ui.components.rideMin
import kotlinx.coroutines.launch
import kotlin.math.abs

private val CARD_W = 313.dp

/** S1 홈: Figma 21:122 동화 선택 캐러셀. 남은 시간에 맞는 봇이 가운데부터 온다. */
@Composable
fun HomeScreen(app: AppState, car: CarState) {
    val min = rideMin(app, car)
    // 잘 맞는 순서대로 가운데 → 오른쪽 → 왼쪽 → 오른쪽 … 에 놓아, 처음부터 양옆에 카드가 보이게 한다
    val ranked = remember(app.visibleBots, min) { app.visibleBots.sortedBy { abs(it.startFor(min).minutes - min) } }
    val bots = remember(ranked) {
        ranked.foldIndexed(ArrayDeque<StoryBot>()) { i, q, b -> q.apply { if (i % 2 == 1) addLast(b) else addFirst(b) } }.toList()
    }
    val list = rememberLazyListState(initialFirstVisibleItemIndex = bots.indexOf(ranked.firstOrNull()).coerceAtLeast(0))
    val scope = rememberCoroutineScope()
    // 화면 가운데에 가장 가까운 카드가 강조 카드다
    val center by remember {
        derivedStateOf {
            val info = list.layoutInfo
            val mid = (info.viewportStartOffset + info.viewportEndOffset) / 2
            info.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - mid) }?.index ?: 0
        }
    }

    Column(Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            LazyRow(
                state = list,
                flingBehavior = rememberSnapFlingBehavior(list),
                contentPadding = PaddingValues(horizontal = (maxWidth - CARD_W) / 2),
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                itemsIndexed(bots, key = { _, b -> b.id }) { i, bot ->
                    BotCard(bot, min, i == center) {
                        if (i == center) AppStore.selectBot(bot.id) else scope.launch { list.animateScrollToItem(i) }
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(start = 32.dp, end = 107.dp, bottom = 19.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(destLabel(app, car), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("도착하면 이야기도 끝나요", fontSize = 17.sp, color = Momo.Sub)
            }
            PillButton("말로 새 이야기 봇 만들기", dark = false, leading = { MicIcon(Momo.Ink) }) { AppStore.open(Overlay.CREATE) }
            Spacer(Modifier.width(11.dp))
            PillButton("마켓 전체 보기") { AppStore.go(Screen.MARKET) }
        }
    }
}

@Composable
private fun BotCard(bot: StoryBot, min: Int, focused: Boolean, onClick: () -> Unit) {
    Box(Modifier.width(CARD_W).height(if (focused) 500.dp else 472.dp).clickable(onClick = onClick)) {
        Cover(bot, Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0.45f to Color.Transparent, 1f to Color.Black.copy(alpha = .9f)),
            ),
        )
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 25.dp, vertical = 27.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                bot.title, color = Color.White, fontSize = 33.sp, lineHeight = 41.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, maxLines = 2, style = KoBreak,
            )
            Spacer(Modifier.height(11.dp))
            Text(metaLine(bot, min), color = Color.White.copy(alpha = .8f), fontSize = 15.sp)
            Spacer(Modifier.height(21.dp))
            Text(
                bot.description, Modifier.fillMaxWidth(), color = Color.White, fontSize = 15.sp, lineHeight = 23.sp,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
        }
        // 양옆 카드는 흐리게
        if (!focused) Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = .45f)))
    }
}
