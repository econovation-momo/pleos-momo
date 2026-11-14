package app.momo.pleos

import ai.pleos.playground.navi.constants.NaviErrorCode
import ai.pleos.playground.navi.constants.NavigationStatus
import ai.pleos.playground.navi.data.*
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener
import android.content.Context

/** NaviHelper SDK 3.0.5. 내비 앱에 요청을 보내고 이벤트를 받는다. */
class NaviSignals(context: Context) {
    private val navi = NaviHelper(context.applicationContext)

    private val listener = object : NaviHelperEventListener {
        override fun onNavigationStatus(status: NavigationStatus) = SignalHub.put("Navi/상태", status)
        override fun onRouteStarted(routeStartInfo: RouteStartInfo) =
            SignalHub.put("Navi/경로 시작", "${routeStartInfo.name} ${routeStartInfo.distance}m ${routeStartInfo.duration}s 통행료 ${routeStartInfo.tollFare}")
        override fun onRoutingCompleted() = SignalHub.put("Navi/경로 탐색", "완료")
        override fun onDestinationInfo(destinationInfo: DestinationInfo) =
            SignalHub.put("Navi/목적지", "${destinationInfo.name} 남은 ${destinationInfo.remainDistance}m / ${destinationInfo.remainTime}s")
        override fun onDestinationArrived(destinationArrivedInfo: DestinationArrivedInfo) =
            SignalHub.put("Navi/도착", destinationArrivedInfo.name)
        override fun onRouteCancelled() = SignalHub.put("Navi/경로 탐색", "취소됨")
        override fun onTBTInfo(tbtInfoList: List<TBTInfo>) =
            SignalHub.put("Navi/다음 회전", tbtInfoList.firstOrNull()?.let { "${it.type} ${it.distance.toInt()}m" } ?: "없음")
        override fun onCurrentRoadSpeedLimit(speedLimit: Int) = SignalHub.put("Navi/제한속도", speedLimit)
        override fun onCameraAlert(cameraAlertList: List<CameraAlert>) =
            SignalHub.put("Navi/과속 카메라", cameraAlertList.joinToString { "${it.type} ${it.remainDistance}m" }.ifEmpty { "없음" })
        override fun onCautionPointInfo(cautionPointList: List<CautionPoint>) =
            SignalHub.put("Navi/주의 구간", cautionPointList.joinToString { "${it.type} ${it.remainDistance}m" }.ifEmpty { "없음" })
        override fun onCurrentLocationInfo(currentLocationInfo: CurrentLocationInfo) =
            SignalHub.put("Navi/현재 위치", "${currentLocationInfo.latitude}, ${currentLocationInfo.longitude}")
        override fun onError(errorCode: NaviErrorCode) = SignalHub.put("Navi/오류", errorCode)
    }

    fun start() {
        try {
            navi.initialize(
                onConnected = {
                    SignalHub.put("Navi/SDK", "연결됨")
                    navi.addListener(listener)
                    navi.getDestinationInfo()
                    navi.getCurrentLocationInfo()
                },
                onFailed = { SignalHub.fail("Navi/SDK", it) },
            )
        } catch (e: Exception) {
            SignalHub.fail("Navi/SDK", e)
        }
    }

    /** 공식 문서 예제 목적지(서울 강남구 남부순환로 2621)로 경로를 요청한다. */
    fun requestSampleRoute() = runCatching {
        navi.requestRoute(
            RouteInfo(
                latitude = 37.48544722,
                longitude = 127.03636666,
                displayLongitude = null,
                displayLatitude = null,
                poiName = "42dot",
                poiId = "36khp37hh8gqcm5nqhe5",
                poiSubId = "0",
                address = "서울시 강남구 남부순환로 2621",
            ),
        )
        SignalHub.log("Navi: requestRoute 호출")
    }.onFailure { SignalHub.fail("Navi/경로 요청", it) }

    fun cancelRoute() = runCatching { navi.cancelRoute() }.onFailure { SignalHub.fail("Navi/경로 취소", it) }

    fun stop() {
        runCatching { navi.removeListener(listener) }
        runCatching { navi.release() }
    }
}
