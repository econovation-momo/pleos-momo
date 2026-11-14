package app.momo.car

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.momo.Graph
import app.momo.core.SeatPos
import app.momo.core.SeatState
import app.momo.ui.Momo
import app.momo.car.PleosCarSource.Companion.ITEM_DEST
import app.momo.car.PleosCarSource.Companion.ITEM_DRIVING

/**
 * 시연용 차량 신호 패널. 평소에는 우측 하단의 작은 "시연" 버튼으로 접혀 있다.
 * 실제 SDK 모드에서 여기서 값을 바꾸면 그 항목만 시뮬레이터 값으로 덮인다("SIM" 표시).
 */
@Composable
fun SimPanel(modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    val pleos = Graph.car as? PleosCarSource
    val sim = Graph.sim
    val car by Graph.car.state.collectAsState()
    val useSdk = pleos?.useSdk?.collectAsState()?.value ?: false
    val speed = pleos?.speed?.collectAsState()?.value ?: 1

    fun simulate(vararg items: String, block: SimCarSource.() -> Unit) =
        if (pleos != null) pleos.simulate(*items, block = block) else sim.block()

    Column(modifier.padding(8.dp), horizontalAlignment = Alignment.End) {
        if (open) Column(
            Modifier.width(360.dp).heightIn(max = 460.dp)
                .background(Glass, RoundedCornerShape(20.dp))
                .border(1.dp, Momo.Line, RoundedCornerShape(20.dp))
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(summary(car), color = Momo.Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(
                if (car.live.isEmpty()) "SDK 값 없음: 전부 시뮬레이터" else liveSummary(car.live),
                color = Momo.Sub, fontSize = 11.sp,
            )
            Line("모드") {
                Chip("실제 SDK", useSdk) { pleos?.setUseSdk(true) }
                Chip("시뮬레이터", !useSdk) { pleos?.setUseSdk(false) }
            }
            Line("목적지") {
                Chip("할머니 댁 45분", car.destination == "할머니 댁") { simulate(ITEM_DEST) { setDestination("할머니 댁", 45 * 60, 38_000) } }
                Chip("유치원 15분", car.destination == "유치원") { simulate(ITEM_DEST) { setDestination("유치원", 15 * 60, 6_000) } }
                Chip("없음", car.destination == null) { simulate(ITEM_DEST) { setDestination(null, null) } }
            }
            Line("주행") {
                Chip(if (car.driving) "주행 중 → 정차" else "정차 중 → 출발", car.driving) {
                    simulate(ITEM_DRIVING, PleosCarSource.ITEM_GEAR) { setDriving(!car.driving) }
                }
            }
            Line("배속") {
                listOf(1, 10, 30).forEach { x -> Chip("${x}x", speed == x) { pleos?.speed?.value = x } }
            }
            Line("도착") {
                Chip("도착 5분 전으로", false) {
                    simulate(ITEM_DEST) {
                        setDestination(state.value.destination ?: "할머니 댁", 300, 4_000)
                    }
                }
                Chip("도착", false) { simulate(ITEM_DEST, ITEM_DRIVING, PleosCarSource.ITEM_GEAR) { arrive() } }
            }
            SeatPos.entries.forEach { pos ->
                val seat = car.seats[pos] ?: SeatState(false, false)
                val (occItem, beltItem) = PleosCarSource.seatItems(pos)
                Line(pos.short) {
                    Chip(tag("앉음", car.isLive(occItem)), seat.occupied) {
                        simulate(occItem, beltItem) { setSeat(pos, seat.copy(occupied = !seat.occupied)) }
                    }
                    Chip(tag("벨트", car.isLive(beltItem)), seat.belted) {
                        simulate(occItem, beltItem) { setSeat(pos, seat.copy(belted = !seat.belted)) }
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier.background(Glass, Pill)
                .border(1.dp, Momo.Line, Pill)
                .clickable { open = !open }
                .padding(horizontal = 14.dp, vertical = 6.dp),
        ) { Text(if (open) "닫기" else "시연", color = Momo.Ink, fontSize = 12.sp) }
    }
}

/** 예: "SDK: 주행 · 기어 · 목적지 · 좌석 8/8" */
private fun liveSummary(live: Set<String>): String {
    val seats = live.count { it.startsWith("착좌") || it.startsWith("벨트") }
    return "SDK: " + (live.filterNot { it.startsWith("착좌") || it.startsWith("벨트") } + "좌석 $seats/8").joinToString(" · ")
}

private fun tag(label: String, live: Boolean) = if (live) "$label·SDK" else label

private fun summary(car: CarState): String {
    val remain = car.remainSec?.let { "%d:%02d".format(it / 60, it % 60) } ?: "-"
    val km = car.remainMeters?.let { "%.1fkm".format(it / 1000f) } ?: ""
    return "${car.source} · ${if (car.driving) "주행" else "정차"} ${car.gear} · ${car.destination ?: "목적지 없음"} $remain $km"
}

@Composable
private fun Line(label: String, content: @Composable RowScope.() -> Unit) = Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
) {
    Text(label, color = Momo.Sub, fontSize = 12.sp, modifier = Modifier.width(56.dp))
    content()
}

@Composable
private fun Chip(label: String, on: Boolean, onClick: () -> Unit) = Box(
    Modifier.background(if (on) Momo.Accent else Momo.Surface, Pill)
        .clickable(onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 6.dp),
) { Text(label, color = if (on) Momo.OnAccent else Momo.Ink, fontSize = 12.sp) }

/** Figma 양식: 반투명 흰 유리 카드, 알약 버튼 */
private val Glass = Color(0xD9FFFFFF)
private val Pill = RoundedCornerShape(50)
