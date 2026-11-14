package app.momo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import app.momo.core.SeatPos
import app.momo.ui.Momo
import app.momo.ui.components.ApiTag
import app.momo.ui.components.Card
import app.momo.ui.components.Chip
import app.momo.ui.components.scenesFor
import kotlinx.coroutines.delay

/** S0 차량 연결: 내비 목적지와 좌석을 먼저 읽고, 목적지가 잡히면 1.5초 뒤 버튼 없이 홈으로 간다. */
@Composable
fun ConnectScreen(app: AppState, car: CarState) {
    val ready = car.destination != null && car.remainSec != null
    LaunchedEffect(ready) {
        if (ready) { delay(1500); AppStore.go(Screen.HOME) }
    }

    Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.spacedBy(19.dp)) {
        Text("차량 연결 · 출발 전 자동 확인", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("앱을 열면 차량 정보를 먼저 읽어서 이야기 길이와 역할을 정해요.", fontSize = 20.sp, color = Momo.Sub)

        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(19.dp)) {
            Card(Modifier.weight(1f).fillMaxHeight()) {
                Text("내비 목적지", fontSize = 18.sp, color = Momo.Sub)
                Spacer(Modifier.height(11.dp))
                if (ready) {
                    val min = car.remainSec!! / 60
                    Text(car.destination!!, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "남은 시간 ${min}분" + (car.remainMeters?.let { " · 남은 거리 ${it / 1000}km" } ?: ""),
                        fontSize = 20.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("→ 약 ${scenesFor(min)}장면, 도착 5분 전 결말로 설정", fontSize = 20.sp, color = Momo.Sub)
                } else {
                    Text("내비에 목적지를 입력하면 남은 시간을 불러와요", fontSize = 24.sp)
                    Spacer(Modifier.height(5.dp))
                    Text("에뮬레이터에서는 오른쪽 아래 '시연' 패널에서 목적지를 넣을 수 있어요.", fontSize = 18.sp, color = Momo.Sub)
                    Spacer(Modifier.height(19.dp))
                    Text("목적지 없이 시작 · 이야기 길이", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 30, 60).forEach { m ->
                            Chip("${m}분", on = app.rideMin == m, fontSize = 20.sp) {
                                AppStore.update { it.copy(rideMin = m, screen = Screen.HOME) }
                            }
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                ApiTag("NaviHelper · DestinationInfo.remainTime / remainDistance")
            }

            Card(Modifier.weight(1f).fillMaxHeight()) {
                val belted = car.seats.values.count { it.occupied && it.belted }
                Text("탑승자", fontSize = 18.sp, color = Momo.Sub)
                Spacer(Modifier.height(11.dp))
                Text("${car.riders}명 탑승 · 벨트 ${belted}명", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                // 차 안 배치 그대로: 앞줄(운전석·조수석), 뒷줄
                listOf(listOf(SeatPos.DRIVER, SeatPos.PASSENGER), listOf(SeatPos.REAR_LEFT, SeatPos.REAR_RIGHT)).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { pos ->
                            val s = car.seats[pos]
                            val label = when {
                                s?.occupied != true -> "빈 좌석"
                                s.belted -> "${app.names[pos] ?: ""} · 벨트"
                                else -> "${app.names[pos] ?: ""} · 벨트 안 함"
                            }
                            Column(
                                Modifier.width(240.dp).background(if (s?.occupied == true) Color.White else Color.Transparent, RoundedCornerShape(11.dp))
                                    .border(1.dp, Momo.Line, RoundedCornerShape(11.dp))
                                    .padding(11.dp),
                            ) {
                                Text(pos.label, fontSize = 15.sp, color = Momo.Sub)
                                Text(label, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (s?.occupied == true) Momo.Ink else Momo.Sub)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.weight(1f))
                ApiTag("Vehicle · Seat.registerSeatOccupancy / 안전벨트")
            }
        }
        Text(
            if (ready) "연동 완료 · 곧 홈으로 이동해요" else "목적지를 기다리는 중이에요",
            Modifier, fontSize = 18.sp, color = Momo.Sub,
        )
    }
}
