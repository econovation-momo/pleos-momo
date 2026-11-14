package app.momo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.momo.car.CarState
import app.momo.core.AppState
import app.momo.core.AppStore
import app.momo.core.Overlay
import app.momo.ui.Momo
import app.momo.ui.components.Gutter
import app.momo.ui.components.minLabel
import app.momo.ui.overlays.*
import kotlinx.coroutines.launch
import kotlin.math.abs

private val ROUTE_MINUTES = listOf(15, 45, 120)
private const val ALL = 0 // 경로 시간 칩 '전체'. app.marketMinutes가 null이면 아직 안 고른 것이라 내비 남은 시간을 따른다.

/** S2 마켓: Figma 21:122 포스터 캐러셀. 위 한 줄에 경로 시간·등급 필터, 가운데 포스터 카드, 행 끝은 '내 봇 올리기' 카드. */
@Composable
fun MarketScreen(app: AppState, car: CarState) {
    val route = car.remainSec?.let { s -> ROUTE_MINUTES.minBy { abs(it - s / 60) } }
    val picked = app.marketMinutes ?: route ?: ALL
    val bots = app.visibleBots.filter { picked == ALL || picked in it.minutes }

    Column(Modifier.fillMaxSize()) {
        // 위 한 줄: 제목과 필터 알약
        Row(Modifier.fillMaxWidth().padding(horizontal = Gutter, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("마켓", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (listOf(ALL) + ROUTE_MINUTES).forEach { m ->
                    val label = if (m == ALL) "전체" else minLabel(m) + if (m == route) " · 지금 경로" else ""
                    Pill(label, picked == m) { AppStore.update { it.copy(marketMinutes = m) } }
                }
            }
            Spacer(Modifier.width(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill("키즈만", app.kids) { if (!app.kids) AppStore.toggleKids() }
                Pill("전체 등급", !app.kids) { if (app.kids) AppStore.open(Overlay.KIDS_PIN) }
            }
            Spacer(Modifier.weight(1f))
            // 역할보다 탑승자가 많아도 남는 사람은 응원단이 되므로 거르지 않고 기준만 보여 준다
            Text("${car.riders}명 탑승 기준", fontSize = 15.sp, color = Momo.Sub)
        }

        // 필터가 바뀌면 캐러셀을 처음(둘째 카드가 가운데) 상태로 다시 그린다
        key(picked, app.kids) {
            val n = bots.size + 1 // 끝의 '내 봇 올리기' 카드까지
            val list = rememberLazyListState(initialFirstVisibleItemIndex = if (n > 2) 1 else 0)
            val center by centerIndex(list)
            val scope = rememberCoroutineScope()
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                LazyRow(
                    state = list, flingBehavior = rememberSnapFlingBehavior(list),
                    contentPadding = PaddingValues(horizontal = (maxWidth - PosterW) / 2),
                    horizontalArrangement = Arrangement.spacedBy(15.dp), verticalAlignment = Alignment.CenterVertically,
                ) {
                    items(n) { i ->
                        val focused = i == center
                        val size = Modifier.width(if (focused) 330.dp else PosterW).height(if (focused) 500.dp else PosterH)
                        val pick = { if (!focused) scope.launch { list.animateScrollToItem(i) } }
                        val b = bots.getOrNull(i)
                        if (b == null) PosterCard(
                            null, "내 봇 올리기", listOf("말로 만들기", "검수 후 공개"), "말로 만든 봇을 마켓에 올리면, 다른 가족이 이야기한 시간만큼 포인트가 쌓여요.",
                            size, focused, onClick = { if (focused) uploadOrCreate(app.myBots) else pick() },
                            extra = {
                                Box(Modifier.size(64.dp).background(Color.White, CircleShape), Alignment.Center) { Text("+", fontSize = 32.sp, color = Momo.Ink) }
                                Spacer(Modifier.height(20.dp))
                            },
                        )
                        else PosterCard(
                            b, b.title, listOf(b.minsLabel, b.playersLabel, b.rating.label, b.creator), b.oneLiner, size, focused,
                            onClick = { if (focused) AppStore.selectBot(b.id) else pick() },
                            top = { ImageTag("▷ ${count(b.plays)}회"); if (b.id in app.myBotIds) ImageTag("내 봇", dark = true) },
                        )
                    }
                }
                if (bots.isEmpty()) Text(
                    "이 경로 시간에 맞는 봇이 없어요", Modifier.align(Alignment.TopCenter).padding(top = 4.dp), fontSize = 16.sp, color = Momo.Sub,
                )
            }
        }

        // 아래 한 줄: 몇 개인지와 팔로우 새 봇 알림(정차 중에만, 마켓 자체도 정차 중에만 열린다)
        Row(Modifier.fillMaxWidth().padding(start = Gutter, end = 110.dp, top = 6.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                (if (picked == ALL) "모든 경로" else "${minLabel(picked)} 경로에 맞는 이야기") + " ${bots.size}개",
                fontSize = 22.sp, fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.weight(1f))
            if (app.following.isNotEmpty() && !car.driving) Text(
                "새 봇 · 팔로우한 ${app.following.first()}님이 새 이야기 봇을 올렸어요",
                Modifier.background(Momo.Ink.copy(alpha = 0.06f), RoundedCornerShape(50)).padding(horizontal = 18.dp, vertical = 10.dp),
                fontSize = 15.sp, fontWeight = FontWeight.Bold,
            )
        }
    }
}

/** 화면 가운데에 가장 가까운 카드의 위치 */
@Composable
fun centerIndex(list: LazyListState) = remember(list) {
    derivedStateOf {
        val info = list.layoutInfo
        val mid = (info.viewportStartOffset + info.viewportEndOffset) / 2
        info.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - mid) }?.index ?: 0
    }
}
