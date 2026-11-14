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

/** TODO(화면 세션): S2 마켓 */
@Composable
fun MarketScreen(app: AppState, car: CarState) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("S2 마켓", fontSize = 28.sp)
        Button({ AppStore.go(Screen.DETAIL) }) { Text("다음") }
    }
}
