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

/** TODO(화면 세션): S0 차량 연결 */
@Composable
fun ConnectScreen(app: AppState, car: CarState) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("S0 차량 연결", fontSize = 28.sp)
        Button({ AppStore.go(Screen.HOME) }) { Text("다음") }
    }
}
