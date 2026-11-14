package app.momo.pleos

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.model.area.DoorArea
import ai.pleos.playground.vehicle.api.model.area.SeatArea
import ai.pleos.playground.vehicle.api.model.area.WindowArea
import ai.pleos.playground.vehicle.constant.*
import ai.pleos.playground.vehicle.constant.area.*
import ai.pleos.playground.vehicle.constant.control.*
import ai.pleos.playground.vehicle.listener.*
import android.content.Context

/**
 * Vehicle SDK 조회·구독. 시그니처는 SDK 2.3.2 레퍼런스 기준.
 * 지원하지 않는 항목은 onFailed로 오므로, 검증 화면에서 "실패: ..."로 보인다.
 */
class VehicleSignals(context: Context) {
    private val vehicle = Vehicle(context)
    private val cleanups = mutableListOf<() -> Unit>()

    private val seats = listOf(
        "운전석" to SeatValue.ROW_1_LEFT,
        "조수석" to SeatValue.ROW_1_RIGHT,
        "뒷좌석 왼쪽" to SeatValue.ROW_2_LEFT,
        "뒷좌석 오른쪽" to SeatValue.ROW_2_RIGHT,
    )

    fun start() {
        try {
            vehicle.initialize()
            SignalHub.put("Vehicle/SDK", "initialize() 반환")
        } catch (e: Exception) {
            SignalHub.fail("Vehicle/SDK", e)
            return
        }
        // 도메인마다 따로 감싸서, 한 곳이 실패해도 나머지는 계속 등록한다
        guard("좌석") { registerSeats() }
        guard("도어") { registerDoors() }
        guard("운전 조작") { registerDriving() }
        guard("에너지·공조·조명") { registerCabin() }
        refreshTelltales()
    }

    fun stop() {
        cleanups.forEach { runCatching(it) }
        cleanups.clear()
        runCatching { vehicle.release() }
    }

    /** 경고등은 구독이 PRIVATE 퍼미션이라 조회만 한다. */
    fun refreshTelltales() = guard("경고등") {
        vehicle.getSafety().getTelltalesInfo(
            onComplete = { t ->
                val on = t.telltales.filter { it.state.toString() != "OFF" }
                SignalHub.put("Vehicle/경고등 (OFF 제외)", if (on.isEmpty()) "없음" else on.joinToString { "${it.type}:${it.state}" })
            },
            onFailed = { SignalHub.fail("Vehicle/경고등 (OFF 제외)", it) },
        )
    }

    private fun registerSeats() {
        val seat = vehicle.getSeat()
        for ((label, value) in seats) {
            val area = SeatArea(value)
            val occupancy = object : SeatOccupancyListener {
                override fun onSeatOccupancyUpdated(occupancy: Boolean?) = SignalHub.put("Vehicle/착좌 $label", occupancy)
                override fun onFailed(e: Exception) = SignalHub.fail("Vehicle/착좌 $label", e)
            }
            seat.registerSeatOccupancy(area, occupancy)
            cleanups += { seat.unregisterSeatOccupancy(area, occupancy) }

            val belt = object : SeatBeltBuckledStateListener {
                override fun onSeatBeltBuckledStateUpdated(state: Boolean?) = SignalHub.put("Vehicle/벨트 $label", state)
                override fun onFailed(e: Exception) = SignalHub.fail("Vehicle/벨트 $label", e)
            }
            seat.registerSeatBeltBuckledState(area, belt)
            cleanups += { seat.unregisterSeatBeltBuckledState(area, belt) }
        }
        val hvac = vehicle.getHVAC()
        for ((label, value) in seats.filter { it.first.startsWith("뒷좌석") || it.first == "운전석" }) {
            val area = SeatArea(value)
            val temp = object : HvacTemperatureListener {
                override fun onHvacTemperatureUpdated(temperature: Float?) = SignalHub.put("Vehicle/실내온도 $label", temperature)
                override fun onFailed(e: Exception) = SignalHub.fail("Vehicle/실내온도 $label", e)
            }
            hvac.registerHvacTemperatureCurrent(area, temp)
            cleanups += { hvac.unregisterHvacTemperatureCurrent(area, temp) }
        }
    }

    private fun registerDoors() {
        val door = vehicle.getDoor()
        val doors = listOf(
            "운전석" to DoorValue.ROW_1_LEFT, "조수석" to DoorValue.ROW_1_RIGHT,
            "뒷좌석 왼쪽" to DoorValue.ROW_2_LEFT, "뒷좌석 오른쪽" to DoorValue.ROW_2_RIGHT,
        )
        for ((label, value) in doors) {
            val area = DoorArea(value)
            val listener = object : DoorPositionListener {
                override fun onPositionUpdated(pos: Int?) = SignalHub.put("Vehicle/도어 $label", pos)
                override fun onFailed(e: Exception) = SignalHub.fail("Vehicle/도어 $label", e)
            }
            door.registerDoorPosition(area, listener)
            cleanups += { door.unregisterDoorPosition(area, listener) }
        }
    }

    private fun registerDriving() {
        val turn = vehicle.getTurnSignal()
        val turnListener = object : TurnSignalStateListener {
            override fun onTurnSignalStateUpdated(state: TurnSignalValue?) = SignalHub.put("Vehicle/방향지시등", state)
            override fun onFailed(e: Exception) = SignalHub.fail("Vehicle/방향지시등", e)
        }
        turn.registerTurnSignalState(turnListener)
        cleanups += { turn.unregisterTurnSignalState(turnListener) }

        val brake = vehicle.getBrake()
        val brakeListener = object : BrakePedalStatusListener {
            override fun onBrakePedalStatusUpdated(status: Boolean?) = SignalHub.put("Vehicle/브레이크 밟음", status)
            override fun onFailed(e: Exception) = SignalHub.fail("Vehicle/브레이크 밟음", e)
        }
        brake.registerBrakePedalStatus(brakeListener)
        cleanups += { brake.unregisterBrakePedalStatus(brakeListener) }

        val steering = vehicle.getSteeringWheel()
        val steeringListener = object : SteeringWheelAngleListener {
            override fun onSteeringWheelAngleUpdated(angle: Float?) = SignalHub.put("Vehicle/조향각", angle)
            override fun onFailed(e: Exception) = SignalHub.fail("Vehicle/조향각", e)
        }
        steering.registerSteeringWheelAngle(steeringListener)
        cleanups += { steering.unregisterSteeringWheelAngle(steeringListener) }

        val wiper = vehicle.getWiper()
        val front = WindowArea(WindowValue.FRONT_WINDSHIELD)
        val wiperListener = object : WiperStateListener {
            override fun onWiperStateUpdated(state: WiperState?) = SignalHub.put("Vehicle/와이퍼", state)
            override fun onFailed(e: Exception) = SignalHub.fail("Vehicle/와이퍼", e)
        }
        wiper.registerWiperState(front, wiperListener)
        cleanups += { wiper.unregisterWiperState(front, wiperListener) }
    }

    private fun registerCabin() {
        val ev = vehicle.getEvBattery()
        val socListener = object : EvBatteryStateOfChargeListener {
            override fun onEvBatteryStateOfChargeUpdated(value: Int?) = SignalHub.put("Vehicle/배터리 %", value)
            override fun onFailed(e: Exception) = SignalHub.fail("Vehicle/배터리 %", e)
        }
        ev.registerEvBatteryStateOfCharge(socListener)
        cleanups += { ev.unregisterEvBatteryStateOfCharge(socListener) }

        val light = vehicle.getLight()
        val lowBeam = object : LightStateListener {
            override fun onLightStateUpdated(state: LightState?) = SignalHub.put("Vehicle/하향등", state)
            override fun onFailed(e: Exception) = SignalHub.fail("Vehicle/하향등", e)
        }
        light.registerLowBeamLightState(lowBeam)
        cleanups += { light.unregisterLowBeamLightState(lowBeam) }
    }

    private fun guard(name: String, block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            SignalHub.fail("Vehicle/등록 실패: $name", e)
        }
    }
}
