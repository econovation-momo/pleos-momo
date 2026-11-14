package app.momo.ui.overlays

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.momo.core.AppState
import app.momo.core.AppStore
import app.momo.core.BotStatus
import app.momo.ui.Momo
import kotlin.random.Random

// ponytail: 가족 계정의 Pleos 차량 목록 대신 시연용 고정값이다.
private val CARS = listOf("할머니 차" to "더 뉴 그랜저 · 가족 계정 연결됨", "아빠 차" to "디 올 뉴 아반떼 · 가족 계정 연결됨")

/** M1 가족 차에 공유. 비공개 봇은 링크 공개로 바뀌고 마켓에는 나오지 않는다. 대상은 app.bot. */
@Composable
fun ShareDialog(app: AppState) {
    val bot = app.bot
    var cars by remember { mutableStateOf(setOf(0)) }
    var memory by remember { mutableStateOf(true) }
    var shared by remember { mutableStateOf(false) }

    Modal(760.dp) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FakeQr(bot.id, Modifier.size(200.dp).background(Momo.Surface, Round).padding(16.dp))
                Spacer(Modifier.height(11.dp))
                Text("폰으로 찍어 링크 받기", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("링크는 7일 동안 열려 있어요", fontSize = 12.sp, color = Momo.Sub)
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("가족 차에 공유", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(
                            bot.title + if (bot.status == BotStatus.PRIVATE) " · 링크 공개로 바뀌어요" else " · 받은 차에서도 기억이 이어져요",
                            fontSize = 13.sp, color = Momo.Sub,
                        )
                    }
                    IconBox("✕") { AppStore.close() }
                }
                Spacer(Modifier.height(12.dp))
                Column(Modifier.background(Momo.Surface, Round).padding(horizontal = 18.dp)) {
                    CARS.forEachIndexed { i, (name, sub) ->
                        CheckRow(name, sub, i in cars, lead = "🚗") { cars = if (i in cars) cars - i else cars + i }
                        HorizontalDivider(color = Momo.Line)
                    }
                    CheckRow("지난 회차 기억 · 엔딩 카드", "연재가 받은 차에서 이어져요", memory) { memory = !memory }
                }
                Spacer(Modifier.height(20.dp))
                if (!shared) Row(Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MainButton("취소", dark = false) { AppStore.close() }
                    MainButton("공유하기", enabled = cars.isNotEmpty()) {
                        if (bot.status == BotStatus.PRIVATE) AppStore.setStatus(bot.id, BotStatus.LINK)
                        shared = true
                    }
                } else Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${cars.sorted().joinToString(", ") { CARS[it].first }} 홈 화면에 「${bot.title}」${iGa(bot.title)} 나타나요",
                        Modifier.weight(1f).background(Momo.Surface, RoundedCornerShape(11.dp)).padding(12.dp),
                        fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.width(8.dp))
                    MainButton("닫기") { AppStore.close() }
                }
            }
        }
    }
}

/** QR 자리표시. 봇 id로 정해지는 무늬에 모서리 위치 표시만 그린다(실제 QR 아님). */
@Composable
private fun FakeQr(seed: String, modifier: Modifier) = Canvas(modifier) {
    val n = 21
    val cell = size.minDimension / n
    val rnd = Random(seed.hashCode())
    fun finder(x: Int, y: Int) = x < 7 && y < 7 || x >= n - 7 && y < 7 || x < 7 && y >= n - 7
    for (y in 0 until n) for (x in 0 until n) if (!finder(x, y) && rnd.nextBoolean())
        drawRect(Momo.Ink, Offset(x * cell, y * cell), Size(cell, cell))
    listOf(0 to 0, n - 7 to 0, 0 to n - 7).forEach { (fx, fy) ->
        drawRect(Momo.Ink, Offset(fx * cell, fy * cell), Size(7 * cell, 7 * cell))
        drawRect(Momo.Surface, Offset((fx + 1) * cell, (fy + 1) * cell), Size(5 * cell, 5 * cell))
        drawRect(Momo.Ink, Offset((fx + 2) * cell, (fy + 2) * cell), Size(3 * cell, 3 * cell))
    }
}
