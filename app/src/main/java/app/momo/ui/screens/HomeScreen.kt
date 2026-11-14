package app.momo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
import kotlin.math.abs

/** S1 홈(구도 A, 21:122): 포스터 캐러셀. 남은 시간에 잘 맞는 봇이 가운데에 온다. */
@Composable
fun HomeScreen(app: AppState, car: CarState) {
    val min = rideMin(app, car)
    // 잘 맞는 순서대로 가운데 → 오른쪽 → 왼쪽 → 오른쪽 … 에 놓아, 처음부터 양옆에 카드가 보이게 한다
    val ranked = remember(app.visibleBots, min) { app.visibleBots.sortedBy { abs(it.startFor(min).minutes - min) } }
    val bots = remember(ranked) {
        ranked.foldIndexed(ArrayDeque<StoryBot>()) { i, q, b -> q.apply { if (i % 2 == 1) addLast(b) else addFirst(b) } }.toList()
    }

    Column(Modifier.fillMaxSize()) {
        PosterRow(
            bots, key = { it.id }, modifier = Modifier.weight(1f).fillMaxWidth(),
            initialIndex = bots.indexOf(ranked.firstOrNull()).coerceAtLeast(0),
            onOpen = { AppStore.selectBot(it.id) },
        ) { bot, focused -> PosterCard(bot, meta = botMeta(bot, min), focused = focused) }

        // 하단 띠: 시안 톤을 깨지 않게 글자만 가볍게 둔다
        Row(Modifier.fillMaxWidth().padding(start = Gutter, end = 120.dp, bottom = 22.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(destLabel(app, car), Modifier.weight(1f), fontSize = 17.sp, color = Momo.Sub)
            TextLink("말로 새 이야기 봇 만들기") { AppStore.open(Overlay.CREATE) }
            Spacer(Modifier.width(28.dp))
            TextLink("마켓 전체 보기") { AppStore.go(Screen.MARKET) }
        }
    }
}

@Composable
private fun TextLink(text: String, onClick: () -> Unit) =
    Text(text, Modifier.clickable(onClick = onClick).padding(vertical = 10.dp), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Momo.Ink)
