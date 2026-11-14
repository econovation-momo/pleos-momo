package app.momo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.momo.Graph
import app.momo.car.SimPanel
import app.momo.core.AppStore
import app.momo.core.Overlay
import app.momo.core.Screen
import app.momo.ui.overlays.*
import app.momo.ui.screens.*

/** 정차 중에만 여는 화면. 주행 중이면 막고 음성 안내만 보여 준다(F7). */
private val PARKED_ONLY = setOf(Screen.HOME, Screen.MARKET, Screen.DETAIL, Screen.PLAYERS, Screen.MY, Screen.UPLOAD)

@Composable
fun MomoApp() {
    val app by AppStore.state.collectAsStateWithLifecycle()
    val car by Graph.car.state.collectAsStateWithLifecycle()
    val blocked = car.driving && app.screen in PARKED_ONLY

    Column(Modifier.fillMaxSize().background(Momo.Bg)) {
        TopBar(app, car.gear, car.driving)
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (blocked) DrivingBlocked()
            else when (app.screen) {
                Screen.CONNECT -> ConnectScreen(app, car)
                Screen.HOME -> HomeScreen(app, car)
                Screen.DETAIL -> DetailScreen(app, car)
                Screen.MARKET -> MarketScreen(app, car)
                Screen.PLAYERS -> PlayersScreen(app, car)
                Screen.PLAY -> PlayScreen(app, car)
                Screen.ENDING -> EndingScreen(app, car)
                Screen.MY -> MyScreen(app, car)
                Screen.UPLOAD -> UploadScreen(app, car)
            }
            // 만들기·신고·공유 같은 화면 조작은 정차 중에만. 주행이 시작되면 숨긴다(내용은 유지 = 일시정지).
            if (!car.driving) when (app.overlay) {
                Overlay.NONE -> Unit
                Overlay.CREATE -> CreateOverlay(app, car)
                Overlay.REPORT -> ReportSheet(app)
                Overlay.KIDS_PIN -> KidsPinDialog(app)
                Overlay.SHARE -> ShareDialog(app)
            }
            SimPanel(Modifier.align(Alignment.BottomEnd))
        }
    }
}

@Composable
private fun DrivingBlocked() = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("주행 중이에요", fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Text("정차하면 이야기를 고를 수 있어요", fontSize = 24.sp, color = Momo.Sub)
    }
}
