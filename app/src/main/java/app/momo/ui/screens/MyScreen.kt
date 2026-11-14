package app.momo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
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
import app.momo.ui.components.*
import app.momo.ui.overlays.*

// ponytail: 나이·적립·이용권은 시연용 고정값이다. 탑승자 편집과 포인트 연동이 생기면 AppState로 옮긴다.
private val AGES = mapOf("지우" to 7, "지호" to 5)
private const val ENDING_TOTAL = 12
private val CardW = 190.dp
private val CardH = 280.dp

/** S7 마이페이지: 위는 내 봇 히어로(21:7)와 오른쪽 유리 카드 요약, 아래는 내 봇·엔딩 카드 포스터 행(21:122) */
@Composable
fun MyScreen(app: AppState, car: CarState) {
    // 히어로에 띄울 봇: 처음에는 가장 최근에 만든 봇, 아래 카드를 누르면 그 봇
    var heroId by remember { mutableStateOf<String?>(null) }
    val hero = app.myBots.firstOrNull { it.id == heroId } ?: app.myBots.firstOrNull()

    Column(Modifier.fillMaxSize().padding(top = 12.dp)) {
        if (hero != null) Hero(hero, Modifier.fillMaxWidth().padding(horizontal = Gutter).height(400.dp)) {
            HeroText(hero, Modifier.align(Alignment.CenterStart).padding(start = 40.dp).widthIn(max = 560.dp))
            Summary(app, car, Modifier.align(Alignment.CenterEnd).padding(end = 24.dp).width(330.dp))
        }

        Row(Modifier.padding(start = Gutter, end = Gutter, top = 16.dp, bottom = 10.dp), verticalAlignment = Alignment.Bottom) {
            Text("내 봇 · 엔딩 카드 보관함", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(12.dp))
            MetaLine(listOf("내 봇 ${app.myBots.size}개", "엔딩 ${app.endingCards.size} / $ENDING_TOTAL"), fontSize = 14.sp)
        }
        LazyRow(contentPadding = PaddingValues(start = Gutter, end = 110.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                PosterCard(
                    null, "말로 새로 만들기", emptyList(), null, Modifier.size(CardW, CardH), titleSize = 18, onClick = ::openCreate,
                    extra = {
                        Box(Modifier.size(48.dp).background(Color.White, CircleShape), Alignment.Center) { Text("+", fontSize = 26.sp, color = Momo.Ink) }
                        Spacer(Modifier.height(14.dp))
                    },
                )
            }
            items(app.myBots, key = { it.id }) { b ->
                PosterCard(
                    b, b.title, listOf(b.status.label), null, Modifier.size(CardW, CardH), focused = b.id == hero?.id, titleSize = 18,
                    onClick = { heroId = b.id }, top = { ImageTag("내 봇", dark = true) },
                )
            }
            items(app.endingCards) { c ->
                PosterCard(
                    app.bots.firstOrNull { it.id == c.botId }, c.title, listOf(c.grade, c.date), null, Modifier.size(CardW, CardH), titleSize = 18,
                    top = { ImageTag("엔딩 카드") },
                )
            }
            item {
                // 미발견 칸: 흐린 그라데이션 카드
                PosterCard(
                    null, "?", listOf("${ENDING_TOTAL - app.endingCards.size}개 남음"), null,
                    Modifier.size(CardW, CardH).alpha(0.5f), titleSize = 34,
                )
            }
        }
    }
}

/** 히어로 왼쪽: 머리말, 상태, 큰 제목, 메타, 두 줄 소개, 알약 버튼 */
@Composable
private fun HeroText(b: StoryBot, modifier: Modifier) = Column(modifier) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("내가 만든 이야기 봇", color = OnImageSub, fontSize = 17.sp)
        Spacer(Modifier.width(10.dp))
        ImageTag(b.status.label, dark = b.status == BotStatus.PUBLIC)
    }
    Spacer(Modifier.height(8.dp))
    Text(b.title, color = Color.White, fontSize = 40.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold, maxLines = 2, style = KoBreak)
    Spacer(Modifier.height(8.dp))
    MetaLine(listOf(if (b.plays > 0) "이용 ${count(b.plays)}회" else "방금 만듦", b.minsLabel, b.playersLabel, b.rating.label), OnImageSub)
    Spacer(Modifier.height(14.dp))
    Text(b.oneLiner, color = Color.White, fontSize = 17.sp, lineHeight = 25.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    Spacer(Modifier.height(24.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        if (b.status == BotStatus.PRIVATE) {
            PillButton("마켓에 올리기", height = 56.dp, fontSize = 18.sp, icon = { Text("↑", color = it, fontSize = 16.sp, fontWeight = FontWeight.Bold) }) { goUpload(b.id) }
            GhostButton("가족 공유") { openShare(b.id) }
        } else PillButton("가족 공유", height = 56.dp, fontSize = 18.sp, icon = { Text("↗", color = it, fontSize = 16.sp, fontWeight = FontWeight.Bold) }) { openShare(b.id) }
        GhostButton("수정") { goUpload(b.id) }
    }
}

/** 히어로 오른쪽 유리 카드: 가족 탑승자, 창작 적립, 이용권, 키즈 모드 PIN */
@Composable
private fun Summary(app: AppState, car: CarState, modifier: Modifier) = GlassCard(modifier) {
    MetaLine(listOf("우리 가족", "탑승자 ${car.riders}명"), fontSize = 13.sp)
    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SeatPos.entries.forEach { seat ->
            val name = app.names[seat] ?: return@forEach
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Avatar(name, active = car.seats[seat]?.occupied == true, size = 44.dp)
                Text(name + (AGES[name]?.let { " ${it}세" } ?: ""), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    MetaLine(listOf("창작 적립", "이번 달"), fontSize = 13.sp)
    Row(verticalAlignment = Alignment.Bottom) {
        Text("12,400", fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Text(" P", Modifier.padding(bottom = 6.dp), fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
    Text("블루멤버스 포인트 · 제휴 전제", fontSize = 12.sp, color = Momo.Sub)
    Spacer(Modifier.height(12.dp))
    MetaLine(listOf("이용권", "팔로우 ${app.following.size}명"), fontSize = 13.sp)
    Text("키즈 무제한 · 월 4,900원", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(14.dp))
    // 키즈 모드 보호: 끌 때만 부모 PIN(K1)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(if (app.kids) "키즈 모드 켜짐" else "키즈 모드 꺼짐", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("끌 때만 부모 PIN을 물어요", fontSize = 12.sp, color = Momo.Sub)
        }
        if (app.kids) PillButton("끄기", onImage = false, icon = null, height = 40.dp, fontSize = 14.sp) { AppStore.open(Overlay.KIDS_PIN) }
        else GhostButton("다시 켜기", onImage = false, fontSize = 14.sp) { AppStore.toggleKids() }
    }
}
