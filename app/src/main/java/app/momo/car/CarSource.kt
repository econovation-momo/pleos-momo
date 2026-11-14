package app.momo.car

import app.momo.core.SeatPos
import app.momo.core.SeatState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** 앱이 쓰는 차량 신호 묶음. 실제 SDK 구현(PleosCarSource)과 시연용 SimCarSource가 같은 모양을 낸다. */
data class CarState(
    val source: String = "시뮬레이터",       // 화면 표시용: "Pleos SDK" / "시뮬레이터"
    val driving: Boolean = false,            // CarUxRestrictions: 주행 규제 ON
    val gear: String = "P",                  // P R N D
    val destination: String? = null,         // NaviHelper DestinationInfo.name
    val remainSec: Int? = null,              // NaviHelper DestinationInfo.remainTime
    val remainMeters: Int? = null,           // NaviHelper DestinationInfo.remainDistance
    val seats: Map<SeatPos, SeatState> = DEFAULT_SEATS,
    /** 실제 SDK에서 온 항목 이름(주행·기어·목적지·좌석 이름). 비어 있으면 전부 시뮬레이터 값이다. */
    val live: Set<String> = emptySet(),
) {
    val riders get() = seats.values.count { it.occupied }
    fun isLive(item: String) = item in live

    companion object {
        val DEFAULT_SEATS = mapOf(
            SeatPos.DRIVER to SeatState(occupied = true, belted = true),
            SeatPos.PASSENGER to SeatState(occupied = false, belted = false),
            SeatPos.REAR_LEFT to SeatState(occupied = true, belted = true),
            SeatPos.REAR_RIGHT to SeatState(occupied = true, belted = true),
        )
    }
}

sealed interface CarEvent {
    data object Arrived : CarEvent          // onDestinationArrived
    data object Rerouted : CarEvent         // onReRouteEnded: 남은 시간 다시 조회
}

interface CarSource {
    val state: StateFlow<CarState>
    val events: SharedFlow<CarEvent>
    fun start()
    fun stop()
    /** NaviHelper.getDestinationInfo() 재조회. 세션 엔진이 60초마다 부른다. */
    fun refreshDestination()
}

/** 시연용. 에뮬레이터에 내비 목적지·좌석 신호가 없을 때 버튼으로 바꾼다. */
class SimCarSource : CarSource {
    private val _state = MutableStateFlow(CarState())
    override val state = _state.asStateFlow()
    private val _events = MutableSharedFlow<CarEvent>(extraBufferCapacity = 8)
    override val events = _events

    override fun start() = Unit
    override fun stop() = Unit
    override fun refreshDestination() = Unit

    fun setDestination(name: String?, remainSec: Int?, remainMeters: Int? = null) =
        _state.update { it.copy(destination = name, remainSec = remainSec, remainMeters = remainMeters) }

    fun setDriving(driving: Boolean) = _state.update { it.copy(driving = driving, gear = if (driving) "D" else "P") }
    fun setRemain(sec: Int) = _state.update { it.copy(remainSec = sec.coerceAtLeast(0)) }
    fun setSeat(seat: SeatPos, value: SeatState) = _state.update { it.copy(seats = it.seats + (seat to value)) }
    fun arrive() {
        _state.update { it.copy(remainSec = 0, driving = false, gear = "P") }
        _events.tryEmit(CarEvent.Arrived)
    }
}
