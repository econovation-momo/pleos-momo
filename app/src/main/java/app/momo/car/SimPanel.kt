package app.momo.car

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.momo.Graph

/**
 * 시연용 차량 신호 패널. 에뮬레이터에 내비 목적지·좌석 신호가 없을 때 쓴다.
 * TODO(차량·음성 세션): 좌석 토글, 시간 배속, 실제 SDK/시뮬레이터 전환 등을 채운다.
 */
@Composable
fun SimPanel(modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    val sim = Graph.sim
    val car by sim.state.collectAsState()
    Column(modifier.padding(12.dp)) {
        if (open) Column(
            Modifier.background(Color(0xEE222222), RoundedCornerShape(12.dp)).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            OutlinedButton({ sim.setDestination("할머니 댁", 45 * 60, 38_000) }) { Text("목적지: 할머니 댁 45분", color = Color.White) }
            OutlinedButton({ sim.setDriving(!car.driving) }) { Text(if (car.driving) "정차 (신호 대기)" else "출발 (주행)", color = Color.White) }
            OutlinedButton({ sim.arrive() }) { Text("도착", color = Color.White) }
        }
        OutlinedButton({ open = !open }) { Text("시연", fontSize = 14.sp) }
    }
}
