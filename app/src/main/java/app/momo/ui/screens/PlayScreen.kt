package app.momo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.momo.car.CarState
import app.momo.core.AppState
import app.momo.core.AppStore
import app.momo.core.Screen

/** TODO(화면 세션): S5 주행 중 플레이 (Figma 운전 중/정차) */
@Composable
fun PlayScreen(app: AppState, car: CarState) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("S5 주행 중 플레이 (Figma 운전 중/정차)", fontSize = 28.sp)
        Button({ AppStore.go(Screen.ENDING) }) { Text("다음") }
    }
}
