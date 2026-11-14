package app.momo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.momo.Graph
import app.momo.R
import app.momo.car.CarState
import app.momo.car.PleosCarSource
import app.momo.core.*
import app.momo.ui.Momo
import app.momo.ui.components.*

/** 차량 위 도식(car_top 300x450)에서 좌석 중심의 비율 위치 */
private val SEAT_XY = mapOf(
    SeatPos.DRIVER to (0.415f to 0.42f), SeatPos.PASSENGER to (0.585f to 0.42f),
    SeatPos.REAR_LEFT to (0.415f to 0.6f), SeatPos.REAR_RIGHT to (0.585f to 0.6f),
)

/** 시연용 좌석 순환: 빈 좌석 → 착좌·벨트 → 벨트 안 함 → 빈 좌석 */
private fun next(s: SeatState?) = when {
    s == null || !s.occupied -> SeatState(occupied = true, belted = true)
    s.belted -> SeatState(occupied = true, belted = false)
    else -> SeatState(occupied = false, belted = false)
}

/** S4 탑승자·역할(구도 B, 21:7): 히어로 왼쪽에 배역을 쌓고, 좌석 도식은 오른쪽 유리 카드 하나에 담는다. */
@Composable
fun PlayersScreen(app: AppState, car: CarState) {
    val bot = app.bot
    val players = assignRoles(bot, car.seats, app.names, app.driverJoins)
    val bySeat = players.associateBy { it.seat }
    val min = rideMin(app, car)
    val cast = players.count { it.plays }
    // 시연 패널과 같은 경로로 바꿔야 실제 SDK 값(예: 운전석 빈 좌석)에 덮이지 않는다
    val tap = { pos: SeatPos ->
        val value = next(car.seats[pos])
        (Graph.car as? PleosCarSource)?.simulate(*PleosCarSource.seatItems(pos).toTypedArray()) { setSeat(pos, value) }
            ?: Graph.sim.setSeat(pos, value)
    }
    val white = Color.White

    // 오른쪽: 차량 위 도식(유리 카드 하나). 좌석을 누르면 시연용으로 착좌·벨트가 바뀐다.
    HeroScreen(bot, side = {
        GlassCard(Modifier.width(470.dp)) {
            BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 56.dp), contentAlignment = Alignment.Center) {
                val w = maxWidth
                val h = w * (450f / 300f)
                Box(Modifier.size(w, h)) {
                    Image(painterResource(R.drawable.car_top), "차량 좌석 도식", Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)))
                    SeatPos.entries.forEach { pos ->
                        val (fx, fy) = SEAT_XY.getValue(pos)
                        val p = bySeat[pos]
                        val left = fx < .5f
                        Box(Modifier.offset(w * fx - 23.dp, h * fy - 23.dp).clickable { tap(pos) }, Alignment.Center) {
                            Avatar(p?.name, active = p?.plays == true, size = 46.dp)
                            // 빈 좌석은 눌러서 탑승자를 추가할 수 있다는 표시
                            if (p == null) Text("+", color = Momo.Ink, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(
                            Modifier.width(110.dp).offset(if (left) w * .2f - 110.dp else w * .8f, h * fy - 22.dp),
                            horizontalAlignment = if (left) Alignment.End else Alignment.Start,
                        ) {
                            Text(p?.name ?: "빈 좌석", color = if (p == null) Momo.Sub else Momo.Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            Text(p?.role ?: if (p != null) "듣기만" else pos.short, color = Momo.Sub, fontSize = 15.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "빈 좌석의 +를 누르면 탑승해요. 다시 누르면 벨트를 풀고, 한 번 더 누르면 내려요 (시연)", Modifier.fillMaxWidth(), color = Momo.Sub, fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )
        }
    }) {
        HeroHeader(
            kicker = "출발 준비 · 좌석과 역할",
            title = bot.title,
            subtitle = "출발하면 화면 대신 목소리로 이야기가 이어져요",
            meta = listOf(destLabel(app, car), "약 ${scenesFor(min)}장면", "참여 ${cast}명") +
                (players.size - cast).let { if (it > 0) listOf("듣기만 ${it}명") else emptyList() },
        )
        Spacer(Modifier.height(22.dp))
        // 배역 목록: 상자 없이 이미지 위에 바로 얹는다
        players.forEach { p ->
            Row(Modifier.clickable { tap(p.seat) }.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(p.name, active = p.plays, size = 40.dp)
                Spacer(Modifier.width(14.dp))
                Text(p.name, color = white, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("  ${p.role ?: "듣기만"}", color = white.copy(alpha = if (p.plays) 1f else .6f), fontSize = 19.sp)
                Text("  ·  ${p.seat.label}", color = white.copy(alpha = .6f), fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("운전자도 조연으로 참여 · 짧은 대사만", color = white, fontSize = 18.sp)
            Spacer(Modifier.width(14.dp))
            Switch(
                app.driverJoins, { v -> AppStore.update { it.copy(driverJoins = v) } },
                colors = SwitchDefaults.colors(checkedTrackColor = white, checkedThumbColor = Momo.Ink, uncheckedTrackColor = white.copy(alpha = .25f)),
            )
        }
        Spacer(Modifier.height(26.dp))
        PillButton(if (cast > 0) "출발 · 이야기 시작" else "역할을 맡을 탑승자가 없어요", height = 64.dp, fontSize = 22.sp) {
            if (cast > 0) {
                Graph.engine.start(bot, players, car.destination ?: "목적지 없음", car.remainSec ?: (min * 60))
                AppStore.go(Screen.PLAY)
            }
        }
    }
}
