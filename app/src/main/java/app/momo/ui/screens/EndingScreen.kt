package app.momo.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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

/** S6 엔딩 카드: 도착하면 이야기도 끝나고, 오늘의 엔딩과 그림책을 보관함·부모 폰으로 넘긴다. */
@Composable
fun EndingScreen(app: AppState, car: CarState) {
    val play by Graph.engine.state.collectAsStateWithLifecycle()
    val card = play.ending ?: app.endingCards.firstOrNull() ?: return
    val bot = app.bots.firstOrNull { it.id == card.botId } ?: app.bot
    val leave = { to: Screen -> Graph.engine.stop(); AppStore.go(to) }

    Row(Modifier.fillMaxSize().padding(start = 32.dp, end = 24.dp, bottom = 24.dp, top = 8.dp), horizontalArrangement = Arrangement.spacedBy(27.dp)) {
        // 왼쪽: 도착지와 숫자
        Column(Modifier.width(333.dp).fillMaxHeight()) {
            Text("도착", fontSize = 18.sp, color = Momo.Sub)
            Text(card.destination, fontSize = 44.sp, fontWeight = FontWeight.Bold)
            Text("${card.date} · 이야기도 함께 끝났어요", fontSize = 18.sp, color = Momo.Sub)
            Spacer(Modifier.height(28.dp))
            // 오늘 함께한 배역: 엔진이 들고 있는 탑승자 역할(엔진이 비었으면 엔딩 카드의 이름만)
            Text("오늘의 배역", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            (play.players.filter { it.role != null }.map { it.name to it.role!! }.ifEmpty { card.players.map { it to "" } }).forEach { (name, role) ->
                Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(name, active = true, size = 44.dp)
                    Spacer(Modifier.width(14.dp))
                    Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    if (role.isNotEmpty()) Text("  $role", fontSize = 18.sp, color = Momo.Sub)
                }
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                StatCard("장면", "${play.sceneIndex.takeIf { play.ending != null }?.plus(1) ?: card.minutes / 2}", "장면", "${card.minutes}분 이야기", Modifier.weight(1f).height(160.dp))
                StatCard("참여", "${card.players.size}", "명", card.players.joinToString(" · "), Modifier.weight(1f).height(160.dp))
            }
            Spacer(Modifier.height(13.dp))
            PillButton("홈으로", Modifier.fillMaxWidth(), dark = false) { leave(Screen.HOME) }
        }

        // 가운데: 엔딩 카드
        Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
            Column(Modifier.width(373.dp).background(Momo.Surface, RoundedCornerShape(24.dp)).padding(15.dp)) {
                Box(Modifier.fillMaxWidth().height(313.dp).clip(RoundedCornerShape(16.dp))) {
                    Cover(bot, Modifier.fillMaxSize(), image = bot.scenes.lastOrNull() ?: bot.cover)
                    Text(
                        "No. %02d".format(app.endingCards.size), Modifier.padding(12.dp).background(Color.White, RoundedCornerShape(7.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 15.sp,
                    )
                    Text(
                        card.grade, Modifier.align(Alignment.TopEnd).padding(12.dp).background(Momo.Ink, RoundedCornerShape(7.dp))
                            .padding(horizontal = 9.dp, vertical = 3.dp), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row {
                    Text("오늘의 엔딩", Modifier.weight(1f), fontSize = 16.sp, color = Momo.Sub)
                    Text(card.players.joinToString(" · "), fontSize = 16.sp, color = Momo.Sub)
                }
                Spacer(Modifier.height(4.dp))
                Text(card.title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(card.summary, fontSize = 17.sp, lineHeight = 21.sp, color = Momo.Sub, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }

        // 오른쪽: 오늘의 그림책, 보관함, 부모 폰 공유
        Column(Modifier.width(373.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Card(Modifier.fillMaxWidth().weight(1f)) {
                Row {
                    Text("오늘의 그림책", Modifier.weight(1f), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("${bot.scenes.size.coerceAtLeast(1)}장", fontSize = 15.sp, color = Momo.Sub)
                }
                (bot.scenes.ifEmpty { listOfNotNull(bot.cover) }).forEachIndexed { i, img ->
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Momo.Line)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Cover(bot, Modifier.size(43.dp).clip(RoundedCornerShape(8.dp)), image = img)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(bot.chapters.getOrNull(i * 2) ?: bot.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("장면 ${i * 2 + 1}", fontSize = 15.sp, color = Momo.Sub)
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "보관함에서 보기",
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).border(2.dp, Momo.Ink, RoundedCornerShape(50))
                        .clickable { leave(Screen.MY) }.padding(vertical = 12.dp),
                    fontSize = 19.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
            Row(
                Modifier.fillMaxWidth().background(Momo.Ink, RoundedCornerShape(16.dp)).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("엔딩 카드 · 그림책", fontSize = 15.sp, color = Color.White.copy(alpha = .6f))
                    Text("부모 폰으로 공유", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("QR을 찍으면 링크가 열려요", fontSize = 15.sp, color = Color.White.copy(alpha = .6f))
                }
                QrPlaceholder(card.title)
            }
        }
    }
}

/** QR 자리표시. 실제 링크 생성은 서버가 생긴 뒤에 붙인다. */
@Composable
private fun QrPlaceholder(seed: String) = Canvas(Modifier.size(88.dp).background(Color.White, RoundedCornerShape(8.dp)).padding(7.dp)) {
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
