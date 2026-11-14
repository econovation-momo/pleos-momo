package app.momo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.momo.car.CarState
import app.momo.core.AppState
import app.momo.core.AppStore
import app.momo.core.Screen
import app.momo.ui.components.*
import kotlinx.coroutines.delay

/**
 * S0 차량 연결(구도 B, 21:7): 내비 목적지와 좌석을 먼저 읽는다.
 * 목적지가 잡히면 1.5초 뒤 버튼 없이 홈으로 가고, 없으면 15/30/60분 중에서 골라 시작한다.
 */
@Composable
fun ConnectScreen(app: AppState, car: CarState) {
    val ready = car.destination != null && car.remainSec != null
    LaunchedEffect(ready) {
        if (ready) { delay(1500); AppStore.go(Screen.HOME) }
    }
    val belted = car.seats.values.count { it.occupied && it.belted }

    HeroScreen(app.bot) {
        if (ready) {
            val min = car.remainSec!! / 60
            HeroHeader(
                kicker = "차량 연결 완료",
                title = "${car.destination}까지 ${min}분",
                subtitle = "도착하면 이야기도 함께 끝나요",
                meta = listOfNotNull(car.remainMeters?.let { "${it / 1000}km" }, "약 ${scenesFor(min)}장면", "${car.riders}명 탑승"),
            )
        } else {
            HeroHeader(
                kicker = "차량 연결 · 출발 전 자동 확인",
                title = "목적지를 기다리고 있어요",
                subtitle = "내비에 목적지를 넣으면 남은 시간에 맞춰 이야기 길이를 정해요",
                meta = listOf("${car.riders}명 탑승", "벨트 ${belted}명", "시연 패널로 목적지 입력"),
            )
        }
        Spacer(Modifier.height(26.dp))
        // 연결 진행: 좌석은 바로 읽히고, 내비 목적지까지 받으면 다 찬다
        ProgressBar(if (ready) 1f else .5f, Color.White, Modifier.width(400.dp))
        Spacer(Modifier.height(10.dp))
        MetaLine(listOf("NaviHelper · DestinationInfo.remainTime", "Vehicle · Seat.registerSeatOccupancy"), Color.White.copy(alpha = .55f))
        if (!ready) {
            Spacer(Modifier.height(30.dp))
            Text("목적지 없이 시작", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(15, 30, 60).forEach { m ->
                    PillButton("${m}분 이야기", icon = null, height = 56.dp, fontSize = 18.sp) {
                        AppStore.update { it.copy(rideMin = m, screen = Screen.HOME) }
                    }
                }
            }
        }
    }
}
