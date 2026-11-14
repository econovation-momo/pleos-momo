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

/** TODO(화면 세션): S3 봇 상세 (Figma 상페) */
@Composable
fun DetailScreen(app: AppState, car: CarState) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("S3 봇 상세 (Figma 상페)", fontSize = 28.sp)
        Button({ AppStore.go(Screen.PLAYERS) }) { Text("다음") }
    }
}
