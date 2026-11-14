package app.momo.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

/** S6 엔딩 카드: 엔딩 그림을 꽉 채운 히어로 위에 오늘의 엔딩을 얹고, 그림책·공유는 유리 카드에 담는다. */
@Composable
fun EndingScreen(app: AppState, car: CarState) {
    val play by Graph.engine.state.collectAsStateWithLifecycle()
    val card = play.ending ?: app.endingCards.firstOrNull() ?: return
    val bot = app.bots.firstOrNull { it.id == card.botId } ?: app.bot
    val leave = { to: Screen -> Graph.engine.stop(); AppStore.go(to) }
    val scenes = if (play.ending != null) play.sceneIndex + 1 else card.minutes / 2
    // 엔진이 들고 있는 배역(역할 포함). 엔진이 비었으면 엔딩 카드의 이름만 쓴다.
    val cast = play.players.filter { it.role != null }.map { it.name to it.role!! }.ifEmpty { card.players.map { it to "" } }
    val white = Color.White

    Hero(
        bot, Modifier.fillMaxSize().padding(start = Gutter, end = Gutter, bottom = Gutter).clip(RoundedCornerShape(4.dp)),
        image = bot.scenes.firstOrNull() ?: bot.cover, // 가로로 긴 장면 그림을 히어로로
    ) {
        Column(Modifier.fillMaxHeight().fillMaxWidth(.56f).padding(start = 48.dp), verticalArrangement = Arrangement.Center) {
            Text("도착 · ${card.destination}", color = white, fontSize = 22.sp)
            Spacer(Modifier.height(10.dp))
            Text(card.title, color = white, fontSize = 40.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold, maxLines = 2, style = KoBreak)
            Spacer(Modifier.height(4.dp))
            Text("이야기도 함께 끝났어요", color = white, fontSize = 21.sp)
            Spacer(Modifier.height(18.dp))
            MetaLine(listOf("${card.grade} 엔딩", "${scenes}장면", "${card.minutes}분", card.date), white.copy(alpha = .75f), 16.sp)
            Spacer(Modifier.height(22.dp))
            Text(
                card.summary, color = white, fontSize = 19.sp, lineHeight = 29.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
                style = KoBreak,
            )
            Spacer(Modifier.height(20.dp))
            cast.forEach { (name, role) ->
                Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(name, active = true, size = 36.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(name, color = white, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    if (role.isNotEmpty()) Text("  $role", color = white.copy(alpha = .75f), fontSize = 17.sp)
                }
            }
            Spacer(Modifier.height(30.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                PillButton("보관함에서 보기", height = 64.dp, fontSize = 22.sp) { leave(Screen.MY) }
                Spacer(Modifier.width(14.dp))
                GhostButton("홈으로") { leave(Screen.HOME) }
            }
        }

        Column(
            Modifier.align(Alignment.CenterEnd).padding(end = 32.dp).width(380.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            GlassCard(Modifier.fillMaxWidth()) {
                Text("오늘의 그림책 · ${bot.scenes.size.coerceAtLeast(1)}장", color = Momo.Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                bot.scenes.ifEmpty { listOfNotNull(bot.cover) }.forEachIndexed { i, img ->
                    Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Cover(bot, Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)), image = img)
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(bot.chapters.getOrNull(i * 2) ?: bot.title, color = Momo.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("장면 ${i * 2 + 1}", color = Momo.Sub, fontSize = 15.sp)
                        }
                    }
                }
            }
            GlassCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("부모 폰으로 공유", color = Momo.Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("QR을 찍으면 엔딩 카드와 그림책이 열려요", color = Momo.Sub, fontSize = 15.sp, style = KoBreak)
                    }
                    Spacer(Modifier.width(12.dp))
                    QrPlaceholder(card.title)
                }
            }
        }
    }
}

/** QR 자리표시. 실제 링크 생성은 서버가 생긴 뒤에 붙인다. */
@Composable
private fun QrPlaceholder(seed: String) = Canvas(Modifier.size(104.dp).background(Color.White, RoundedCornerShape(10.dp)).padding(8.dp)) {
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
