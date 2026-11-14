package app.momo.pleos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * SDK 검증 화면. 에뮬레이터 + Virtual Console에서 어떤 신호가 실제로 들어오는지 확인한다.
 * 디버그용: adb shell am start -n app.momo/.pleos.SdkCheckActivity
 */
class SdkCheckActivity : ComponentActivity() {
    private lateinit var vehicle: VehicleSignals
    private lateinit var navi: NaviSignals
    private lateinit var car: CarSignals
    private lateinit var voice: VoiceIO

    private val permissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        result.forEach { (p, granted) -> SignalHub.log("권한 ${p.substringAfterLast('.')}: ${if (granted) "허용" else "거부"}") }
        car.subscribeProperties()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vehicle = VehicleSignals(this).also { it.start() }
        navi = NaviSignals(this).also { it.start() }
        car = CarSignals(this).also { it.start() }
        voice = VoiceIO(this).also { it.start() }
        permissions.launch(arrayOf("android.car.permission.CAR_SPEED", "pleos.car.permission.FUSED_LOCATION"))

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                CheckScreen(
                    actions = listOf(
                        "TTS 말하기" to { voice.speak("안녕하세요. 플레오스 검증 화면입니다.") },
                        "STT 듣기" to { voice.listen() },
                        "STT 중지" to { voice.stopListening() },
                        "내비 경로 요청" to { navi.requestSampleRoute() },
                        "내비 경로 취소" to { navi.cancelRoute() },
                        "경고등 조회" to { vehicle.refreshTelltales() },
                    ),
                    rms = { voice.lastRms },
                )
            }
        }
    }

    override fun onDestroy() {
        voice.stop(); navi.stop(); car.stop(); vehicle.stop()
        super.onDestroy()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CheckScreen(actions: List<Pair<String, () -> Unit>>, rms: () -> Double) {
    val signals by SignalHub.signals.collectAsStateWithLifecycle()
    val log by SignalHub.log.collectAsStateWithLifecycle()
    val groups = signals.entries.groupBy { it.key.substringBefore('/') }.toSortedMap()

    Row(Modifier.fillMaxSize().background(Color(0xFF101418)).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LazyColumn(Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Pleos SDK 검증", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    actions.forEach { (label, run) -> Button(onClick = run) { Text(label, fontSize = 18.sp) } }
                }
                Text("STT 소리 크기(rms): ${"%.1f".format(rms())}", color = Color.LightGray, fontSize = 16.sp)
            }
            items(groups.entries.toList()) { (group, entries) ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2329))) {
                    Column(Modifier.padding(12.dp)) {
                        Text(group, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7FD1FF))
                        entries.sortedBy { it.key }.forEach { (key, s) ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                Text(key.substringAfter('/'), Modifier.weight(1f), color = Color.LightGray, fontSize = 16.sp)
                                Text(s.value, Modifier.weight(2f), color = if (s.value.startsWith("실패")) Color(0xFFFF8A80) else Color.White, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
        LazyColumn(Modifier.weight(1f).background(Color(0xFF0B0E11)).padding(8.dp)) {
            item { Text("이벤트 로그", color = Color.White, fontWeight = FontWeight.Bold) }
            items(log) { Text(it, color = Color(0xFFB0BEC5), fontFamily = FontFamily.Monospace, fontSize = 12.sp) }
        }
    }
}
