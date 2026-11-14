package app.momo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.momo.car.CarState
import app.momo.core.AppState
import app.momo.core.AppStore
import app.momo.core.BotStatus
import app.momo.core.Overlay
import app.momo.core.SeatPos
import app.momo.core.StoryBot
import app.momo.ui.Momo
import app.momo.ui.overlays.*

// ponytail: 나이·적립·이용권은 시연용 고정값이다. 탑승자 편집과 포인트 연동이 생기면 AppState로 옮긴다.
private val AGES = mapOf("지우" to 7, "지호" to 5)
private const val ENDING_TOTAL = 12

/** S7 마이페이지: 가족 탑승자, 창작 적립, 내가 만든 봇, 엔딩 카드 보관함 */
@Composable
fun MyScreen(app: AppState, car: CarState) {
    Row(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Family(app, car, Modifier.width(304.dp).fillMaxHeight())
        MyBots(app, Modifier.weight(1f).fillMaxHeight())
        Endings(app, Modifier.width(336.dp).fillMaxHeight())
    }
}

@Composable
private fun Family(app: AppState, car: CarState, modifier: Modifier) = Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Column {
        Text("마이페이지", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("우리 가족 · 탑승자 ${car.riders}명", fontSize = 14.sp, color = Momo.Sub)
    }
    Panel(Modifier.fillMaxWidth()) {
        Text("우리 가족 탑승자", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text("좌석별 이름·나이로 역할을 나눠요", fontSize = 12.sp, color = Momo.Sub)
        SeatPos.entries.forEach { seat ->
            val name = app.names[seat] ?: return@forEach
            Row(Modifier.padding(top = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(Momo.Ink), contentAlignment = Alignment.Center) {
                    Text(name.take(1), color = Momo.OnAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(11.dp))
                Column {
                    Text(name + (AGES[name]?.let { " · ${it}세" } ?: ""), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text(seat.label, fontSize = 12.sp, color = Momo.Sub)
                }
            }
        }
    }
    Spacer(Modifier.weight(1f))
    Panel(Modifier.fillMaxWidth()) {
        Text("창작 적립", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.Bottom) {
            Text("12,400", fontSize = 36.sp, fontWeight = FontWeight.Light)
            Text(" P · 이번 달", Modifier.padding(bottom = 7.dp), fontSize = 14.sp, color = Momo.Sub)
        }
        Text("내 봇이 이용된 시간만큼 블루멤버스 포인트로 적립돼요", fontSize = 12.sp)
        Text("블루멤버스 제휴 전제 · 1P = 1원", fontSize = 12.sp, color = Momo.Sub)
    }
    Panel(Modifier.fillMaxWidth()) {
        Text("이용권", fontSize = 13.sp, color = Momo.Sub)
        Text("키즈 무제한 · 월 4,900원", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("팔로우 중인 창작자 ${app.following.size}명", fontSize = 13.sp, color = Momo.Sub)
    }
}

@Composable
private fun MyBots(app: AppState, modifier: Modifier) = Panel(modifier) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("내가 만든 이야기 봇 · ${app.myBots.size}", Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Pill("+ 말로 새로 만들기", false, off = Momo.Bg) { openCreate() }
    }
    Spacer(Modifier.height(8.dp))
    LazyColumn(Modifier.weight(1f)) {
        items(app.myBots, key = { it.id }) { b ->
            HorizontalDivider(color = Momo.Line)
            MyBotRow(b)
        }
    }
    HorizontalDivider(color = Momo.Line)
    Text("🚗 가족 차에 공유 · ✎ 소개·공개 범위·등급 수정", Modifier.padding(top = 9.dp), fontSize = 12.sp, color = Momo.Sub)
}

@Composable
private fun MyBotRow(b: StoryBot) = Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
    Cover(b, Modifier.size(48.dp).clip(RoundedCornerShape(11.dp)))
    Spacer(Modifier.width(12.dp))
    Column(Modifier.weight(1f)) {
        Text(b.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            listOf(if (b.plays > 0) "이용 ${count(b.plays)}회" else "방금 만듦", b.minsLabel, b.playersLabel, b.rating.label).joinToString(" · "),
            fontSize = 13.sp, color = Momo.Sub, maxLines = 1,
        )
    }
    Spacer(Modifier.width(8.dp))
    Badge(b.status.label, dark = b.status == BotStatus.PUBLIC)
    Spacer(Modifier.width(8.dp))
    IconBox("🚗", bg = Momo.Bg) { openShare(b.id) }
    Spacer(Modifier.width(5.dp))
    IconBox("✎", bg = Momo.Bg) { goUpload(b.id) }
    if (b.status == BotStatus.PRIVATE) {
        Spacer(Modifier.width(5.dp))
        Pill("마켓에 올리기", true) { goUpload(b.id) }
    }
}

@Composable
private fun Endings(app: AppState, modifier: Modifier) = Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Panel(Modifier.fillMaxWidth().weight(1f)) {
        Row {
            Text("엔딩 카드 보관함", Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("${app.endingCards.size} / $ENDING_TOTAL", fontSize = 14.sp, color = Momo.Sub)
        }
        Spacer(Modifier.height(9.dp))
        LazyVerticalGrid(GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(9.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(app.endingCards) { c ->
                Column {
                    Cover(app.bots.firstOrNull { it.id == c.botId }, Modifier.fillMaxWidth().aspectRatio(1.1f).clip(RoundedCornerShape(12.dp)))
                    Text(c.title, Modifier.padding(top = 5.dp), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${c.grade} · ${c.date}", fontSize = 12.sp, color = Momo.Sub)
                }
            }
            item {
                Column {
                    Box(Modifier.fillMaxWidth().aspectRatio(1.1f).clip(RoundedCornerShape(12.dp)).background(Momo.Line), contentAlignment = Alignment.Center) {
                        Text("?", fontSize = 32.sp, color = Momo.Sub)
                    }
                    Text("아직 못 만난 엔딩", Modifier.padding(top = 5.dp), fontSize = 13.sp, color = Momo.Sub)
                    Text("${ENDING_TOTAL - app.endingCards.size}개 남음", fontSize = 12.sp, color = Momo.Sub)
                }
            }
        }
    }
    // 키즈 모드 보호: 끌 때만 부모 PIN(K1)
    Row(
        Modifier.fillMaxWidth().clip(Round).background(Momo.Ink)
            .clickable { if (app.kids) AppStore.open(Overlay.KIDS_PIN) else AppStore.toggleKids() }.padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(if (app.kids) "키즈 모드 켜짐" else "키즈 모드 꺼짐", fontSize = 13.sp, color = Momo.Line)
            Text("부모 PIN 설정됨", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Momo.OnAccent)
            Text(if (app.kids) "끌 때만 PIN을 물어요" else "누르면 다시 켜요", fontSize = 12.sp, color = Momo.Line)
        }
        Box(Modifier.size(48.dp).clip(CircleShape).background(Momo.Bg), contentAlignment = Alignment.Center) {
            Text(if (app.kids) "🔒" else "🔓", fontSize = 19.sp)
        }
    }
}
