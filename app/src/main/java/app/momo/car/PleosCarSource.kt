package app.momo.car

import android.content.Context
import android.util.Log
import app.momo.core.SeatPos
import app.momo.core.SeatState
import app.momo.pleos.CarSignals
import app.momo.pleos.NaviSignals
import app.momo.pleos.VehicleSignals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 실제 Pleos SDK 신호와 시뮬레이터 값을 항목별로 합친다.
 * - 실제 SDK 모드: SDK가 값을 준 항목은 SDK 값, 못 준 항목은 시뮬레이터 값을 쓴다.
 *   시연 패널에서 특정 항목을 직접 바꾸면 그 항목만 시뮬레이터 값으로 덮는다(overrides).
 * - 시뮬레이터 모드: 전부 시뮬레이터 값을 쓴다.
 * 어떤 항목이 SDK에서 왔는지는 CarState.live로 화면에 보여 줄 수 있다.
 */
class PleosCarSource(context: Context, val sim: SimCarSource, private val scope: CoroutineScope) : CarSource {
    private val app = context.applicationContext
    private val carSignals = CarSignals(app)
    private val navi = NaviSignals(app)
    private val vehicle = VehicleSignals(app)

    val useSdk = MutableStateFlow(true)
    val overrides = MutableStateFlow<Set<String>>(emptySet())
    /** 시뮬레이터 남은 시간 배속: 주행 중 1초마다 이만큼 remainSec가 줄어든다. */
    val speed = MutableStateFlow(1)

    private val realEvents = MutableSharedFlow<CarEvent>(extraBufferCapacity = 8)
    override val events = merge(realEvents, sim.events).shareIn(scope, SharingStarted.Eagerly)

    private val real = combine(carSignals.driving, carSignals.gear, navi.destination, vehicle.occupied, vehicle.belted) { d, g, dest, occ, belt ->
        Real(d, g, dest?.let { Dest(it.name.orEmpty(), it.remainTime.toInt(), it.remainDistance.toInt()) }, occ, belt)
    }

    override val state: StateFlow<CarState> = combine(real, sim.state, useSdk, overrides) { r, s, sdk, ov ->
        if (!sdk) s.copy(source = "시뮬레이터", live = emptySet()) else merge(r, s, ov)
    }.stateIn(scope, SharingStarted.Eagerly, sim.state.value)

    private var started = false

    override fun start() {
        // MainActivity가 권한 허용 뒤 한 번 더 부른다. 그때는 차속·기어 구독만 다시 한다.
        carSignals.start()
        MomoService.start(app)
        if (started) return
        started = true
        navi.onArrived = { log("SDK 도착"); realEvents.tryEmit(CarEvent.Arrived) }
        navi.onRerouted = { log("SDK 재탐색 → 남은 시간 다시 받음"); realEvents.tryEmit(CarEvent.Rerouted) }
        navi.start()
        vehicle.start(seatsOnly = true)
        scope.launch { state.collect { log("상태 ${describe(it)}") } }
        scope.launch { tick() }
        // 탑승자 화면 등에서 Graph.sim으로 좌석을 바꿔도 SDK 값에 덮이지 않도록, 바뀐 좌석은 시뮬레이터 값으로 고정한다
        scope.launch {
            var prev = sim.state.value.seats
            sim.state.collect { s ->
                val changed = SeatPos.entries.filter { s.seats[it] != prev[it] }.flatMap { seatItems(it) }
                if (changed.isNotEmpty()) overrides.update { it + changed }
                prev = s.seats
            }
        }
    }

    override fun stop() {
        carSignals.stop(); navi.stop(); vehicle.stop()
        started = false
    }

    override fun refreshDestination() {
        navi.refresh()
    }

    /** 시연·확인용: 실제 내비에 예제 목적지로 경로를 요청한다(내비 앱이 앞으로 나온다). */
    fun requestSampleRoute() {
        navi.requestSampleRoute()
    }

    /** 시연 패널이 시뮬레이터 값을 바꿀 때 쓴다. 실제 SDK 모드에서는 그 항목을 시뮬레이터 값으로 고정한다. */
    fun simulate(vararg items: String, block: SimCarSource.() -> Unit) {
        sim.block()
        overrides.update { it + items }
    }

    fun setUseSdk(on: Boolean) {
        useSdk.value = on
        overrides.value = emptySet()
    }

    /** 시뮬레이터 목적지를 쓰는 동안, 주행 중이면 배속만큼 남은 시간을 줄이고 0이 되면 도착시킨다. */
    private suspend fun tick() {
        while (true) {
            delay(1000)
            val s = state.value
            val d = sim.state.value
            val simRemain = d.remainSec ?: continue
            if (!s.driving || ITEM_DEST in s.live || simRemain <= 0) continue
            val left = simRemain - speed.value
            if (left > 0) sim.setDestination(d.destination, left, d.remainMeters?.let { (it.toLong() * left / simRemain).toInt() })
            else { log("시뮬레이터 도착"); sim.arrive() }
        }
    }

    private fun merge(r: Real, s: CarState, ov: Set<String>): CarState {
        val live = mutableSetOf<String>()
        fun <T> pick(item: String, real: T?, simValue: T): T =
            if (real != null && item !in ov) { live += item; real } else simValue

        val driving = pick(ITEM_DRIVING, r.driving, s.driving)
        val gear = pick(ITEM_GEAR, r.gear, s.gear)
        val dest = pick(ITEM_DEST, r.dest, Dest(s.destination ?: "", s.remainSec ?: -1, s.remainMeters ?: -1))
        val seats = SeatPos.entries.associateWith { pos ->
            val simSeat = s.seats[pos] ?: SeatState(false, false)
            SeatState(
                occupied = pick("착좌 ${pos.short}", r.occupied[pos], simSeat.occupied),
                belted = pick("벨트 ${pos.short}", r.belted[pos], simSeat.belted),
            )
        }
        return CarState(
            source = if (live.isEmpty()) "시뮬레이터" else "Pleos SDK",
            driving = driving,
            gear = gear,
            destination = dest.name.ifEmpty { null },
            remainSec = dest.remainSec.takeIf { it >= 0 },
            remainMeters = dest.remainMeters.takeIf { it >= 0 },
            seats = seats,
            live = live,
        )
    }

    private fun describe(s: CarState) = "[${s.source}] 주행=${s.driving} 기어=${s.gear} 목적지=${s.destination} " +
        "남은=${s.remainSec}s/${s.remainMeters}m 좌석=" +
        s.seats.entries.joinToString(" ") { (p, v) -> "${p.short}:${if (v.occupied) "앉음" else "빈"}${if (v.belted) "+벨트" else ""}" } +
        " SDK=${s.live}"

    private data class Dest(val name: String, val remainSec: Int, val remainMeters: Int)
    private data class Real(
        val driving: Boolean?, val gear: String?, val dest: Dest?,
        val occupied: Map<SeatPos, Boolean>, val belted: Map<SeatPos, Boolean>,
    )

    private fun log(msg: String) { Log.i("MomoCar", msg) }

    companion object {
        const val ITEM_DRIVING = "주행"
        const val ITEM_GEAR = "기어"
        const val ITEM_DEST = "목적지"
        fun seatItems(pos: SeatPos) = listOf("착좌 ${pos.short}", "벨트 ${pos.short}")
    }
}
