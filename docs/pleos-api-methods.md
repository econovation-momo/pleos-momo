# Pleos Connect SDK 메서드 전체 목록

document.pleos.ai의 SDK 레퍼런스 클래스 페이지를 브라우저로 렌더링해서 원문 시그니처와 `@RequiresPermission` 값을 스크립트로 그대로 옮긴 표입니다. 사람이 손으로 옮겨 적은 내용은 없습니다(추출일 2026-10-22).
퍼미션 열의 `pleos.car.permission.` 접두사는 생략했습니다. `CONTROL_`로 시작하면 제어용, `PRIVATE_`로 시작하면 서드파티 사용이 어려울 가능성이 큰 퍼미션입니다.

## FusedLocation SDK (9개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `addProximityAlert` | 제어/요청 | FUSED_LOCATION | `` | Registers a proximity alert for the specified area and transitions. |
| `getLastKnownLocation` | 조회 | FUSED_LOCATION | `` | Gets the last known location. |
| `initialize` | 기타 | - | `` | Initializes the SDK. |
| `isSupported` | 조회 | - | `Boolean` | Returns whether the Fused Location Service package is present on Pleos Connect. |
| `registerFusedLocationCallback` | 구독 | FUSED_LOCATION | `` | Registers a callback to receive location updates. |
| `release` | 기타 | - | `` | Releases the SDK. |
| `removeProximityAlert` | 제어/요청 | FUSED_LOCATION | `` | Removes a previously registered proximity alert. |
| `showLocationAccessBlockedPopup` | 기타 | - | `` | Displays a popup message informing the user that location access is blocked due to their current privacy settings. |
| `unregisterFusedLocationCallback` | 구독 | FUSED_LOCATION | `` | Stops receiving location updates through the specified listener. |

## NaviHelper SDK · NaviHelperEventListener (이벤트) (25개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `onBookMarkChanged` | 기타 | - | `` | Called when arriving at or removing the bookmark. |
| `onBookmarkInfo` | 기타 | - | `` | Called when the bookmark information is changed. |
| `onCameraAlert` | 기타 | - | `` | Called when camera alerts are notified. Received when speeding camera exists. |
| `onCautionPointInfo` | 기타 | - | `` | Called when caution point information is notified. |
| `onChargerOperatorInfo` | 기타 | - | `` | Called when charger operator information is notified. |
| `onCurrentLocationInfo` | 기타 | - | `` | Called when the current location information is changed. |
| `onCurrentRoadSpeedLimit` | 기타 | - | `` | Called when the road speed limit is notified. Received every second after the event listener registration. |
| `onDestinationArrived` | 기타 | - | `` | Called when arriving at the destination. |
| `onDestinationChanged` | 기타 | - | `` | Called when arriving at or removing the destination. |
| `onDestinationInfo` | 기타 | - | `` | Called when the destination information is changed. |
| `onDrivingInfo` | 기타 | - | `` | Called when driving information is notified. |
| `onError` | 기타 | - | `` | Called when a navigation error occurs. |
| `onNavigationStatus` | 기타 | - | `` | Called when the navigation status is notified. Received whenever the driving status changes |
| `onRecentDestinationInfo` | 기타 | - | `` | Called when the recent destination information is changed. |
| `onReRouteEnded` | 기타 | - | `` | Called when navigation re-route is completed. |
| `onRouteCancelled` | 기타 | - | `` | Called when the current route is canceled. |
| `onRouteOptionChanged` | 기타 | - | `` | Called when the route type is changed. |
| `onRouteStarted` | 기타 | - | `` | Called when navigation route search begins. |
| `onRouteStateInfo` | 기타 | - | `` | Called when the route state information is changed. |
| `onRoutingCompleted` | 기타 | - | `` | Called when navigation route search is completed. |
| `onTBTInfo` | 기타 | - | `` | Called when the TBT information is notified. |
| `onTripInfo` | 기타 | - | `` | Called when trip information is notified. |
| `onWayPointArrived` | 기타 | - | `` | Called when arriving at the waypoint. |
| `onWaypointChanged` | 기타 | - | `` | Called when arriving at or removing the waypoint. |
| `onWayPointInfo` | 기타 | - | `` | Called when the waypoint information is changed. |

## NaviHelper SDK · NaviHelper (21개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `addListener` | 구독 | - | `` | Adds a listener. Registers a listener for NaviHelper callback function |
| `addWaypoint` | 제어/요청 | NAVI_ROUTE_SEARCH | `` | Adds waypoint. |
| `cancelRoute` | 제어/요청 | NAVI_ROUTE_SEARCH | `` | Cancels route. |
| `changeRouteOption` | 제어/요청 | NAVI_ROUTE_SEARCH | `` | Changes route option. |
| `getBookmarkInfo` | 조회 | NAVI_ROUTE | `` | Gets bookmark info. |
| `getChargerOperatorInfo` | 조회 | NAVI_ROUTE | `` | Gets Charger Operator info. |
| `getCurrentLocationInfo` | 조회 | NAVI_ROUTE | `` | Gets current location info. |
| `getDestinationInfo` | 조회 | NAVI_ROUTE | `` | Gets destination info. |
| `getRecentDestinationInfo` | 조회 | NAVI_ROUTE | `` | Gets recent destination info. |
| `getRouteStateInfo` | 조회 | NAVI_ROUTE | `` | Gets route state info. |
| `getTBTInfo` | 조회 | NAVI_ROUTE | `` | Gets TBT info. |
| `getWaypointInfo` | 조회 | NAVI_ROUTE | `` | Gets waypoint info. |
| `initialize` | 기타 | - | `` | Initializes NaviHelper SDK. When the app starts or is created, it should call the initialization of SDK. Initializes NaviHelper SDK with con |
| `release` | 기타 | - | `` | Releases NaviHelper SDK. When the app terminates or is destroyed, it should call the release of SDK |
| `removeListener` | 구독 | - | `` | Removes a listener. |
| `removeWaypoint` | 제어/요청 | NAVI_ROUTE_SEARCH | `` | Removes waypoint. |
| `requestNavigationScreen` | 제어/요청 | NAVI_CUSTOM_MAP | `` | Requests navigation screen. |
| `requestReRoute` | 제어/요청 | NAVI_ROUTE_SEARCH | `` | Requests re-route. |
| `requestRoute` | 제어/요청 | NAVI_ROUTE_SEARCH | `` | Requests route. |
| `requestRouteWithWaypoints` | 제어/요청 | NAVI_ROUTE_SEARCH | `` | Requests a route with waypoints. |
| `requestTrip` | 제어/요청 | NAVI_ROUTE | `` | Requests trip. |

## SpeechToText SDK (10개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `addListener` | 구독 | STT_SERVICE | `` | Adds a listener that will be notified of various events related to SpeechToText Requires the caller to have STT_PERMISSION. |
| `completeAudioSend` | 제어/요청 | STT_SERVICE | `` | Completes the external audio buffer input started by sendAudio and finishes recognition. Available only in Mode.ON_DEVICE. In other modes th |
| `initialize` | 기타 | STT_SERVICE | `` | Initializes SDK to use SpeechToText whether the application is created or not. Requires the caller to have STT_PERMISSION. Initializes SDK t |
| `registerApp` | 구독 | STT_SERVICE | `` | Registers the application to use the server-oriented or hybrid SpeechToText service. Requires the caller to have STT_PERMISSION. |
| `release` | 기타 | STT_SERVICE | `` | Releases SDK whether the application is destroyed or not. Requires the caller to have STT_PERMISSION. |
| `removeListener` | 구독 | STT_SERVICE | `` | Removes a previously registered listener Requires the caller to have STT_PERMISSION. |
| `request` | 제어/요청 | STT_SERVICE | `` | Requests for starting speech recognition. After the application invoke request(), microphone will be open, and then you can stream your audi |
| `sendAudio` | 제어/요청 | STT_SERVICE | `` | Sends an external audio buffer (PCM) for recognition. Available only in Mode.ON_DEVICE. In other modes the call is ignored and ResultListene |
| `setLocale` | 제어/요청 | - | `` | Updates the locale that the user wants to set. Requires the caller to have STT_PERMISSION. |
| `stop` | 제어/요청 | STT_SERVICE | `` | Stops speech recognition. After the application invoke stop(), the microphone will be disabled. |

## TextToSpeech SDK (11개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `addEventListener` | 구독 | TTS_SERVICE | `` | Adds a listener that will be notified of various events related to TextToSpeech |
| `initialize` | 기타 | TTS_SERVICE | `` | Initializes TextToSpeech SDK When the app starts or is created, it should call the initialization of SDK Initializes TextToSpeech SDK with a |
| `registerApp` | 구독 | TTS_SERVICE | `` | Register app |
| `release` | 기타 | TTS_SERVICE | `` | Releases TextToSpeech SDK When the app terminates or is destroyed, it should call the release of SDK |
| `removeEventListener` | 구독 | TTS_SERVICE | `` | Removes a previously registered listener |
| `setAudioAttributes` | 제어/요청 | TTS_SERVICE | `` | Sets the audio attributes to be used for TextToSpeech SDK |
| `setAudioZoneId` | 제어/요청 | TTS_SERVICE | `` | Update audio zone id that user want to set. Requires the caller to have TTS_PERMISSION. |
| `setLocale` | 제어/요청 | - | `` | Updates the locale that the user wants to set. Requires the caller to have TTS_PERMISSION. |
| `setSpeakerType` | 제어/요청 | - | `` | Updates the speaker type (voice) used for speech synthesis. Requires the caller to have TTS_PERMISSION. If this method is not called, the sp |
| `speak` | 제어/요청 | TTS_SERVICE | `` | Converts Text-To-Speech and speaks the text |
| `stop` | 제어/요청 | TTS_SERVICE | `` | Interrupts the current TextToSpeech playback |

## Vehicle SDK · brake (4개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `checkBrakePedalStatusCapabilities` | 지원 확인 | CAR_DRIVE | `CapabilityConfig` | Gets the brake pedal status capabilities. |
| `getCurrentBrakePedalStatus` | 조회 | CAR_DRIVE | `Boolean?` | Gets the brake pedal status. If true, the brake pedal is being pressed. |
| `registerBrakePedalStatus` | 구독 | CAR_DRIVE | `` | Registers a listener for brake pedal status property updates. |
| `unregisterBrakePedalStatus` | 구독 | CAR_DRIVE | `` | Unregisters the listener so brake pedal status property updates stop. |

## Vehicle SDK · car-info (4개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `checkCRNInfoCapabilities` | 지원 확인 | CAR_INFO | `CapabilityConfig` | Gets CRN info capabilities. |
| `checkVINInfoCapabilities` | 지원 확인 | CAR_INFO | `CapabilityConfig` | Gets VIN info capabilities. |
| `getCRNInfo` | 조회 | CAR_INFO | `String?` | Gets the Connected Resource Name (CRN) Info. |
| `getVINInfo` | 조회 | CAR_INFO | `String?` | Gets the Vehicle Identification Number (VIN). |

## Vehicle SDK · display (4개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `checkTemperatureDisplayUnitCapabilities` | 지원 확인 | CAR_INFO | `CapabilityConfig` | Gets temperature display unit capabilities. |
| `getCurrentTemperatureDisplayUnit` | 조회 | CAR_INFO | `TemperatureUnit?` | Gets current temperature units for display. |
| `registerTemperatureDisplayUnit` | 구독 | CAR_INFO | `` | Registers TemperatureDisplayUnit to get property updates. |
| `unregisterTemperatureDisplayUnit` | 구독 | CAR_INFO | `` | Stops getting property updates for the given TemperatureDisplayUnit. |

## Vehicle SDK · door (15개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `checkDoorLockCapabilities` | 지원 확인 | CAR_DOORS | `List<DoorArea>` | Gets DoorLock capabilities. |
| `checkDoorPositionCapabilities` | 지원 확인 | CAR_DOORS | `Map<DoorArea, AreaBounds<Int>>` | Gets DoorPosition capabilities. |
| `checkGloveBoxLockCapabilities` | 지원 확인 | CAR_DOORS | `CapabilityConfig` | Gets the glove box lock capabilities. |
| `getCurrentDoorPosition` | 조회 | CAR_DOORS | `Int?` | Gets the current door position state. |
| `getCurrentGloveBoxLockState` | 조회 | CAR_DOORS | `Boolean?` | Gets the current glove lock state. |
| `getDoorLockState` | 조회 | CAR_DOORS | `Boolean?` | Gets current door lock state. |
| `registerDoorLockState` | 구독 | CAR_DOORS | `` | Registers the door lock state to get property updates. |
| `registerDoorPosition` | 구독 | CAR_DOORS | `` | Registers the door area to get property updates. |
| `registerGloveBoxLockState` | 구독 | CAR_DOORS | `` | Registers the glove box lock state to get property updates. |
| `setDoorLock` | 제어/요청 | CONTROL_CAR_DOORS | `DoorArea, Boolean?` | Sets the current door lock state. |
| `setDoorPosition` | 제어/요청 | CONTROL_CAR_DOORS | `DoorArea, Int?` | Sets the current door position state. The maximum (fully open) value is vehicle-dependent and is not fixed. Call checkDoorPositionCapabiliti |
| `setGloveBoxLock` | 제어/요청 | CONTROL_CAR_DOORS | `Boolean?` | Sets the current glove lock state. |
| `unregisterDoorLockState` | 구독 | CAR_DOORS | `` | Stops getting property updates for the given door lock state. |
| `unregisterDoorPosition` | 구독 | CAR_DOORS | `` | Stops getting property updates for the given door area. |
| `unregisterGloveBoxLockState` | 구독 | CAR_DOORS | `` | Stops getting property updates for the given glove box lock state. |

## Vehicle SDK · driving-mode (12개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `checkCruiseControlStateCapabilities` | 지원 확인 | CAR_DRIVE | `CapabilityConfig` | Gets CruiseControlState capabilities. |
| `checkCruiseControlTargetSpeedCapabilities` | 지원 확인 | CAR_DRIVE | `CapabilityConfig` | Gets CruiseControlTargetSpeed capabilities. |
| `checkDrivingModeStateCapabilities` | 지원 확인 | CAR_DRIVE | `CapabilityConfig` | Gets DrivingModeState capabilities. |
| `getCruiseControlState` | 조회 | CAR_DRIVE | `CruiseControl?` | Gets the current cruise control state. |
| `getCruiseControlTargetSpeed` | 조회 | CAR_DRIVE | `Float?` | Gets the current cruise control target speed in meters per second (m/s). |
| `getCurrentDrivingModeState` | 조회 | CAR_DRIVE | `DrivingModeType?` | Gets current driving mode state. |
| `registerCruiseControlState` | 구독 | CAR_DRIVE | `` | Registers CruiseControlState to get property updates. |
| `registerCruiseControlTargetSpeed` | 구독 | CAR_DRIVE | `` | Registers a listener for cruise control target speed updates in meters per second (m/s). |
| `registerDrivingModeState` | 구독 | CAR_DRIVE | `` | Registers DrivingModeState to get property updates. |
| `unregisterCruiseControlState` | 구독 | CAR_DRIVE | `` | Stops getting property updates for the given CruiseControlState. |
| `unregisterCruiseControlTargetSpeed` | 구독 | CAR_DRIVE | `` | Stops getting property updates for the given CruiseControlTargetSpeed. |
| `unregisterDrivingModeState` | 구독 | CAR_DRIVE | `` | Stops getting property updates for the given DrivingModeState. |

## Vehicle SDK · ev-battery (30개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `checkCumulativeEvEfficiencyCapabilities` | 지원 확인 | CAR_ENERGY | `CapabilityConfig` | Gets Cumulative EV Efficiency capabilities. |
| `checkCurrentEvEfficiencyCapabilities` | 지원 확인 | CAR_ENERGY | `CapabilityConfig` | Gets Current EV Efficiency capabilities. |
| `checkEvBatteryStateOfChargeCapabilities` | 지원 확인 | CAR_ENERGY | `CapabilityConfig` | Gets EV Battery State Of Charge capabilities. |
| `checkEvChargeCurrentDrawLimitCapabilities` | 지원 확인 | CAR_ENERGY | `List<Float>` | Gets the supported EV charge current draw limits in amperes (A). Gets capabilities for EV charge current draw limits in amperes (A). |
| `checkEvChargePercentLimitCapabilities` | 지원 확인 | CAR_ENERGY | `CapabilityConfig, List<Float>` | Gets capabilities for EV charge limits, each expressed as a percentage (%). |
| `checkEvChargePortOpenCapabilities` | 지원 확인 | CAR_ENERGY | `CapabilityConfig` | Gets EvChargePortOpen capabilities. |
| `checkEvChargeSwitchCapabilities` | 지원 확인 | CAR_ENERGY | `CapabilityConfig` | Gets EvChargeSwitch capabilities. |
| `checkInstantaneousEvEfficiencyCapabilities` | 지원 확인 | CAR_ENERGY | `CapabilityConfig` | Gets Instantaneous EV Efficiency capabilities. |
| `checkSinceChargeEvEfficiencyCapabilities` | 지원 확인 | CAR_ENERGY | `CapabilityConfig` | Gets EV Efficiency Since Charge capabilities. |
| `getCumulativeEvEfficiency` | 조회 | CAR_ENERGY | `Float?` | Gets cumulative EV efficiency. |
| `getCurrentEvEfficiency` | 조회 | CAR_ENERGY | `Float?` | Gets current EV efficiency. |
| `getEvBatteryStateOfCharge` | 조회 | CAR_ENERGY | `Int?` | Gets EV battery state of charge as a percentage (%). |
| `getEvChargeCurrentDrawLimit` | 조회 | CAR_ENERGY | `Float?` | Gets the EV charge current draw limit in amperes (A). |
| `getEvChargeSwitchState` | 조회 | CAR_ENERGY | `Boolean?` | Gets the current EV battery charging state. |
| `getInstantaneousEvEfficiency` | 조회 | CAR_ENERGY | `Float?` | Gets instantaneous EV efficiency. |
| `getSinceChargeEvEfficiency` | 조회 | CAR_ENERGY | `Float?` | Gets EV efficiency since charge. |
| `registerCumulativeEvEfficiency` | 구독 | CAR_ENERGY | `` | Registers Cumulative EV Efficiency listener. |
| `registerCurrentEvEfficiency` | 구독 | CAR_ENERGY | `` | Registers Current EV Efficiency listener. |
| `registerEvBatteryStateOfCharge` | 구독 | CAR_ENERGY | `` | Registers a listener for EV battery state of charge updates expressed as a percentage (%). |
| `registerInstantaneousEvEfficiency` | 구독 | CAR_ENERGY | `` | Registers Instantaneous EV Efficiency listener. |
| `registerSinceChargeEvEfficiency` | 구독 | CAR_ENERGY | `` | Registers EV Efficiency Since Charge listener. |
| `setEvChargeCurrentDrawLimit` | 제어/요청 | CONTROL_CAR_ENERGY | `Float?` | Sets the EV charge current draw limit in amperes (A). |
| `setEvChargePercentLimit` | 제어/요청 | CONTROL_CAR_ENERGY | `Float?` | Sets the battery state-of-charge limit for EV charging, expressed as a percentage (%). |
| `setEvChargePortOpen` | 제어/요청 | CONTROL_CAR_ENERGY | `Boolean?` | Sets the EV charge port open state to On or Off. |
| `setEvChargeSwitch` | 제어/요청 | CONTROL_CAR_ENERGY | `Boolean?` | Sets whether to start or stop EV battery charging. |
| `unregisterCumulativeEvEfficiency` | 구독 | CAR_ENERGY | `` | Unregisters Cumulative EV Efficiency listener. |
| `unregisterCurrentEvEfficiency` | 구독 | CAR_ENERGY | `` | Unregisters Current EV Efficiency listener. |
| `unregisterEvBatteryStateOfCharge` | 구독 | CAR_ENERGY | `` | Unregisters EV Battery State Of Charge listener. |
| `unregisterInstantaneousEvEfficiency` | 구독 | CAR_ENERGY | `` | Unregisters Instantaneous EV Efficiency listener. |
| `unregisterSinceChargeEvEfficiency` | 구독 | CAR_ENERGY | `` | Unregisters EV Efficiency Since Charge listener. |

## Vehicle SDK · fuel (20개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `checkCumulativeFuelEconomyCapabilities` | 지원 확인 | CAR_ENERGY | `CapabilityConfig` | Gets Cumulative Fuel Economy capabilities. |
| `checkCurrentFuelEconomyCapabilities` | 지원 확인 | CAR_ENERGY | `CapabilityConfig` | Gets Current Fuel Economy capabilities. |
| `checkFuelPercentCapabilities` | 지원 확인 | CAR_ENERGY | `CapabilityConfig` | Gets Fuel Percent capabilities. |
| `checkInstantaneousFuelEconomyCapabilities` | 지원 확인 | CAR_ENERGY | `CapabilityConfig` | Gets Instantaneous Fuel Economy capabilities. |
| `checkSinceChargeFuelEconomyCapabilities` | 지원 확인 | CAR_ENERGY | `CapabilityConfig` | Gets Fuel Economy Since Refuel capabilities. |
| `getCumulativeFuelEconomy` | 조회 | CAR_ENERGY | `Float?` | Gets cumulative fuel economy. |
| `getCurrentFuelEconomy` | 조회 | CAR_ENERGY | `Float?` | Gets current fuel economy. |
| `getFuelPercent` | 조회 | CAR_ENERGY | `Int?` | Gets the fuel level as a percentage (%). |
| `getInstantaneousFuelEconomy` | 조회 | CAR_ENERGY | `Float?` | Gets instantaneous fuel economy. |
| `getSinceChargeFuelEconomy` | 조회 | CAR_ENERGY | `Float?` | Gets fuel economy since refuel. |
| `registerCumulativeFuelEconomy` | 구독 | CAR_ENERGY | `` | Registers Cumulative Fuel Economy listener. |
| `registerCurrentFuelEconomy` | 구독 | CAR_ENERGY | `` | Registers Current Fuel Economy listener. |
| `registerFuelPercent` | 구독 | CAR_ENERGY | `` | Registers a listener for fuel level updates expressed as a percentage (%). |
| `registerInstantaneousFuelEconomy` | 구독 | CAR_ENERGY | `` | Registers Instantaneous Fuel Economy listener. |
| `registerSinceChargeFuelEconomy` | 구독 | CAR_ENERGY | `` | Registers Fuel Economy Since Refuel listener. |
| `unregisterCumulativeFuelEconomy` | 구독 | CAR_ENERGY | `` | Unregisters Cumulative Fuel Economy listener. |
| `unregisterCurrentFuelEconomy` | 구독 | CAR_ENERGY | `` | Unregisters Current Fuel Economy listener. |
| `unregisterFuelPercent` | 구독 | CAR_ENERGY | `` | Unregisters Fuel Percent listener. |
| `unregisterInstantaneousFuelEconomy` | 구독 | CAR_ENERGY | `` | Unregisters Instantaneous Fuel Economy listener. |
| `unregisterSinceChargeFuelEconomy` | 구독 | CAR_ENERGY | `` | Unregisters Fuel Economy Since Refuel listener. |

## Vehicle SDK · h-v-a-c (67개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `checkHvacAcCapabilities` | 지원 확인 | CAR_CLIMATE | `List<SeatArea>` | Gets HvacAC capabilities. |
| `checkHvacAirCleanerCapabilities` | 지원 확인 | CAR_CLIMATE | `List<SeatArea>` | Gets HVAC air cleaner capabilities. |
| `checkHvacAutoCapabilities` | 지원 확인 | CAR_CLIMATE | `List<SeatArea>` | Gets HvacAuto capabilities. |
| `checkHvacAutoRecirculationCapabilities` | 지원 확인 | CAR_CLIMATE | `List<SeatArea>` | Gets HVAC auto recirculation capabilities. |
| `checkHvacDefrosterCapabilities` | 지원 확인 | CAR_CLIMATE | `List<WindowArea>` | Gets HVAC defroster capabilities. |
| `checkHvacDualCapabilities` | 지원 확인 | CAR_CLIMATE | `List<SeatArea>` | Gets HVAC dual capabilities. |
| `checkHvacFanDirectionCapabilities` | 지원 확인 | CAR_CLIMATE | `Map<SeatArea, List<FanDirectionType>>` | Gets HVAC fan direction capabilities. |
| `checkHvacFanSpeedCapabilities` | 지원 확인 | CAR_CLIMATE | `Map<SeatArea, AreaBounds<Int>>` | Gets HVAC fan speed capabilities. |
| `checkHvacMaxAcCapabilities` | 지원 확인 | CAR_CLIMATE | `List<SeatArea>` | Gets HVAC max AC capabilities. |
| `checkHvacMaxDefrostCapabilities` | 지원 확인 | CAR_CLIMATE | `List<SeatArea>` | Gets HVAC max defrost capabilities. |
| `checkHvacPowerCapabilities` | 지원 확인 | CAR_CLIMATE | `List<SeatArea>` | Gets HVAC power capabilities. |
| `checkHvacRecirculationCapabilities` | 지원 확인 | CAR_CLIMATE | `List<SeatArea>` | Gets HVAC recirculation capabilities. |
| `checkHvacTemperatureCapabilities` | 지원 확인 | CAR_CLIMATE | `Map<SeatArea, AreaBounds<Float>>` | Gets the supported target temperature ranges in degrees Celsius (°C). Gets HVAC temperature range capabilities as AreaBounds in degrees Cels |
| `checkHvacTemperatureCurrentCapabilities` | 지원 확인 | CAR_CLIMATE | `CapabilityConfig, List<SeatArea>` | Gets HVAC temperature current capabilities. |
| `getHvacAcState` | 조회 | CAR_CLIMATE | `Boolean?` | Gets the current AC state. Permission: PleosPermission.CAR_CLIMATE (protection level: normal) |
| `getHvacAirCleanerState` | 조회 | CAR_CLIMATE | `Boolean?` | Gets the current air cleaner state. Permission: PleosPermission.CAR_CLIMATE (protection level: normal) |
| `getHvacAutoRecirculationState` | 조회 | CAR_CLIMATE | `Boolean?` | Gets the current automatic recirculation. Permission: PleosPermission.CAR_CLIMATE (protection level: normal) |
| `getHvacAutoState` | 조회 | CAR_CLIMATE | `Boolean?` | Gets the current automatic mode. Permission: PleosPermission.CAR_CLIMATE (protection level: normal) |
| `getHvacDefrosterState` | 조회 | CAR_CLIMATE | `Boolean?` | Gets the current defroster state. Permission: PleosPermission.CAR_CLIMATE (protection level: normal) |
| `getHvacDualState` | 조회 | CAR_CLIMATE | `Boolean?` | Gets the current temperature coupling state between areas. Permission: PleosPermission.CAR_CLIMATE (protection level: normal) |
| `getHvacFanDirection` | 조회 | CAR_CLIMATE | `FanDirectionType?` | Gets the current fan direction. Permission: PleosPermission.CAR_CLIMATE (protection level: normal) |
| `getHvacFanSpeed` | 조회 | CAR_CLIMATE | `Int?` | Gets the current fan speed. Permission: PleosPermission.CAR_CLIMATE (protection level: normal) |
| `getHvacMaxAcState` | 조회 | CAR_CLIMATE | `Boolean?` | Gets the current max AC state. Permission: PleosPermission.CAR_CLIMATE (protection level: normal) |
| `getHvacMaxDefrostState` | 조회 | CAR_CLIMATE | `Boolean?` | Gets the current max defrost state. Permission: PleosPermission.CAR_CLIMATE (protection level: normal) |
| `getHvacPowerState` | 조회 | CAR_CLIMATE | `Boolean?` | Gets the current global power state. Permission: PleosPermission.CAR_CLIMATE (protection level: normal) |
| `getHvacRecirculationState` | 조회 | CAR_CLIMATE | `Boolean?` | Gets the current recirculation state. Permission: PleosPermission.CAR_CLIMATE (protection level: normal) |
| `getHvacTemperature` | 조회 | CAR_CLIMATE | `Float?` | Gets the target temperature in the specified unit. Permission: PleosPermission.CAR_CLIMATE (protection level: normal) |
| `getHvacTemperatureCurrent` | 조회 | CAR_CLIMATE | `Float?` | Gets the current internal temperature of the vehicle in degrees Celsius (°C). Gets the current internal temperature of the vehicle in the re |
| `registerHvacAcState` | 구독 | CAR_CLIMATE | `` | Registers the HvacAC state to get property updates. |
| `registerHvacAirCleanerState` | 구독 | CAR_CLIMATE | `` | Registers the HvacAirCleaner state to get property updates. |
| `registerHvacAutoRecirculationState` | 구독 | CAR_CLIMATE | `` | Registers the HvacAutoRecirculation state to get property updates. |
| `registerHvacAutoState` | 구독 | CAR_CLIMATE | `` | Registers the HvacAuto state to get property updates. |
| `registerHvacDefrosterState` | 구독 | CAR_CLIMATE | `` | Registers the HvacDefroster state to get property updates. |
| `registerHvacDualState` | 구독 | CAR_CLIMATE | `` | Registers the HvacDual state to get property updates. |
| `registerHvacFanSpeed` | 구독 | CAR_CLIMATE | `` | Registers the HvacFanSpeed to get property updates. |
| `registerHvacMaxAcState` | 구독 | CAR_CLIMATE | `` | Registers the HvacMaxAc state to get property updates. |
| `registerHvacMaxDefrostState` | 구독 | CAR_CLIMATE | `` | Registers the HvacMaxDefrost state to get property updates. |
| `registerHvacPowerState` | 구독 | CAR_CLIMATE | `` | Registers the HvacPower state to get property updates. |
| `registerHvacRecirculationState` | 구독 | CAR_CLIMATE | `` | Registers the HvacRecirculation to get property updates. |
| `registerHvacTemperature` | 구독 | CAR_CLIMATE | `` | Registers a listener for target temperature updates in degrees Celsius (°C) only. Registers the HvacTemperature to get property updates in d |
| `registerHvacTemperatureCurrent` | 구독 | CAR_CLIMATE | `` | Registers a listener for current interior temperature updates in degrees Celsius (°C). Registers HvacTemperatureCurrent updates in degrees C |
| `setHvacAc` | 제어/요청 | CONTROL_CAR_CLIMATE | `SeatArea, Boolean?` | Sets AC to On/Off for the designated areaId. Permission: PleosPermission.CONTROL_CAR_CLIMATE (protection level: normal) |
| `setHvacAirCleaner` | 제어/요청 | CONTROL_CAR_CLIMATE | `SeatArea, Boolean?` | Sets the air cleaner to On/Off. Permission: PleosPermission.CONTROL_CAR_CLIMATE (protection level: normal) |
| `setHvacAuto` | 제어/요청 | CONTROL_CAR_CLIMATE | `SeatArea, Boolean?` | Sets automatic mode to On/Off. Permission: PleosPermission.CONTROL_CAR_CLIMATE (protection level: normal) |
| `setHvacAutoRecirculation` | 제어/요청 | CONTROL_CAR_CLIMATE | `SeatArea, Boolean?` | Sets the automatic recirculation to On/Off. Permission: PleosPermission.CONTROL_CAR_CLIMATE (protection level: normal) |
| `setHvacDefroster` | 제어/요청 | CONTROL_CAR_CLIMATE | `WindowArea, Boolean?` | Sets the defroster to On/Off. Permission: PleosPermission.CONTROL_CAR_CLIMATE (protection level: normal) |
| `setHvacDual` | 제어/요청 | CONTROL_CAR_CLIMATE | `SeatArea, Boolean?` | Enable temperature coupling between areas. Permission: PleosPermission.CONTROL_CAR_CLIMATE (protection level: normal) |
| `setHvacFanDirection` | 제어/요청 | CONTROL_CAR_CLIMATE | `SeatArea, FanDirectionType?` | Sets fan direction. Permission: PleosPermission.CONTROL_CAR_CLIMATE (protection level: normal) |
| `setHvacFanSpeed` | 제어/요청 | CONTROL_CAR_CLIMATE | `SeatArea, Int?` | Sets the fan speed. The supported fan speed range is vehicle-dependent and is not fixed. Call checkHvacFanSpeedCapabilities first. That API' |
| `setHvacMaxAc` | 제어/요청 | CONTROL_CAR_CLIMATE | `SeatArea, Boolean?` | Sets the max AC to On/Off. Permission: PleosPermission.CONTROL_CAR_CLIMATE (protection level: normal) |
| `setHvacMaxDefrost` | 제어/요청 | CONTROL_CAR_CLIMATE | `SeatArea, Boolean?` | Sets the max defrost to On/Off. Permission: PleosPermission.CONTROL_CAR_CLIMATE (protection level: normal) |
| `setHvacPower` | 제어/요청 | CONTROL_CAR_CLIMATE | `SeatArea, Boolean?` | Sets the global power state to On/Off. Permission: PleosPermission.CONTROL_CAR_CLIMATE (protection level: normal) |
| `setHvacRecirculation` | 제어/요청 | CONTROL_CAR_CLIMATE | `SeatArea, Boolean?` | Sets recirculation to On/Off. Permission: PleosPermission.CONTROL_CAR_CLIMATE (protection level: normal) |
| `setHvacTemperature` | 제어/요청 | CONTROL_CAR_CLIMATE | `SeatArea, Float?` | Sets the target temperature in the specified unit. The supported temperature range is vehicle-dependent and is not fixed. Call checkHvacTemp |
| `unregisterHvacAcState` | 구독 | CAR_CLIMATE | `` | Stops getting property updates for the given HvacAC state. |
| `unregisterHvacAirCleanerState` | 구독 | CAR_CLIMATE | `` | Stops getting property updates for the given HvacAirCleaner state. |
| `unregisterHvacAutoRecirculationState` | 구독 | CAR_CLIMATE | `` | Stops getting property updates for the given HvacAutoRecirculation state. |
| `unregisterHvacAutoState` | 구독 | CAR_CLIMATE | `` | Stops getting property updates for the given HvacAuto state. |
| `unregisterHvacDefrosterState` | 구독 | CAR_CLIMATE | `` | Stops getting property updates for the given HvacDefroster state. |
| `unregisterHvacDualState` | 구독 | CAR_CLIMATE | `` | Stops getting property updates for the given HvacDual state. |
| `unregisterHvacFanSpeed` | 구독 | CAR_CLIMATE | `` | Stops getting property updates for the given HvacFanSpeed. |
| `unregisterHvacMaxAcState` | 구독 | CAR_CLIMATE | `` | Stop getting property updates for the given HvacMaxAc state. |
| `unregisterHvacMaxDefrostState` | 구독 | CAR_CLIMATE | `` | Stops getting property updates for the given HvacMaxDefrost state. |
| `unregisterHvacPowerState` | 구독 | CAR_CLIMATE | `` | Stops getting property updates for the given HvacPower state. |
| `unregisterHvacRecirculationState` | 구독 | CAR_CLIMATE | `` | Stops getting property updates for the given HvacRecirculation. |
| `unregisterHvacTemperature` | 구독 | CAR_CLIMATE | `` | Stops getting property updates for the given HvacTemperature. Stops getting HVAC temperature updates for the given listener that receives bo |
| `unregisterHvacTemperatureCurrent` | 구독 | CAR_CLIMATE | `` | Stops getting property updates for the given HvacTemperatureCurrent. |

## Vehicle SDK · light (71개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `checkAmbientColorAutoByDriveModeEnabledCapabilities` | 지원 확인 | CAR_LIGHTS | `CapabilityConfig` | Gets Ambient Color Auto By Drive Mode Enabled capabilities. |
| `checkAmbientLightBrightnessCapabilities` | 지원 확인 | CAR_LIGHTS | `CapabilityConfig, Map<SeatArea, AreaBounds<Int>>` | Gets ambient light brightness capabilities. |
| `checkAmbientLightBrightnessColorCapabilities` | 지원 확인 | CAR_LIGHTS | `CapabilityConfig, List<SeatArea>` | Gets ambient light brightness color capabilities. |
| `checkAmbientLightStateCapabilities` | 지원 확인 | CAR_LIGHTS | `CapabilityConfig, Map<SeatArea, List<LightState>>` | Gets ambient light state capabilities. |
| `checkAmbientLightSwitchCapabilities` | 지원 확인 | CAR_LIGHTS | `CapabilityConfig, Map<SeatArea, List<LightSwitch>>` | Gets ambient light switch capabilities. |
| `checkCabinLightStateCapabilities` | 지원 확인 | CAR_LIGHTS | `CapabilityConfig, List<LightState>` | Gets cabin light state capabilities. |
| `checkCabinLightSwitchCapabilities` | 지원 확인 | CAR_LIGHTS | `List<LightSwitch>` | Gets cabin light switch capabilities. |
| `checkFrontFogLightStateCapabilities` | 지원 확인 | CAR_LIGHTS | `CapabilityConfig, List<LightState>` | Gets front fog light state capabilities. |
| `checkFrontFogLightSwitchCapabilities` | 지원 확인 | CAR_LIGHTS | `List<LightSwitch>` | Gets front fog light switch capabilities. |
| `checkHighBeamLightStateCapabilities` | 지원 확인 | CAR_LIGHTS | `CapabilityConfig, List<LightState>` | Gets high beam light state capabilities. |
| `checkHighBeamLightSwitchCapabilities` | 지원 확인 | CAR_LIGHTS | `List<LightSwitch>` | Gets high beam light switch capabilities. |
| `checkLowBeamLightStateCapabilities` | 지원 확인 | CAR_LIGHTS | `CapabilityConfig, List<LightState>` | Gets low beam light state capabilities. |
| `checkLowBeamLightSwitchCapabilities` | 지원 확인 | CAR_LIGHTS | `List<LightSwitch>` | Gets low beam light switch capabilities. |
| `checkReadingLightStateCapabilities` | 지원 확인 | CAR_LIGHTS | `CapabilityConfig, Map<SeatArea, List<LightState>>` | Gets reading light state capabilities. |
| `checkReadingLightSwitchCapabilities` | 지원 확인 | CAR_LIGHTS | `Map<SeatArea, List<LightSwitch>>` | Gets reading light switch capabilities. |
| `checkRearFogLightStateCapabilities` | 지원 확인 | CAR_LIGHTS | `CapabilityConfig, List<LightState>` | Gets rear fog light state capabilities. |
| `checkRearFogLightSwitchCapabilities` | 지원 확인 | CAR_LIGHTS | `List<LightSwitch>` | Gets rear fog light switch capabilities. |
| `checkTailLampLightStateCapabilities` | 지원 확인 | CAR_LIGHTS | `CapabilityConfig, List<LightState>` | Gets tail lamp light state capabilities. |
| `checkTailLampLightSwitchCapabilities` | 지원 확인 | CAR_LIGHTS | `List<LightSwitch>` | Gets tail lamp light switch capabilities. |
| `getAmbientColorAutoByDriveModeEnabled` | 조회 | CAR_LIGHTS | `Boolean?` | Gets whether Ambient Color Auto By Drive Mode is enabled. |
| `getAmbientLightBrightness` | 조회 | CAR_LIGHTS | `Int?` | Gets the current Ambient Light Brightness for the specified seat area. |
| `getAmbientLightBrightnessColor` | 조회 | CAR_LIGHTS | `Int?` | Gets the current Ambient Light Brightness Color (32-bit ARGB) for the specified seat area. |
| `getAmbientLightState` | 조회 | CAR_LIGHTS | `LightState?` | Gets the current Ambient Light State for the specified seat area. |
| `getAmbientLightSwitch` | 조회 | CAR_LIGHTS | `LightSwitch?` | Gets the current Ambient Light Switch for the specified seat area. |
| `getCabinLightSwitch` | 조회 | CAR_LIGHTS | `LightSwitch?` | Gets the current cabin light switch. |
| `getCurrentCabinLightState` | 조회 | CAR_LIGHTS | `LightState?` | Gets the current cabin light state. |
| `getCurrentFrontFogLightState` | 조회 | CAR_LIGHTS | `LightState?` | Gets the current front fog light state. |
| `getCurrentHighBeamLightState` | 조회 | CAR_LIGHTS | `LightState?` | Gets the current high beam light state. |
| `getCurrentLowBeamLightState` | 조회 | CAR_LIGHTS | `LightState?` | Gets the current low beam light state. |
| `getCurrentReadingLightState` | 조회 | CAR_LIGHTS | `LightState?` | Gets the current reading light state. |
| `getCurrentRearFogLightState` | 조회 | CAR_LIGHTS | `LightState?` | Gets the current rear fog light state. |
| `getCurrentTailLampLightState` | 조회 | CAR_LIGHTS | `LightState?` | Gets the current tail lamp light state. |
| `getFrontFogLightSwitch` | 조회 | CAR_LIGHTS | `LightSwitch?` | Gets the current front fog light switch. |
| `getHighBeamLightSwitch` | 조회 | CAR_LIGHTS | `LightSwitch?` | Gets the current high beam light switch. |
| `getLowBeamLightSwitch` | 조회 | CAR_LIGHTS | `LightSwitch?` | Gets the current low beam light switch. |
| `getReadingLightSwitch` | 조회 | CAR_LIGHTS | `LightSwitch?` | Gets the current reading light switch. |
| `getRearFogLightSwitch` | 조회 | CAR_LIGHTS | `LightSwitch?` | Gets the current rear fog light switch. |
| `getTailLampLightSwitch` | 조회 | CAR_LIGHTS | `LightSwitch?` | Gets the current tail lamp light switch. |
| `registerAmbientColorAutoByDriveModeEnabled` | 구독 | CAR_LIGHTS | `` | Registers a listener to receive Ambient Color Auto By Drive Mode Enabled updates. |
| `registerAmbientLightBrightnessColorListener` | 구독 | CAR_LIGHTS | `` | Registers a listener to receive Ambient Light Brightness Color updates for the specified seat area. |
| `registerAmbientLightBrightnessListener` | 구독 | CAR_LIGHTS | `` | Registers a listener to receive Ambient Light Brightness updates for the specified seat area. |
| `registerAmbientLightStateListener` | 구독 | CAR_LIGHTS | `` | Registers a listener to receive Ambient Light State updates for the specified seat area. |
| `registerCabinLightState` | 구독 | CAR_LIGHTS | `` | Registers the CabinLightState to get property updates. |
| `registerFrontFogLightState` | 구독 | CAR_LIGHTS | `` | Registers the FrontFogLightState to get property updates. |
| `registerHighBeamLightState` | 구독 | CAR_LIGHTS | `` | Registers the HighBeamLightState to get property updates. |
| `registerLowBeamLightState` | 구독 | CAR_LIGHTS | `` | Registers the LowBeamLightState to get property updates. |
| `registerReadingLightState` | 구독 | CAR_LIGHTS | `` | Registers the ReadingLightState to get property updates. |
| `registerRearFogLightState` | 구독 | CAR_LIGHTS | `` | Registers the RearFogLightState to get property updates. |
| `registerTailLampLightState` | 구독 | CAR_LIGHTS | `` | Registers the TailLampLightState to get property updates. |
| `setAmbientColorAutoByDriveModeEnabled` | 제어/요청 | CONTROL_CAR_LIGHTS | `Boolean?` | Sets whether Ambient Color Auto By Drive Mode is enabled. |
| `setAmbientLightBrightness` | 제어/요청 | CAR_LIGHTS | `SeatArea, Int` | Sets the Ambient Light Brightness for the specified seat area. |
| `setAmbientLightBrightnessColor` | 제어/요청 | CAR_LIGHTS | `SeatArea, Int` | Sets the Ambient Light Brightness Color for the specified seat area. |
| `setAmbientLightSwitch` | 제어/요청 | CAR_LIGHTS | `SeatArea, LightSwitch?` | Sets the Ambient Light Switch for the specified seat area. |
| `setCabinLightSwitch` | 제어/요청 | CONTROL_CAR_LIGHTS | `LightSwitch?` | Sets the cabin light switch. |
| `setFrontFogLightSwitch` | 제어/요청 | CONTROL_CAR_LIGHTS | `LightSwitch?` | Sets the front fog light switch. |
| `setHighBeamLightSwitch` | 제어/요청 | CONTROL_CAR_LIGHTS | `LightSwitch?` | Sets the high beam light switch. |
| `setLowBeamLightSwitch` | 제어/요청 | CONTROL_CAR_LIGHTS | `LightSwitch?` | Sets the low beam light switch. |
| `setReadingLightSwitch` | 제어/요청 | CONTROL_CAR_LIGHTS | `LightSwitch?` | Sets the reading light switch. |
| `setRearFogLightSwitch` | 제어/요청 | CONTROL_CAR_LIGHTS | `LightSwitch?` | Sets the rear fog light switch. |
| `setTailLampLightSwitch` | 제어/요청 | CONTROL_CAR_LIGHTS | `LightSwitch?` | Sets the tail lamp light switch. |
| `unregisterAmbientColorAutoByDriveModeEnabled` | 구독 | CAR_LIGHTS | `` | Unregisters a previously registered Ambient Color Auto By Drive Mode Enabled listener. |
| `unregisterAmbientLightBrightnessColorListener` | 구독 | CAR_LIGHTS | `` | Unregisters a previously registered Ambient Light Brightness Color listener for the specified seat area. |
| `unregisterAmbientLightBrightnessListener` | 구독 | CAR_LIGHTS | `` | Unregisters a previously registered Ambient Light Brightness listener for the specified seat area. |
| `unregisterAmbientLightStateListener` | 구독 | CAR_LIGHTS | `` | Unregisters a previously registered Ambient Light State listener for the specified seat area. |
| `unregisterCabinLightState` | 구독 | CAR_LIGHTS | `` | Stops getting property updates for the given CabinLightState. |
| `unregisterFrontFogLightState` | 구독 | CAR_LIGHTS | `` | Stops getting property updates for the given FrontFogLightState. |
| `unregisterHighBeamLightState` | 구독 | CAR_LIGHTS | `` | Stops getting property updates for the given HighBeamLightState. |
| `unregisterLowBeamLightState` | 구독 | CAR_LIGHTS | `` | Stops getting property updates for the given LowBeamLightState. |
| `unregisterReadingLightState` | 구독 | CAR_LIGHTS | `` | Stops getting property updates for the given ReadingLightState. |
| `unregisterRearFogLightState` | 구독 | CAR_LIGHTS | `` | Stops getting property updates for the given RearFogLightState. |
| `unregisterTailLampLightState` | 구독 | CAR_LIGHTS | `` | Stops getting property updates for the given TailLampLightState. |

## Vehicle SDK · odometer (35개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `checkCurrentDistanceDrivenCapabilities` | 지원 확인 | CAR_DRIVE | `CapabilityConfig` | Gets current distance driven capabilities. |
| `checkTripCumulativeDrivingDistanceCapabilities` | 지원 확인 | CAR_DRIVE | `CapabilityConfig` | Gets Trip Cumulative Driving Distance capabilities. |
| `checkTripCumulativeDrivingTimeCapabilities` | 지원 확인 | CAR_DRIVE | `CapabilityConfig` | Gets Trip Cumulative Driving Time capabilities. |
| `checkTripCurrentDrivingDistanceCapabilities` | 지원 확인 | CAR_DRIVE | `CapabilityConfig` | Gets Trip Current Driving Distance capabilities. |
| `checkTripCurrentDrivingTimeCapabilities` | 지원 확인 | CAR_DRIVE | `CapabilityConfig` | Gets Trip Current Driving Time capabilities. |
| `checkTripDrivingDistanceCapabilities` | 지원 확인 | CAR_DRIVE | `CapabilityConfig` | Gets Trip Driving Distance capabilities. |
| `checkTripDrivingTimeCapabilities` | 지원 확인 | CAR_DRIVE | `CapabilityConfig` | Gets Trip Driving Time capabilities. |
| `checkTripInstantaneousDrivingDistanceCapabilities` | 지원 확인 | CAR_DRIVE | `CapabilityConfig` | Gets Trip Instantaneous Driving Distance capabilities. |
| `checkTripSinceRefillDrivingDistanceCapabilities` | 지원 확인 | CAR_DRIVE | `CapabilityConfig` | Gets Trip Since Refuel Driving Distance capabilities. |
| `checkTripSinceRefillDrivingTimeCapabilities` | 지원 확인 | CAR_DRIVE | `CapabilityConfig` | Gets Trip Since Refuel Driving Time capabilities. |
| `getCurrentDistanceDriven` | 조회 | CAR_DRIVE | `Float?` | Current odometer value of the vehicle in kilometers (km). |
| `getTripCumulativeDrivingDistance` | 조회 | CAR_DRIVE | `Float?` | Gets trip cumulative driving distance in meters (m). |
| `getTripCumulativeDrivingTime` | 조회 | CAR_DRIVE | `Int?` | Gets trip cumulative driving time. |
| `getTripCurrentDrivingDistance` | 조회 | CAR_DRIVE | `Float?` | Gets trip current driving distance in meters (m). |
| `getTripCurrentDrivingTime` | 조회 | CAR_DRIVE | `Int?` | Gets trip current driving time. |
| `getTripInstantaneousDrivingDistance` | 조회 | CAR_DRIVE | `Float?` | Gets trip instantaneous driving distance in meters (m). |
| `getTripSinceRefillDrivingDistance` | 조회 | CAR_DRIVE | `Float?` | Gets trip since refuel driving distance in meters (m). |
| `getTripSinceRefillDrivingTime` | 조회 | CAR_DRIVE | `Int?` | Gets trip since refuel driving time. |
| `registerDistanceDriven` | 구독 | CAR_DRIVE | `` | Registers a listener for odometer updates in kilometers (km). |
| `registerTripCumulativeDrivingDistance` | 구독 | CAR_DRIVE | `` | Registers a listener for trip cumulative driving distance updates in meters (m). |
| `registerTripCumulativeDrivingTime` | 구독 | CAR_DRIVE | `` | Registers Trip Cumulative Driving Time listener. |
| `registerTripCurrentDrivingDistance` | 구독 | CAR_DRIVE | `` | Registers a listener for trip current driving distance updates in meters (m). |
| `registerTripCurrentDrivingTime` | 구독 | CAR_DRIVE | `` | Registers Trip Current Driving Time listener. |
| `registerTripInstantaneousDrivingDistance` | 구독 | CAR_DRIVE | `` | Registers a listener for trip instantaneous driving distance updates in meters (m). |
| `registerTripSinceRefillDrivingDistance` | 구독 | CAR_DRIVE | `` | Registers a listener for trip since refuel driving distance updates in meters (m). |
| `registerTripSinceRefillDrivingTime` | 구독 | CAR_DRIVE | `` | Registers Trip Since Refuel Driving Time listener. |
| `setRangeRemainingDistance` | 제어/요청 | CONTROL_CAR_DRIVE | `Float?` | Sets the estimated remaining range in meters (m). |
| `unregisterDistanceDriven` | 구독 | CAR_DRIVE | `` | Stops getting property updates for the given DistanceDriven. |
| `unregisterTripCumulativeDrivingDistance` | 구독 | CAR_DRIVE | `` | Unregisters Trip Cumulative Driving Distance listener. |
| `unregisterTripCumulativeDrivingTime` | 구독 | CAR_DRIVE | `` | Unregisters Trip Cumulative Driving Time listener. |
| `unregisterTripCurrentDrivingDistance` | 구독 | CAR_DRIVE | `` | Unregisters Trip Current Driving Distance listener. |
| `unregisterTripCurrentDrivingTime` | 구독 | CAR_DRIVE | `` | Unregisters Trip Current Driving Time listener. |
| `unregisterTripInstantaneousDrivingDistance` | 구독 | CAR_DRIVE | `` | Unregisters Trip Instantaneous Driving Distance listener. |
| `unregisterTripSinceRefillDrivingDistance` | 구독 | CAR_DRIVE | `` | Unregisters Trip Since Refuel Driving Distance listener. |
| `unregisterTripSinceRefillDrivingTime` | 구독 | CAR_DRIVE | `` | Unregisters Trip Since Refuel Driving Time listener. |

## Vehicle SDK · safety (37개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `checkAirbagCapabilities` | 지원 확인 | CAR_SAFETY | `List<SeatArea>` | Gets airbag capabilities. |
| `checkAirbagDeployedCapabilities` | 지원 확인 | CAR_SAFETY | `CapabilityConfig, List<SeatArea>` | Gets airbag deployed capabilities. |
| `checkBrakeFluidLevelStateCapabilities` | 지원 확인 | CAR_SAFETY | `CapabilityConfig, List<OilLevelState>` | Gets brake fluid level state capabilities. |
| `checkEngineOilLevelStateCapabilities` | 지원 확인 | CAR_SAFETY | `CapabilityConfig, List<OilLevelState>` | Gets engine oil level state capabilities. |
| `checkLowWasherFluidWarningStateCapabilities` | 지원 확인 | CAR_SAFETY | `CapabilityConfig` | Gets low washer fluid warning state capabilities. |
| `checkPowerDownWarningIndicatorCapabilities` | 지원 확인 | CAR_SAFETY | `CapabilityConfig` | Gets power down warning indicator capabilities. |
| `checkSeatBuckledCapabilities` | 지원 확인 | CAR_SAFETY | `List<SeatArea>` | Gets seat buckled capabilities. |
| `checkSeatNotBuckledWarningCapabilities` | 지원 확인 | CAR_SAFETY | `CapabilityConfig, List<SeatArea>` | Gets seat not buckled warning capabilities. |
| `checkTelltalesCapabilities` | 지원 확인 | PRIVATE_CAR_SAFETY | `CapabilityConfig` | Gets telltales capabilities. |
| `checkTransmissionOilLevelStateCapabilities` | 지원 확인 | CAR_SAFETY | `CapabilityConfig, List<OilLevelState>` | Gets transmission oil level state capabilities. |
| `getBrakeFluidLevelState` | 조회 | CAR_SAFETY | `OilLevelState?` | Gets the current brake fluid level. |
| `getEngineOilLevelState` | 조회 | CAR_SAFETY | `OilLevelState?` | Gets the current engine oil level. |
| `getTelltalesInfo` | 조회 | CAR_SAFETY | `telltales: Telltales` | Gets telltales info. |
| `getTransmissionOilLevelState` | 조회 | CAR_SAFETY | `OilLevelState?` | Gets the current transmission oil level. |
| `hasLowWasherFluidWarning` | 조회 | CAR_SAFETY | `Boolean?` | Checks the current washer fluid low warning status. If the status is true, there is an issue with the washer fluid |
| `hasPowerDownWarningIndicator` | 조회 | CAR_SAFETY | `Boolean?` | Gets the current power down warning indicator state. If the state is true, there is a problem with the engine. |
| `hasSeatAirbagBeenDeployed` | 조회 | CAR_SAFETY | `Boolean?` | Gets the current airbag deployment status. If the state is true, the airbag is deployed. |
| `hasSeatAirbagEnabled` | 조회 | CAR_SAFETY | `Boolean?` | Gets the seat airbag enabled state. If the state is true, there is a problem with the seat airbag in the area. |
| `hasSeatBeltNotBuckledWarning` | 조회 | CAR_SAFETY | `Boolean?` | Gets the seat belt buckled warning state. If the state is true, the seat belt in the area is not buckled. |
| `registerBrakeFluidLevelState` | 구독 | CAR_SAFETY | `` | Registers the BrakeFluidLevelState to get property updates. |
| `registerEngineOilLevelState` | 구독 | CAR_SAFETY | `` | Registers the EngineOilLevelState to get property updates. |
| `registerLowWasherFluidWaringState` | 구독 | CAR_SAFETY | `` | Registers the LowWasherFluidWaringState to get property updates. |
| `registerPowerDownWarningIndicator` | 구독 | CAR_SAFETY | `` | Registers the PowerDownWarningIndicator to get property updates. |
| `registerSeatAirbagDeployed` | 구독 | CAR_SAFETY | `` | Registers the SeatAirbagDeployed to get property updates. |
| `registerSeatAirbagEnabled` | 구독 | CAR_SAFETY | `` | Registers the SeatAirbagEnabled to get property updates. |
| `registerSeatBeltNotBuckledWarning` | 구독 | CAR_SAFETY | `` | Registers the SeatBeltNotBuckledWarning to get property updates. |
| `registerTelltales` | 구독 | PRIVATE_CAR_SAFETY | `` | Registers a Telltales listener to receive telltale information updates. |
| `registerTransmissionOilLevelState` | 구독 | CAR_SAFETY | `` | Registers the TransmissionOilLevel to get property updates. |
| `unregisterBrakeFluidLevelState` | 구독 | CAR_SAFETY | `` | Stops getting property updates for the given BrakeFluidLevelState. |
| `unregisterEngineOilLevelState` | 구독 | CAR_SAFETY | `` | Stops getting property updates for the given EngineOilLevelState. |
| `unregisterLowWasherFluidWaringState` | 구독 | CAR_SAFETY | `` | Stops getting property updates for the given LowWasherFluidWaringState. |
| `unregisterPowerDownWarningIndicator` | 구독 | CAR_SAFETY | `` | Stops getting property updates for the given PowerDownWarningIndicator. |
| `unregisterSeatAirbagDeployed` | 구독 | CAR_SAFETY | `` | Stops getting property updates for the given SeatAirbagDeployed. |
| `unregisterSeatAirbagEnabled` | 구독 | CAR_SAFETY | `` | Stops getting property updates for the given SeatAirbagEnabled. |
| `unregisterSeatBeltNotBuckledWarning` | 구독 | CAR_SAFETY | `` | Stops getting property updates for the given SeatBeltNotBuckledWarning. |
| `unregisterTelltales` | 구독 | PRIVATE_CAR_SAFETY | `` | Unregisters a previously registered Telltales listener. |
| `unregisterTransmissionOilLevelState` | 구독 | CAR_SAFETY | `` | Stops getting property updates for the given TransmissionOilLevel. |

## Vehicle SDK · seat (97개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `checkMoveSeatBackrestAngleCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, List<SeatArea>` | Gets move seat BackrestAngle capabilities. |
| `checkMoveSeatBeltHeightCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, List<SeatArea>` | Gets move seat belt height capabilities. |
| `checkMoveSeatDepthCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, List<SeatArea>` | Gets move seat depth capabilities. |
| `checkMoveSeatForeAftCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, List<SeatArea>` | Gets move seat ForeAft capabilities. |
| `checkMoveSeatHeadrestAngleCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, List<SeatArea>` | Gets move seat headrest angle capabilities. |
| `checkMoveSeatHeadrestForeAftCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, List<SeatArea>` | Gets move seat HeadrestForeAft capabilities. |
| `checkMoveSeatHeadrestHeightCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, List<SeatArea>` | Gets move seat headrest height capabilities. |
| `checkMoveSeatHeightCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, List<SeatArea>` | Gets move seat height capabilities. |
| `checkMoveSeatLumbarForeAftCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, List<SeatArea>` | Gets move seat lumbar ForeAft capabilities. |
| `checkMoveSeatLumbarSideSupportCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, List<SeatArea>` | Gets move seat lumbar side support capabilities. |
| `checkMoveSeatTiltCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, List<SeatArea>` | Gets move seat tilt capabilities. |
| `checkSeatBackrestAngleCapabilities` | 지원 확인 | CAR_SEATS | `Map<SeatArea, AreaBounds<Int>>` | checkSeatBackrestAnglePositionCapabilities |
| `checkSeatBackrestAnglePositionCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, Map<SeatArea, AreaBounds<Int>>` | Gets seat BackrestAnglePosition capabilities. |
| `checkSeatBeltBuckledCapabilities` | 지원 확인 | CAR_SEATS | `List<SeatArea>` | Gets seat belt buckled capabilities. |
| `checkSeatBeltHeightCapabilities` | 지원 확인 | CAR_SEATS | `Map<SeatArea, AreaBounds<Int>>` | checkSeatBeltHeightPositionCapabilities |
| `checkSeatBeltHeightPositionCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, Map<SeatArea, AreaBounds<Int>>` | Gets seat belt height position capabilities. |
| `checkSeatDepthCapabilities` | 지원 확인 | CAR_SEATS | `Map<SeatArea, AreaBounds<Int>>` | checkSeatDepthPositionCapabilities |
| `checkSeatDepthPositionCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, Map<SeatArea, AreaBounds<Int>>` | Gets seat depth position capabilities. |
| `checkSeatForeAftCapabilities` | 지원 확인 | CAR_SEATS | `Map<SeatArea, AreaBounds<Int>>` | checkSeatForeAftPositionCapabilities |
| `checkSeatForeAftPositionCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, Map<SeatArea, AreaBounds<Int>>` | Gets seat ForeAft position capabilities. |
| `checkSeatHeadrestAngleCapabilities` | 지원 확인 | CAR_SEATS | `Map<SeatArea, AreaBounds<Int>>` | checkSeatHeadrestAnglePositionCapabilities |
| `checkSeatHeadrestAnglePositionCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, Map<SeatArea, AreaBounds<Int>>` | Gets seat HeadrestAnglePosition capabilities. |
| `checkSeatHeadrestForeAftCapabilities` | 지원 확인 | CAR_SEATS | `Map<SeatArea, AreaBounds<Int>>` | checkSeatHeadrestForeAftPositionCapabilities |
| `checkSeatHeadrestForeAftPositionCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, Map<SeatArea, AreaBounds<Int>>` | Gets seat HeadrestForeAftPosition capabilities. |
| `checkSeatHeadrestHeightCapabilities` | 지원 확인 | CAR_SEATS | `Map<SeatArea, AreaBounds<Int>>` | checkSeatHeadrestHeightPositionCapabilities |
| `checkSeatHeadrestHeightPositionCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, Map<SeatArea, AreaBounds<Int>>` | Gets seat HeadrestHeightPosition capabilities. |
| `checkSeatHeightCapabilities` | 지원 확인 | CAR_SEATS | `Map<SeatArea, AreaBounds<Int>>` | checkSeatHeightPositionCapabilities |
| `checkSeatHeightPositionCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, Map<SeatArea, AreaBounds<Int>>` | Gets seat height position capabilities. |
| `checkSeatLumbarForeAftCapabilities` | 지원 확인 | CAR_SEATS | `Map<SeatArea, AreaBounds<Int>>` | checkSeatLumbarForeAftPositionCapabilities |
| `checkSeatLumbarForeAftPositionCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, Map<SeatArea, AreaBounds<Int>>` | Gets seat LumbarForeAftPosition capabilities. |
| `checkSeatLumbarSideSupportCapabilities` | 지원 확인 | CAR_SEATS | `Map<SeatArea, AreaBounds<Int>>` | checkSeatLumbarSideSupportPositionCapabilities |
| `checkSeatLumbarSideSupportPositionCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, Map<SeatArea, AreaBounds<Int>>` | Gets seat LumbarSideSupportPosition capabilities. |
| `checkSeatMemoryCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, Map<SeatArea, AreaBounds<Int>>` | Gets seat memory capabilities. |
| `checkSeatOccupancyCapabilities` | 지원 확인 | CAR_SEATS | `List<SeatArea>` | Gets seat occupancy capabilities. |
| `checkSeatTemperatureCapabilities` | 지원 확인 | CAR_SEATS | `Map<SeatArea, AreaBounds<Int>>` | Gets seat temperature capabilities. |
| `checkSeatTiltCapabilities` | 지원 확인 | CAR_SEATS | `Map<SeatArea, AreaBounds<Int>>` | checkSeatTiltPositionCapabilities |
| `checkSeatTiltPositionCapabilities` | 지원 확인 | CAR_SEATS | `CapabilityConfig, Map<SeatArea, AreaBounds<Int>>` | Gets seat tilt position capabilities. |
| `checkSeatVentilationCapabilities` | 지원 확인 | CAR_SEATS | `Map<SeatArea, AreaBounds<Int>>` | Gets seat ventilation capabilities. |
| `getCurrentSeatBackrestAnglePosition` | 조회 | CAR_SEATS | `Int?` | Gets the current seat backrest angle position. |
| `getCurrentSeatBeltBuckledState` | 조회 | CAR_SEATS | `Boolean?` | Gets the current seat belt buckled state. |
| `getCurrentSeatBeltHeightPosition` | 조회 | CAR_SEATS | `Int?` | Gets the current seat belt height position. |
| `getCurrentSeatDepthPosition` | 조회 | CAR_SEATS | `Int?` | Gets the current seat depth position. |
| `getCurrentSeatForeAftPosition` | 조회 | CAR_SEATS | `Int?` | Gets the current seat fore/aft position. |
| `getCurrentSeatHeadrestAnglePosition` | 조회 | CAR_SEATS | `Int?` | Gets the current seat headrest angle position. |
| `getCurrentSeatHeadrestForeAftPosition` | 조회 | CAR_SEATS | `Int?` | Gets current seat headrest fore/aft position. |
| `getCurrentSeatHeadrestHeightAftPosition` | 조회 | CAR_SEATS | `Int?` | Gets the current seat headrest height aft position. |
| `getCurrentSeatHeightPosition` | 조회 | CAR_SEATS | `Int?` | Gets the current seat height position. |
| `getCurrentSeatLumbarForeAftPosition` | 조회 | CAR_SEATS | `Int?` | Gets the current seat lumbar fore/aft position. |
| `getCurrentSeatLumbarSideSupportPosition` | 조회 | CAR_SEATS | `Int?` | Gets the seat lumbar side support position. |
| `getCurrentSeatOccupancy` | 조회 | CAR_SEATS | `Boolean?` | Gets current seat occupancy. |
| `getCurrentSeatTiltPosition` | 조회 | CAR_SEATS | `Int?` | Gets the current seat tilt position. |
| `getHvacSeatTemperature` | 조회 | CAR_SEATS | `Int?` | Gets the current seat heating/cooling temperature. |
| `getHvacSeatVentilationState` | 조회 | CAR_SEATS | `Int?` | Gets the current seat ventilation state. |
| `moveSeatBackrestAngle` | 제어/요청 | CONTROL_CAR_SEATS | `SeatArea, MoveControl?` | Move seat backrestAngle. |
| `moveSeatBeltHeight` | 제어/요청 | CONTROL_CAR_SEATS | `SeatArea, MoveControl?` | Moves the seat belt height. |
| `moveSeatDepth` | 제어/요청 | CONTROL_CAR_SEATS | `SeatArea, MoveControl?` | Moves the seat depth. |
| `moveSeatForeAft` | 제어/요청 | CONTROL_CAR_SEATS | `SeatArea, MoveControl?` | Moves the seat fore/aft. |
| `moveSeatHeadrestAngle` | 제어/요청 | CONTROL_CAR_SEATS | `SeatArea, MoveControl?` | Moves the seat headrest angle. |
| `moveSeatHeadrestForeAft` | 제어/요청 | CONTROL_CAR_SEATS | `SeatArea, MoveControl?` | Moves seat headrest fore/aft. |
| `moveSeatHeadrestHeight` | 제어/요청 | CONTROL_CAR_SEATS | `SeatArea, MoveControl?` | Moves seat headrest height. |
| `moveSeatHeight` | 제어/요청 | CONTROL_CAR_SEATS | `SeatArea, MoveControl?` | Moves the seat height. |
| `moveSeatLumbarForeAft` | 제어/요청 | CONTROL_CAR_SEATS | `SeatArea, MoveControl?` | Moves seat lumbar fore/aft. |
| `moveSeatLumbarSideSupport` | 제어/요청 | CONTROL_CAR_SEATS | `SeatArea, MoveControl?` | Moves the seat lumbar side support. |
| `moveSeatTilt` | 제어/요청 | CONTROL_CAR_SEATS | `SeatArea, MoveControl?` | Moves seat tilt. |
| `registerHvacSeatTemperature` | 구독 | CAR_SEATS | `` | Registers the HvacSeatTemperature to get property updates. |
| `registerHvacSeatVentilation` | 구독 | CAR_SEATS | `` | Registers the HvacSeatVentilation to get property updates. |
| `registerSeatBackrestAnglePosition` | 구독 | CAR_SEATS | `` | Registers the SeatBackrestAnglePosition to get property updates. |
| `registerSeatBeltBuckledState` | 구독 | CAR_SEATS | `` | Registers the SeatBeltBuckledState to get property updates. |
| `registerSeatBeltHeightPosition` | 구독 | CAR_SEATS | `` | Registers the SeatBeltHeightPosition to get property updates. |
| `registerSeatDepthPosition` | 구독 | CAR_SEATS | `` | Registers the SeatDepthPosition to get property updates. |
| `registerSeatForeAftPosition` | 구독 | CAR_SEATS | `` | Registers the SeatForeAftPosition to get property updates. |
| `registerSeatHeadrestAnglePosition` | 구독 | CAR_SEATS | `` | Registers the SeatHeadrestAnglePosition to get property updates. |
| `registerSeatHeadrestForeAftPosition` | 구독 | CAR_SEATS | `` | Registers the SeatHeadrestForeAftPosition to get property updates. |
| `registerSeatHeadrestHeightAftPosition` | 구독 | CAR_SEATS | `` | Registers the SeatHeadrestHeightAftPosition to get property updates. |
| `registerSeatHeightPosition` | 구독 | CAR_SEATS | `` | Registers the SeatHeightPosition to get property updates. |
| `registerSeatLumbarForeAftPosition` | 구독 | CAR_SEATS | `` | Registers the SeatLumbarForeAftPosition to get property updates. |
| `registerSeatLumbarSideSupportPosition` | 구독 | CAR_SEATS | `` | Registers the SeatLumbarSideSupportPosition to get property updates. |
| `registerSeatOccupancy` | 구독 | CAR_SEATS | `` | Registers the SeatOccupancy to get property updates. |
| `registerSeatTiltPosition` | 구독 | CAR_SEATS | `` | Registers the SeatTiltPosition to get property updates. |
| `selectSeatMemory` | 제어/요청 | CONTROL_CAR_SEATS | `` | Selects seat memory. |
| `setHvacSeatTemperature` | 제어/요청 | CONTROL_CAR_SEATS | `SeatArea, Int?` | Sets the seat heating/cooling temperature. |
| `setHvacSeatVentilation` | 제어/요청 | CONTROL_CAR_SEATS | `SeatArea, Int?` | Sets the seat ventilation. |
| `unregisterHvacSeatTemperature` | 구독 | CAR_SEATS | `` | Stops getting property updates for the given HvacSeatTemperature. |
| `unregisterHvacSeatVentilation` | 구독 | CAR_SEATS | `` | Stops getting property updates for the given HvacSeatVentilation. |
| `unregisterSeatBackrestAnglePosition` | 구독 | CAR_SEATS | `` | Stops getting property updates for the given SeatBackrestAnglePosition. |
| `unregisterSeatBeltBuckledState` | 구독 | CAR_SEATS | `` | Stops getting property updates for the given SeatBeltBuckledState. |
| `unregisterSeatBeltHeightPosition` | 구독 | CAR_SEATS | `` | Stops getting property updates for the given SeatBeltHeightPosition. |
| `unregisterSeatDepthPosition` | 구독 | CAR_SEATS | `` | Stops getting property updates for the given SeatDepthPosition. |
| `unregisterSeatForeAftPosition` | 구독 | CAR_SEATS | `` | Stops getting property updates for the given SeatForeAftPosition. |
| `unregisterSeatHeadrestAnglePosition` | 구독 | CAR_SEATS | `` | Stops getting property updates for the given SeatHeadrestAnglePosition. |
| `unregisterSeatHeadrestForeAftPosition` | 구독 | CAR_SEATS | `` | Stops getting property updates for the given SeatHeadrestForeAftPosition. |
| `unregisterSeatHeadrestHeightAftPosition` | 구독 | CAR_SEATS | `` | Stops getting property updates for the given SeatHeadrestHeightAftPosition. |
| `unregisterSeatHeightPosition` | 구독 | CAR_SEATS | `` | Stops getting property updates for the given SeatHeightPosition. |
| `unregisterSeatLumbarForeAftPosition` | 구독 | CAR_SEATS | `` | Stops getting property updates for the given SeatLumbarForeAftPosition. |
| `unregisterSeatLumbarSideSupportPosition` | 구독 | CAR_SEATS | `` | Stops getting property updates for the given SeatLumbarSideSupportPosition. |
| `unregisterSeatOccupancy` | 구독 | CAR_SEATS | `` | Stops getting property updates for the given SeatOccupancy. |
| `unregisterSeatTiltPosition` | 구독 | CAR_SEATS | `` | Stops getting property updates for the given SeatTiltPosition. |

## Vehicle SDK · side-mirror (5개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `checkHvacSideMirrorHeatCapabilities` | 지원 확인 | CAR_MIRRORS | `Map<MirrorArea, AreaBounds<Int>>` | Gets hvac side mirror heat capabilities. |
| `getHvacSideMirrorHeatState` | 조회 | CAR_MIRRORS | `Int?` | Gets the current side mirror heat state. |
| `registerHvacSideMirrorHeatState` | 구독 | CAR_CLIMATE | `` | Registers HvacSideMirrorHeatState state to get property updates. |
| `setHvacSideMirrorHeat` | 제어/요청 | CONTROL_CAR_MIRRORS | `MirrorArea, Int?` | Sets the side mirror heat state. |
| `unregisterHvacSideMirrorHeatState` | 구독 | CAR_CLIMATE | `` | Stops getting property updates for the given HvacSideMirrorHeatState state. |

## Vehicle SDK · steering-wheel (9개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `checkHvacSteeringWheelHeatCapabilities` | 지원 확인 | CAR_CLIMATE | `AreaBounds<Int>` | Gets hvac steering wheel heat capabilities. |
| `checkSteeringWheelAngleCapabilities` | 지원 확인 | CAR_DRIVE | `CapabilityConfig, AreaBounds<Float>` | Gets the capabilities for the front-wheel steering angle. |
| `getCurrentSteeringWheelAngle` | 조회 | CAR_DRIVE | `Float?` | Gets the current front-wheel steering angle in degrees. |
| `getHvacSteeringWheelHeatState` | 조회 | CAR_CLIMATE | `Int?` | Gets the current SteeringWheel heating/cooling state. Permission: PleosPermission.CAR_CLIMATE (protection level: normal) |
| `registerHvacSteeringWheelHeatState` | 구독 | CAR_CLIMATE | `` | Registers Hvac SteeringWheel heat state to get property updates. |
| `registerSteeringWheelAngle` | 구독 | CAR_DRIVE | `` | Registers a listener to receive front-wheel steering angle updates. |
| `setHvacSteeringWheelHeat` | 제어/요청 | CONTROL_CAR_CLIMATE | `Int?` | Sets SteeringWheel heating/cooling temperature. Permission: PleosPermission.CONTROL_CAR_CLIMATE (protection level: normal) |
| `unregisterHvacSteeringWheelHeatState` | 구독 | CAR_CLIMATE | `` | Stops getting property updates for the given Hvac SteeringWheel heat state. |
| `unregisterSteeringWheelAngle` | 구독 | CAR_DRIVE | `` | Unregisters the given listener from front-wheel steering angle updates. |

## Vehicle SDK · tire (3개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `checkLowTirePressureCapabilities` | 지원 확인 | CAR_SAFETY | `CapabilityConfig, List<WheelArea>` | Gets low tire pressure capabilities. |
| `checkWheelAreaCapabilities` | 지원 확인 | CAR_SAFETY | `List<WheelArea>` | getLowTirePressure |
| `getLowTirePressure` | 조회 | CAR_SAFETY | `Float?` | Gets the critically low tire pressure threshold in kilopascals (kPa). |

## Vehicle SDK · toll (5개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `checkEtcsVoiceGuidanceVolumeCapabilities` | 지원 확인 | CAR_TOLL | `CapabilityConfig, AreaBounds<Int>` | Gets ETCS voice guidance volume capabilities. |
| `getCurrentEtcsVoiceGuidanceVolume` | 조회 | CAR_TOLL | `Int?` | Gets current ETCS voice guidance volume. |
| `registerEtcsVoiceGuidanceVolume` | 구독 | CAR_TOLL | `` | Registers ETCS voice guidance volume updates. |
| `setEtcsVoiceGuidanceVolume` | 제어/요청 | CONTROL_CAR_TOLL | `` | Sets ETCS voice guidance volume. |
| `unregisterEtcsVoiceGuidanceVolume` | 구독 | CAR_TOLL | `` | Stops ETCS voice guidance volume updates. |

## Vehicle SDK · turn-signal (4개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `checkTurnSignalStateCapabilities` | 지원 확인 | CAR_LIGHTS | `CapabilityConfig` | Gets turn signal state capabilities. |
| `getCurrentTurnSignalState` | 조회 | CAR_LIGHTS | `TurnSignalValue?` | Gets the current turn signal state. |
| `registerTurnSignalState` | 구독 | CAR_LIGHTS | `` | Registers the turn signal state to get property updates. |
| `unregisterTurnSignalState` | 구독 | CAR_LIGHTS | `` | Stops getting property updates for the given turn signal state. |

## Vehicle SDK · window (29개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `checkMoveWindowBlindCapabilities` | 지원 확인 | CAR_WINDOWS | `CapabilityConfig, List<WindowArea>` | Gets move window blind capabilities. |
| `checkSmartGlassLevelCapabilities` | 지원 확인 | CAR_WINDOWS | `CapabilityConfig, Map<WindowArea, AreaBounds<Int>>` | Gets smart glass level capabilities. |
| `checkSmartGlassStateCapabilities` | 지원 확인 | CAR_WINDOWS | `CapabilityConfig` | Gets smart glass state capabilities. |
| `checkSmartGlassZoneStateCapabilities` | 지원 확인 | CAR_WINDOWS | `CapabilityConfig, List<WindowArea>` | Gets smart glass zone state capabilities. |
| `checkWindowBlindPositionCapabilities` | 지원 확인 | CAR_WINDOWS | `CapabilityConfig, Map<WindowArea, AreaBounds<Int>>` | Gets window blind position capabilities. |
| `checkWindowLockCapabilities` | 지원 확인 | CAR_WINDOWS | `List<WindowArea>` | Gets window lock capabilities. |
| `checkWindowPositionCapabilities` | 지원 확인 | CAR_WINDOWS | `Map<WindowArea, AreaBounds<Int>>` | Gets window position capabilities. |
| `getCurrentSmartGlassLevel` | 조회 | CAR_WINDOWS | `Int?` | Gets the current Smart Glass Level for the specified window area. |
| `getCurrentSmartGlassState` | 조회 | CAR_WINDOWS | `Boolean?` | Gets the current Smart Glass state. |
| `getCurrentWindowBlindPosition` | 조회 | CAR_WINDOWS | `Int?` | Gets the current Window Blind Position for the specified window area. |
| `getCurrentWindowLockState` | 조회 | CAR_WINDOWS | `Boolean?` | Gets the current window lock state. |
| `getCurrentWindowPosition` | 조회 | CAR_WINDOWS | `Int?` | Gets the current window position. |
| `getSmartGlassConfig` | 조회 | CAR_WINDOWS | `SmartGlassConfig?` | Gets the Smart Glass configuration for the specified window area. |
| `moveWindowBlind` | 제어/요청 | CONTROL_CAR_WINDOWS | `WindowArea, MoveControl?` | Moves the Window Blind for the specified window area. |
| `registerSmartGlassLevel` | 구독 | CAR_WINDOWS | `` | Registers a listener to receive Smart Glass Level updates for the specified window area. |
| `registerSmartGlassState` | 구독 | CAR_WINDOWS | `` | Registers Smart Glass state listener. |
| `registerWindowBlindPosition` | 구독 | CAR_WINDOWS | `` | Registers a listener to receive Window Blind Position updates for the specified window area. |
| `registerWindowLockState` | 구독 | CAR_WINDOWS | `` | Registers the window lock state to get property updates. |
| `registerWindowPosition` | 구독 | CAR_WINDOWS | `` | Registers the window area to get property updates. |
| `setSmartGlassLevel` | 제어/요청 | CONTROL_CAR_WINDOWS | `WindowArea, Int?` | Sets the Smart Glass Level for the specified window area. |
| `setSmartGlassState` | 제어/요청 | CONTROL_CAR_WINDOWS | `Boolean?` | Sets the Smart Glass state. |
| `setSmartGlassZoneState` | 제어/요청 | CONTROL_CAR_WINDOWS | `WindowArea, List<SmartGlassZoneCommand>?` | Sets the Smart Glass zone state by submitting one command per smart glass panel for the specified window area. |
| `setWindowLock` | 제어/요청 | CONTROL_CAR_WINDOWS | `WindowArea, Boolean?` | Sets the current window lock state. |
| `setWindowPosition` | 제어/요청 | CONTROL_CAR_WINDOWS | `WindowArea, Int?` | Sets the current window position state. |
| `unregisterSmartGlassLevel` | 구독 | CAR_WINDOWS | `` | Unregisters a previously registered Smart Glass Level listener for the specified window area. |
| `unregisterSmartGlassState` | 구독 | CAR_WINDOWS | `` | Unregisters Smart Glass state listener. |
| `unregisterWindowBlindPosition` | 구독 | CAR_WINDOWS | `` | Unregisters a previously registered Window Blind Position listener for the specified window area. |
| `unregisterWindowLockState` | 구독 | CAR_WINDOWS | `` | Stops getting property updates for the given window lock state. |
| `unregisterWindowPosition` | 구독 | CAR_WINDOWS | `` | Stops getting property updates for the given window position. |

## Vehicle SDK · wiper (7개)

| 메서드 | 구분 | 퍼미션 | 결과 타입 | 설명(원문) |
|---|---|---|---|---|
| `checkWiperStateCapabilities` | 지원 확인 | CAR_WINDOWS | `Map<WindowArea, List<WiperState>>` | Gets wiper state capabilities. |
| `checkWiperSwitchCapabilities` | 지원 확인 | CAR_WINDOWS | `Map<WindowArea, List<WiperSwitch>>` | Gets wiper switch capabilities. |
| `getCurrentWiperState` | 조회 | CAR_WINDOWS | `WiperState?` | Gets the current wiper mode state. |
| `getCurrentWiperSwitch` | 조회 | CAR_WINDOWS | `WiperSwitch?` | Gets the current Wiper Switch state for the specified window area. |
| `registerWiperState` | 구독 | CAR_WINDOWS | `` | Registers the wiper state to get property updates. |
| `setWiperSwitch` | 제어/요청 | CONTROL_CAR_WINDOWS | `WindowArea, WiperSwitch?` | Sets the current wiper switch. |
| `unregisterWiperState` | 구독 | CAR_WINDOWS | `` | Stops getting property updates for the given wiper state. |
