package app.momo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import app.momo.core.Screen
import app.momo.core.SeatPos
import app.momo.ui.Momo
import app.momo.ui.components.*
import kotlinx.coroutines.delay

/** S0 차량 연결: 내비 목적지와 좌석을 먼저 읽고, 목적지가 잡히면 1.5초 뒤 버튼 없이 홈으로 간다. */
@Composable
fun ConnectScreen(app: AppState, car: CarState) {
    val ready = car.destination != null && car.remainSec != null
    LaunchedEffect(ready) {
        if (ready) { delay(1500); AppStore.go(Screen.HOME) }
    }
    val bot = app.bot
    val white = Color.White

    Hero(
        bot, Modifier.fillMaxSize().padding(start = Gutter, end = Gutter, bottom = Gutter).clip(RoundedCornerShape(4.dp)),
        image = bot.scenes.firstOrNull() ?: bot.cover,
    ) {
        Column(Modifier.fillMaxHeight().fillMaxWidth(.58f).padding(start = 48.dp), verticalArrangement = Arrangement.Center) {
            Text("차량 연결 · 출발 전 자동 확인", color = white, fontSize = 22.sp)
            Spacer(Modifier.height(12.dp))
            if (ready) {
                val min = car.remainSec!! / 60
                Text(car.destination!!, color = white, fontSize = 44.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "남은 시간 ${min}분" + (car.remainMeters?.let { " · 남은 거리 ${it / 1000}km" } ?: ""),
                    color = white, fontSize = 24.sp,
                )
                Spacer(Modifier.height(20.dp))
                MetaLine(listOf("약 ${scenesFor(min)}장면", "도착 5분 전 결말", "곧 홈으로 이동해요"), white.copy(alpha = .75f), 17.sp)
            } else {
                Text("목적지를 기다리고 있어요", color = white, fontSize = 44.sp, fontWeight = FontWeight.Bold, style = KoBreak)
                Spacer(Modifier.height(6.dp))
                Text("내비에 목적지를 넣으면 남은 시간에 맞춰 이야기 길이를 정해요", color = white, fontSize = 22.sp, style = KoBreak)
                Spacer(Modifier.height(20.dp))
                MetaLine(listOf("에뮬레이터에서는 오른쪽 아래 '시연' 패널로 목적지를 넣어요"), white.copy(alpha = .75f), 17.sp)
                Spacer(Modifier.height(36.dp))
                Text("목적지 없이 시작", color = white, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(15, 30, 60).forEach { m ->
                        GhostButton("${m}분 이야기") { AppStore.update { it.copy(rideMin = m, screen = Screen.HOME) } }
                    }
                }
            }
            Spacer(Modifier.height(36.dp))
            Text("NaviHelper · DestinationInfo.remainTime / remainDistance", color = white.copy(alpha = .5f), fontSize = 15.sp)
        }

        // 오른쪽: 좌석 신호를 유리 카드에
        GlassCard(Modifier.align(Alignment.CenterEnd).padding(end = 40.dp).width(380.dp)) {
            val belted = car.seats.values.count { it.occupied && it.belted }
            Text("탑승자", color = Momo.Sub, fontSize = 16.sp)
            Text("${car.riders}명 탑승 · 벨트 ${belted}명", color = Momo.Ink, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            listOf(SeatPos.DRIVER, SeatPos.PASSENGER, SeatPos.REAR_LEFT, SeatPos.REAR_RIGHT).forEach { pos ->
                val s = car.seats[pos]
                val name = app.names[pos].takeIf { s?.occupied == true }
                Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(name, active = s?.belted == true && name != null, size = 40.dp)
                    Spacer(Modifier.width(14.dp))
                    Text(name ?: "빈 좌석", color = if (name == null) Momo.Sub else Momo.Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text(pos.label + if (name != null && s?.belted != true) " · 벨트 안 함" else "", color = Momo.Sub, fontSize = 15.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Vehicle · Seat.registerSeatOccupancy", color = Momo.Sub, fontSize = 15.sp)
        }
    }
}
