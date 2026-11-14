package app.momo.pleos

import android.car.Car
import android.car.VehicleGear
import android.car.VehiclePropertyIds
import android.car.drivingstate.CarUxRestrictions
import android.car.drivingstate.CarUxRestrictionsManager
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context

/**
 * AAOS 표준 Car API.
 * - 주행 규제: CarUxRestrictionsManager (공식 주행 규제 가이드)
 * - 차속: PERF_VEHICLE_SPEED (Pleos FAQ가 안내하는 방식, CAR_SPEED 런타임 권한 필요)
 * - 기어: GEAR_SELECTION (서드파티 허용 여부는 이 화면으로 확인)
 */
class CarSignals(private val context: Context) {
    private var car: Car? = null
    private var ux: CarUxRestrictionsManager? = null
    private var props: CarPropertyManager? = null

    private val uxListener = CarUxRestrictionsManager.OnUxRestrictionsChangedListener { report(it) }

    private val propCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<*>) {
            when (value.propertyId) {
                VehiclePropertyIds.PERF_VEHICLE_SPEED ->
                    SignalHub.put("Car/차속 km/h", "%.1f".format((value.value as Float) * 3.6f))
                VehiclePropertyIds.GEAR_SELECTION ->
                    SignalHub.put("Car/기어", VehicleGear.toString(value.value as Int))
            }
        }

        override fun onErrorEvent(propertyId: Int, zone: Int) =
            SignalHub.put("Car/오류", VehiclePropertyIds.toString(propertyId))
    }

    fun start() {
        try {
            val c = Car.createCar(context)
            car = c
            ux = (c.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE) as CarUxRestrictionsManager).also {
                it.registerListener(uxListener)
                report(it.currentCarUxRestrictions)
            }
            SignalHub.put("Car/SDK", "연결됨")
        } catch (e: Exception) {
            SignalHub.fail("Car/SDK", e)
        }
        subscribeProperties()
    }

    /** CAR_SPEED는 dangerous 권한이라 허용 후 다시 부른다. */
    fun subscribeProperties() {
        val c = car ?: return
        try {
            val p = props ?: (c.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager).also { props = it }
            subscribe(p, VehiclePropertyIds.PERF_VEHICLE_SPEED, CarPropertyManager.SENSOR_RATE_NORMAL, "Car/차속 km/h")
            subscribe(p, VehiclePropertyIds.GEAR_SELECTION, CarPropertyManager.SENSOR_RATE_ONCHANGE, "Car/기어")
        } catch (e: Exception) {
            SignalHub.fail("Car/프로퍼티", e)
        }
    }

    private fun subscribe(p: CarPropertyManager, id: Int, rate: Float, key: String) {
        try {
            if (!p.registerCallback(propCallback, id, rate)) SignalHub.put(key, "구독 실패(지원 안 함)")
        } catch (e: SecurityException) {
            SignalHub.put(key, "권한 없음: ${e.message}")
        }
    }

    private fun report(r: CarUxRestrictions) {
        SignalHub.put("Car/주행 규제", if (r.isRequiresDistractionOptimization) "ON (0x${r.activeRestrictions.toString(16)})" else "OFF")
    }

    fun stop() {
        runCatching { ux?.unregisterListener() }
        runCatching { props?.unregisterCallback(propCallback) }
        runCatching { car?.disconnect() }
    }
}
