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

/** TODO(화면 세션): K1 키즈 모드 해제 PIN */
@Composable
fun KidsPinDialog(app: AppState) {
    Box(Modifier.fillMaxSize().background(Momo.Scrim), contentAlignment = Alignment.Center) {
        Column { Text("K1 키즈 모드 해제 PIN", fontSize = 28.sp, color = Momo.OnAccent); Button({ AppStore.close() }) { Text("닫기") } }
    }
}
