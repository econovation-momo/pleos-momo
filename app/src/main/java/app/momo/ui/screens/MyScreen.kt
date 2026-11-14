package app.momo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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

/** S7 마이페이지: 가족 탑승자·창작 적립(왼쪽), 내가 만든 봇·엔딩 카드 보관함(오른쪽) */
@Composable
fun MyScreen(app: AppState, car: CarState) {
    Row(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(36.dp)) {
        Family(app, car, Modifier.width(260.dp).fillMaxHeight())
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("내가 만든 이야기 봇", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Meta(listOf("${app.myBots.size}개", "가족 공유 · 수정 · 마켓에 올리기"), size = 14.sp, modifier = Modifier.weight(1f))
                PillButton("말로 새로 만들기", icon = "+", height = 44.dp) { openCreate() }
            }
            Spacer(Modifier.height(14.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(app.myBots, key = { it.id }) { MyBotCard(it) }
            }
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("엔딩 카드 보관함", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Meta(listOf("${app.endingCards.size} / $ENDING_TOTAL", "${ENDING_TOTAL - app.endingCards.size}개 남음"), size = 14.sp)
            }
            Spacer(Modifier.height(14.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(app.endingCards) { c ->
                    CoverCard(app.bots.firstOrNull { it.id == c.botId }, Modifier.height(210.dp).aspectRatio(CARD_RATIO)) {
                        CardTitle(c.title, size = 15.sp)
                        Spacer(Modifier.height(4.dp))
                        Meta(listOf(c.grade, c.date), color = OnImageSub, size = 12.sp)
                    }
                }
                item {
                    Column(
                        Modifier.height(210.dp).aspectRatio(CARD_RATIO).clip(Round).background(Momo.Surface).padding(16.dp),
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        Text("?", Modifier.alpha(DIM), fontSize = 40.sp, fontWeight = FontWeight.Bold)
                        Text("아직 못 만난 엔딩", Modifier.alpha(DIM), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun Family(app: AppState, car: CarState, modifier: Modifier) = Column(modifier) {
    Text("마이페이지", fontSize = 40.sp, fontWeight = FontWeight.Bold)
    Meta(listOf("우리 가족", "탑승자 ${car.riders}명"), size = 14.sp)
    Spacer(Modifier.height(24.dp))
    Text("우리 가족 탑승자", fontSize = 13.sp, color = Momo.Sub)
    SeatPos.entries.forEach { seat ->
        val name = app.names[seat] ?: return@forEach
        Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(Momo.Ink), contentAlignment = Alignment.Center) {
                Text(name.take(1), color = Momo.OnAccent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(name + (AGES[name]?.let { " · ${it}세" } ?: ""), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(seat.label, fontSize = 13.sp, color = Momo.Sub)
            }
        }
    }
    Spacer(Modifier.weight(1f))
    Meta(listOf("창작 적립", "이번 달"), size = 13.sp)
    Row(verticalAlignment = Alignment.Bottom) {
        Text("12,400", fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Text(" P", Modifier.padding(bottom = 7.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
    Text("내 봇이 이용된 시간만큼 블루멤버스 포인트로 쌓여요 (제휴 전제)", fontSize = 13.sp, color = Momo.Sub)
    Spacer(Modifier.height(20.dp))
    Meta(listOf("이용권", "팔로우 중인 창작자 ${app.following.size}명"), size = 13.sp)
    Text("키즈 무제한 · 월 4,900원", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(20.dp))
    // 키즈 모드 보호: 끌 때만 부모 PIN(K1)
    Meta(listOf("키즈 모드 ${if (app.kids) "켜짐" else "꺼짐"}", "부모 PIN 설정됨"), size = 13.sp)
    Spacer(Modifier.height(8.dp))
    if (app.kids) PillButton("키즈 모드 끄기 (PIN)", height = 44.dp) { AppStore.open(Overlay.KIDS_PIN) }
    else PillButton("키즈 모드 다시 켜기", light = true, height = 44.dp) { AppStore.toggleKids() }
}

@Composable
private fun MyBotCard(b: StoryBot) = CoverCard(
    b, Modifier.height(380.dp).aspectRatio(CARD_RATIO),
    top = { ImageTag(b.status.label, dark = b.status == BotStatus.PUBLIC) },
) {
    CardTitle(b.title, size = 21.sp)
    Spacer(Modifier.height(4.dp))
    Meta(listOf(if (b.plays > 0) "이용 ${count(b.plays)}회" else "방금 만듦", b.minsLabel, b.playersLabel), color = OnImageSub, size = 12.sp)
    Spacer(Modifier.height(12.dp))
    if (b.status == BotStatus.PRIVATE) {
        PillButton("마켓에 올리기", Modifier.fillMaxWidth(), icon = "↑", onImage = true, height = 44.dp) { goUpload(b.id) }
        Spacer(Modifier.height(8.dp))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PillButton("가족 공유", Modifier.weight(1f), onImage = true, height = 40.dp) { openShare(b.id) }
        PillButton("수정", Modifier.weight(1f), onImage = true, height = 40.dp) { goUpload(b.id) }
    }
}
