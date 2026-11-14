package app.momo.ui

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import app.momo.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.momo.core.AppState
import app.momo.core.AppStore
import app.momo.core.Overlay
import app.momo.core.Screen
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Figma 상단 바: 로고 · (정차 중) 탭 · 키즈 모드 · PRND · 시계 */
@Composable
fun TopBar(app: AppState, gear: String, driving: Boolean) {
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) { while (true) { now = Date(); delay(10_000) } }

    Row(
        Modifier.fillMaxWidth().height(72.dp).background(Momo.Bg).padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.background(Color.White, RoundedCornerShape(6.dp)).padding(horizontal = 16.dp, vertical = 10.dp)) {
            Image(painterResource(R.drawable.logo_momo), "MOMO", Modifier.height(22.dp))
        }
        if (!driving && app.screen != Screen.PLAY && app.screen != Screen.CONNECT) {
            Spacer(Modifier.width(24.dp))
            Tab("홈", app.screen == Screen.HOME) { AppStore.go(Screen.HOME) }
            Tab("마켓", app.screen in setOf(Screen.MARKET, Screen.DETAIL, Screen.PLAYERS)) { AppStore.go(Screen.MARKET) }
            Tab("마이페이지", app.screen in setOf(Screen.MY, Screen.UPLOAD)) { AppStore.go(Screen.MY) }
        }
        Spacer(Modifier.weight(1f))
        Row(
            Modifier.background(Momo.Surface, RoundedCornerShape(12.dp)).padding(start = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("키즈 모드", fontSize = 15.sp, color = Momo.Ink)
            Spacer(Modifier.width(8.dp))
            // 키즈 모드를 끌 때는 부모 PIN 확인(K1)
            Switch(
                checked = app.kids,
                onCheckedChange = { if (app.kids) AppStore.open(Overlay.KIDS_PIN) else AppStore.toggleKids() },
                enabled = !driving,
                colors = SwitchDefaults.colors(checkedTrackColor = Momo.Ink),
            )
        }
        Spacer(Modifier.width(24.dp))
        "PRND".forEach { g ->
            Text(
                g.toString(), Modifier.padding(horizontal = 5.dp), fontSize = 20.sp,
                color = if (g.toString() == gear) Momo.Ink else Momo.Line, fontWeight = FontWeight.Normal,
            )
        }
        Spacer(Modifier.width(24.dp))
        Text(SimpleDateFormat("HH:mm", Locale.KOREA).format(now), fontSize = 20.sp, fontWeight = FontWeight.Normal)
    }
}

@Composable
private fun Tab(label: String, on: Boolean, onClick: () -> Unit) {
    Text(
        label,
        Modifier.padding(end = 8.dp).background(if (on) Momo.Ink else Momo.Bg, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 10.dp),
        color = if (on) Momo.OnAccent else Momo.Ink, fontSize = 17.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
    )
}
