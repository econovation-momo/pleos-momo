package app.momo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.momo.Graph
import app.momo.R
import app.momo.car.CarState
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

/** S4 탑승자·역할: 좌석·벨트 신호로 역할을 나누고 출발한다. */
@Composable
fun PlayersScreen(app: AppState, car: CarState) {
    val bot = app.bot
    val players = assignRoles(bot, car.seats, app.names, app.driverJoins)
    val bySeat = players.associateBy { it.seat }
    val min = rideMin(app, car)
    val cast = players.count { it.plays }
    val tap = { pos: SeatPos -> Graph.sim.setSeat(pos, next(car.seats[pos])) }

    Row(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(21.dp)) {
        // 왼쪽: 차량 위 도식
        BoxWithConstraints(
            Modifier.weight(1f).fillMaxHeight().background(Color(0xFFE7E6E4), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            val h = maxHeight
            val w = h * (300f / 450f)
            Box(Modifier.size(w, h)) {
                Image(painterResource(R.drawable.car_top), "차량 좌석 도식", Modifier.fillMaxSize())
                SeatPos.entries.forEach { pos ->
                    val (fx, fy) = SEAT_XY.getValue(pos)
                    val p = bySeat[pos]
                    val left = fx < .5f
                    Avatar(
                        p?.name, active = p?.plays == true, size = 51.dp,
                        modifier = Modifier.offset(w * fx - 25.dp, h * fy - 25.dp).clickable { tap(pos) },
                    )
                    // 이름표는 차체 바깥에 두고 가는 선으로 좌석과 잇는다
                    val lineX = if (left) w * .23f else w * fx + 26.dp
                    Box(Modifier.offset(lineX, h * fy).size(if (left) w * (fx - .23f) - 26.dp else w * (.77f - fx) - 26.dp, 1.dp).background(Momo.Sub.copy(alpha = .5f)))
                    Column(
                        Modifier.width(160.dp).offset(if (left) w * .22f - 160.dp else w * .78f, h * fy - 24.dp),
                        horizontalAlignment = if (left) Alignment.End else Alignment.Start,
                    ) {
                        Text(p?.name ?: "빈 좌석", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if (p == null) Momo.Sub else Momo.Ink)
                        Text(p?.role ?: if (p != null) "듣기만" else pos.short, fontSize = 17.sp, color = Momo.Sub)
                    }
                }
            }
            Text(
                "좌석을 눌러 탑승·벨트를 바꿀 수 있어요 (시연)",
                Modifier.align(Alignment.BottomCenter).padding(13.dp), fontSize = 15.sp, color = Momo.Sub,
            )
        }

        // 오른쪽: 좌석 목록, 운전자 참여, 오늘의 이야기
        Column(Modifier.width(453.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Card(Modifier.fillMaxWidth().weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("좌석과 역할", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("좌석을 눌러 바꾸기", fontSize = 15.sp, color = Momo.Sub)
                }
                listOf(SeatPos.REAR_LEFT, SeatPos.REAR_RIGHT, SeatPos.DRIVER, SeatPos.PASSENGER).forEach { pos ->
                    val p = bySeat[pos]
                    val s = car.seats[pos]
                    HorizontalDivider(Modifier.padding(vertical = 7.dp), color = Momo.Line)
                    Row(Modifier.fillMaxWidth().clickable { tap(pos) }, verticalAlignment = Alignment.CenterVertically) {
                        Avatar(p?.name, active = p?.plays == true, size = 52.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p?.name ?: "빈 좌석", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = if (p == null) Momo.Sub else Momo.Ink)
                            Text(pos.label + if (s?.occupied == true && !s.belted) " · 벨트 안 함" else "", fontSize = 16.sp, color = Momo.Sub)
                        }
                        when {
                            p?.role != null -> Chip(p.role, on = true)
                            p != null -> Chip("듣기만")
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                HorizontalDivider(Modifier.padding(bottom = 8.dp), color = Momo.Line)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("운전자도 조연으로 참여", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text("짧은 대사만 맡아요", fontSize = 15.sp, color = Momo.Sub)
                    }
                    Switch(
                        app.driverJoins, { v -> AppStore.update { it.copy(driverJoins = v) } },
                        colors = SwitchDefaults.colors(checkedTrackColor = Momo.Ink),
                    )
                }
            }

            Card(Modifier.fillMaxWidth(), color = Momo.Ink) {
                Text("오늘의 이야기", fontSize = 15.sp, color = Color.White.copy(alpha = .6f))
                Text(bot.title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text("${car.destination?.let { "${it}까지 " } ?: ""}${min}분 · 약 ${scenesFor(min)}장면", fontSize = 19.sp, color = Color.White)
                Text(
                    "참여 ${cast}명" + (players.size - cast).let { if (it > 0) " · 듣기만 ${it}명" else "" },
                    fontSize = 19.sp, color = Color.White.copy(alpha = .8f),
                )
                Spacer(Modifier.height(13.dp))
                PillButton(
                    if (cast > 0) "출발 · 이야기 시작" else "역할을 맡을 탑승자가 없어요",
                    Modifier.fillMaxWidth(), dark = false, height = 56.dp, fontSize = 19.sp,
                ) {
                    if (cast > 0) {
                        Graph.engine.start(bot, players, car.destination ?: "목적지 없음", car.remainSec ?: (min * 60))
                        AppStore.go(Screen.PLAY)
                    }
                }
                Spacer(Modifier.height(7.dp))
                Text("출발하면 음성으로만 진행돼요", Modifier.fillMaxWidth(), fontSize = 15.sp, color = Color.White.copy(alpha = .6f), textAlign = TextAlign.Center)
            }
        }
    }
}
