package app.momo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.momo.car.CarState
import app.momo.core.AppState
import app.momo.core.AppStore
import app.momo.core.Overlay
import app.momo.core.StoryBot
import app.momo.ui.Momo
import app.momo.ui.overlays.*
import kotlin.math.abs

private val ROUTE_MINUTES = listOf(15, 45, 120)
private const val ALL = 0 // 경로 시간 칩 '전체'. app.marketMinutes가 null이면 아직 안 고른 것이라 내비 남은 시간을 따른다.
const val CARD_RATIO = 0.68f // Figma 21:122 동화 선택 카드 비율

/** S2 마켓: 경로 시간·키즈 등급으로 거른 세로 표지 카드 그리드 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarketScreen(app: AppState, car: CarState) {
    val route = car.remainSec?.let { s -> ROUTE_MINUTES.minBy { abs(it - s / 60) } }
    val picked = app.marketMinutes ?: route ?: ALL
    val bots = app.visibleBots.filter { picked == ALL || picked in it.minutes }

    Row(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        Column(Modifier.width(232.dp).fillMaxHeight()) {
            Text("마켓", fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Meta(listOf(if (picked == ALL) "모든 경로" else "${minLabel(picked)} 경로", "${car.riders}명", if (app.kids) "키즈" else "전체 등급"), size = 14.sp)
            Section("경로 시간")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (listOf(ALL) + ROUTE_MINUTES).forEach { m ->
                    val label = if (m == ALL) "전체" else minLabel(m) + if (m == route) " · 지금 경로" else ""
                    Pill(label, picked == m) { AppStore.update { it.copy(marketMinutes = m) } }
                }
            }
            Section("인원")
            // 역할보다 탑승자가 많아도 남는 사람은 응원단이 되므로 거르지 않고 기준만 보여 준다
            Text("${car.riders}명 탑승 기준", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("남는 사람은 응원단으로 함께해요", fontSize = 13.sp, color = Momo.Sub)
            Section("등급")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill("키즈만", app.kids) { if (!app.kids) AppStore.toggleKids() }
                Pill("전체 등급", !app.kids) { if (app.kids) AppStore.open(Overlay.KIDS_PIN) }
            }
            Spacer(Modifier.weight(1f))
            // 운전과 무관한 알림이라 정차 중에만(마켓 자체도 정차 중에만 열린다)
            if (app.following.isNotEmpty() && !car.driving) {
                Meta(listOf("새 봇 알림", "정차 중에만"), size = 12.sp)
                Text("팔로우한 ${app.following.first()}님이\n새 이야기 봇을 올렸어요", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyVerticalGrid(
            GridCells.Fixed(4), Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(bots, key = { it.id }) { BotCard(it, mine = it.id in app.myBotIds) }
            if (bots.isEmpty()) item(span = { GridItemSpan(4) }) {
                Text("이 경로 시간에 맞는 봇이 없어요. 다른 시간을 골라 보세요.", Modifier.padding(vertical = 40.dp), fontSize = 18.sp, color = Momo.Sub)
            }
            item { UploadCard { uploadOrCreate(app.myBots) } }
        }
    }
}

@Composable
private fun Section(label: String) {
    Spacer(Modifier.height(28.dp))
    Text(label, fontSize = 13.sp, color = Momo.Sub)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun BotCard(b: StoryBot, mine: Boolean) = CoverCard(
    b, Modifier.fillMaxWidth().aspectRatio(CARD_RATIO), onClick = { AppStore.selectBot(b.id) },
    top = { ImageTag("▷ ${count(b.plays)}회"); if (mine) ImageTag("내 봇", dark = true) },
) {
    CardTitle(b.title)
    Spacer(Modifier.height(4.dp))
    Meta(listOf(b.minsLabel, b.playersLabel, b.rating.label), color = OnImageSub, size = 12.sp)
    Spacer(Modifier.height(8.dp))
    CardBody(b.oneLiner)
    Spacer(Modifier.height(6.dp))
    Text("만든 사람 ${b.creator}", color = OnImageSub, fontSize = 12.sp)
}

/** 그리드 끝: 검정 카드에 흰 원 + */
@Composable
private fun UploadCard(onClick: () -> Unit) = Column(
    Modifier.fillMaxWidth().aspectRatio(CARD_RATIO).clip(Round).background(Momo.Ink).clickable(onClick = onClick).padding(16.dp),
) {
    Spacer(Modifier.weight(1f))
    Box(Modifier.size(52.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
        Text("+", fontSize = 28.sp, color = Momo.Ink)
    }
    Spacer(Modifier.height(16.dp))
    CardTitle("내 봇 올리기")
    Spacer(Modifier.height(4.dp))
    CardBody("말로 만든 봇을 마켓에 공개해요. 이용된 시간만큼 포인트가 쌓여요.")
}
