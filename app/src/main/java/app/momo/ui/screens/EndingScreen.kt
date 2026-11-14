package app.momo.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.momo.Graph
import app.momo.car.CarState
import app.momo.core.AppState
import app.momo.core.AppStore
import app.momo.core.Screen
import app.momo.ui.Momo
import app.momo.ui.components.*

/**
 * S6 엔딩(구도 B, 21:7): 그 봇의 장면 그림을 꽉 채우고 왼쪽에 오늘의 엔딩을 얹는다.
 * 오른쪽에는 엔딩 카드를 포스터 카드(구도 A) 한 장으로 두고, '부모 폰으로 공유'를 누르면 그 자리에 QR 유리 카드가 뜬다.
 */
@Composable
fun EndingScreen(app: AppState, car: CarState) {
    val play by Graph.engine.state.collectAsStateWithLifecycle()
    val card = play.ending ?: app.endingCards.firstOrNull() ?: return
    val bot = app.bots.firstOrNull { it.id == card.botId } ?: app.bot
    val leave = { to: Screen -> Graph.engine.stop(); AppStore.go(to) }
    // 장면 수는 방금 끝난 이야기에서만 정확히 안다. 보관함 샘플 카드에는 숫자를 붙이지 않는다.
    val scenes = if (play.ending != null) play.sceneIndex + 1 else null
    var qr by remember { mutableStateOf(false) }

    HeroScreen(
        bot,
        side = {
            if (qr) GlassCard(Modifier.width(330.dp)) {
                QrPlaceholder(card.title, Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(14.dp))
                Text("부모 폰으로 공유", Modifier.fillMaxWidth(), color = Momo.Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("QR을 찍으면 엔딩 카드와 오늘의 장면 그림이 열려요", Modifier.fillMaxWidth(), color = Momo.Sub, fontSize = 15.sp, textAlign = TextAlign.Center, style = KoBreak)
            } else PosterCard(
                bot, title = card.title, meta = listOfNotNull(card.grade, scenes?.let { "${it}장면" }, "${card.minutes}분"), body = card.summary,
                image = bot.scenes.lastOrNull() ?: bot.cover, badge = "No. %02d".format(app.endingCards.size),
            )
        },
    ) {
        HeroHeader(
            kicker = "오늘의 엔딩 · ${card.grade}",
            title = card.title,
            meta = listOf(card.destination, "${card.minutes}분", card.players.joinToString(" · ")),
            body = card.summary,
        )
        Spacer(Modifier.height(30.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            PillButton("보관함에서 보기", height = 64.dp, fontSize = 22.sp) { leave(Screen.MY) }
            Spacer(Modifier.width(12.dp))
            GhostButton("홈으로") { leave(Screen.HOME) }
            Spacer(Modifier.width(10.dp))
            GhostButton(if (qr) "엔딩 카드 보기" else "부모 폰으로 공유") { qr = !qr }
        }
    }
}

/** QR 자리표시. 실제 링크 생성은 서버가 생긴 뒤에 붙인다. */
@Composable
private fun QrPlaceholder(seed: String, modifier: Modifier) = Canvas(modifier.size(200.dp).background(Color.White, RoundedCornerShape(12.dp)).padding(12.dp)) {
    val n = 21
    val cell = size.width / n
    val rnd = java.util.Random(seed.hashCode().toLong())
    fun finder(x: Int, y: Int) {
        drawRect(Color.Black, Offset(x * cell, y * cell), Size(7 * cell, 7 * cell))
        drawRect(Color.White, Offset((x + 1) * cell, (y + 1) * cell), Size(5 * cell, 5 * cell))
        drawRect(Color.Black, Offset((x + 2) * cell, (y + 2) * cell), Size(3 * cell, 3 * cell))
    }
    for (y in 0 until n) for (x in 0 until n) {
        val inFinder = (x < 8 && y < 8) || (x > n - 9 && y < 8) || (x < 8 && y > n - 9)
        if (!inFinder && rnd.nextBoolean()) drawRect(Color.Black, Offset(x * cell, y * cell), Size(cell, cell))
    }
    finder(0, 0); finder(n - 7, 0); finder(0, n - 7)
}
