package app.momo.ui.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import app.momo.core.AppState
import app.momo.core.AppStore
import app.momo.ui.Momo

/** TODO(화면 세션): M1 가족 차 공유 */
@Composable
fun ShareDialog(app: AppState) {
    Box(Modifier.fillMaxSize().background(Momo.Scrim), contentAlignment = Alignment.Center) {
        Column { Text("M1 가족 차 공유", fontSize = 28.sp, color = Momo.OnAccent); Button({ AppStore.close() }) { Text("닫기") } }
    }
}
