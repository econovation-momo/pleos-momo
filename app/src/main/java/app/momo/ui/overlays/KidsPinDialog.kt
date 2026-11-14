package app.momo.ui.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.momo.core.AppState
import app.momo.core.AppStore
import app.momo.ui.Momo
import app.momo.ui.components.GhostButton

// ponytail: 부모 PIN은 시연용 고정값이다. 설정 화면이 생기면 저장소로 옮긴다.
private const val PARENT_PIN = "0000"

/** K1 키즈 모드를 끌 때 부모 PIN 4자리를 묻는다 */
@Composable
fun KidsPinDialog(app: AppState) {
    var pin by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }

    fun press(d: Char) {
        if (pin.length >= 4) return
        pin += d
        wrong = false
        if (pin.length < 4) return
        if (pin == PARENT_PIN) { if (app.kids) AppStore.toggleKids(); AppStore.close() } else { wrong = true; pin = "" }
    }

    Modal(400.dp) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("키즈 모드를 끌까요?", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                if (wrong) "PIN이 맞지 않아요. 다시 입력해 주세요" else "부모 PIN 4자리를 입력해 주세요 · 시연 PIN 0000",
                fontSize = 14.sp, color = if (wrong) Momo.Ink else Momo.Sub, fontWeight = if (wrong) FontWeight.Bold else FontWeight.Normal,
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                repeat(4) { i ->
                    Box(Modifier.size(14.dp).clip(CircleShape).then(if (i < pin.length) Modifier.background(Momo.Ink) else Modifier.border(1.5.dp, Momo.Ink, CircleShape)))
                }
            }
            Spacer(Modifier.height(20.dp))
            listOf("123", "456", "789", " 0⌫").forEach { row ->
                Row(Modifier.padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    row.forEach { k ->
                        Box(
                            Modifier.size(68.dp).clip(CircleShape)
                                .background(if (k == ' ') Momo.Ink.copy(alpha = 0f) else Momo.Ink.copy(alpha = 0.06f))
                                .clickable(enabled = k != ' ') { if (k == '⌫') pin = pin.dropLast(1) else press(k) },
                            contentAlignment = Alignment.Center,
                        ) { Text(k.toString(), fontSize = 26.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            GhostButton("취소", onImage = false) { AppStore.close() }
        }
    }
}
