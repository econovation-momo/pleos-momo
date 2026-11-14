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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

/** S2 마켓: 경로 시간·인원·키즈 등급으로 거른 봇 카드 그리드 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarketScreen(app: AppState, car: CarState) {
    val route = car.remainSec?.let { s -> ROUTE_MINUTES.minBy { abs(it - s / 60) } }
    val picked = app.marketMinutes ?: route ?: ALL
    val bots = app.visibleBots.filter { picked == ALL || picked in it.minutes }

    Row(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(Modifier.width(240.dp).fillMaxHeight()) {
            Text("마켓", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text(
                listOf(if (picked == ALL) "모든 경로" else "${minLabel(picked)} 경로", "${car.riders}명", if (app.kids) "키즈" else "전체 등급").joinToString(" · "),
                fontSize = 14.sp, color = Momo.Sub,
            )
            Section("경로 시간")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (listOf(ALL) + ROUTE_MINUTES).forEach { m ->
                    val label = if (m == ALL) "전체" else minLabel(m) + if (m == route) " (지금 경로)" else ""
                    Pill(label, picked == m) { AppStore.update { it.copy(marketMinutes = m) } }
                }
            }
            Section("인원")
            // 역할보다 탑승자가 많아도 남는 사람은 응원단이 되므로 거르지 않고 기준만 보여 준다
            Pill("${car.riders}명 탑승 기준", true)
            Section("등급")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill("키즈만", app.kids) { if (!app.kids) AppStore.toggleKids() }
                Pill("전체 등급", !app.kids) { if (app.kids) AppStore.open(Overlay.KIDS_PIN) }
            }
            Spacer(Modifier.weight(1f))
            // 운전과 무관한 알림이라 정차 중에만(마켓 자체도 정차 중에만 열린다)
            if (app.following.isNotEmpty() && !car.driving) Panel(Modifier.fillMaxWidth()) {
                Text("🔔 새 봇", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("팔로우한 ${app.following.first()}님이 새 이야기 봇을 올렸어요", fontSize = 14.sp, color = Momo.Sub)
            }
        }

        LazyVerticalGrid(
            GridCells.Fixed(3), Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            items(bots, key = { it.id }) { BotCard(it, mine = it.id in app.myBotIds) }
            if (bots.isEmpty()) item(span = { GridItemSpan(3) }) {
                Text("이 경로 시간에 맞는 봇이 없어요. 다른 시간을 골라 보세요.", Modifier.padding(vertical = 32.dp), fontSize = 16.sp, color = Momo.Sub)
            }
            item { UploadCard { uploadOrCreate(app.myBots) } }
        }
    }
}

@Composable
private fun Section(label: String) {
    Spacer(Modifier.height(24.dp))
    Text(label, fontSize = 14.sp, color = Momo.Sub)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun BotCard(b: StoryBot, mine: Boolean) = Column(Modifier.clickable { AppStore.selectBot(b.id) }) {
    Box {
        Cover(b, Modifier.fillMaxWidth().aspectRatio(1.3f).clip(Round))
        Text(
            "▷ ${count(b.plays)}회", Modifier.padding(12.dp).background(Momo.Bg, Round).padding(horizontal = 9.dp, vertical = 3.dp),
            fontSize = 12.sp, fontWeight = FontWeight.Medium,
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(b.title, Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (mine) Badge("내 봇")
        Badge(b.rating.label, dark = true)
    }
    Text(b.oneLiner, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    Text("${b.creator} · ${b.playersLabel} · ${b.minsLabel}", fontSize = 13.sp, color = Momo.Sub)
}

/** 그리드 끝의 점선 카드 */
@Composable
private fun UploadCard(onClick: () -> Unit) = Column(Modifier.clickable(onClick = onClick)) {
    Box(
        Modifier.fillMaxWidth().aspectRatio(1.3f).drawBehind {
            drawRoundRect(
                Momo.Sub, cornerRadius = CornerRadius(16.dp.toPx()),
                style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))),
            )
        },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(Momo.Ink), contentAlignment = Alignment.Center) {
            Text("+", color = Momo.OnAccent, fontSize = 23.sp)
        }
    }
    Spacer(Modifier.height(8.dp))
    Text("+ 내 봇 올리기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    Text("말로 만든 봇을 마켓에 공개해요", fontSize = 13.sp, color = Momo.Sub)
}
