# Pleos Playground: 공모전에서 쓸 수 있는 API (재분석, 2026-10-22)

이 문서는 이전 분석(`pleos-api-analysis.md`)의 API 부분을 처음부터 다시 조사해서 대체합니다.
메서드 하나하나의 원문 시그니처와 퍼미션은 [pleos-api-methods.md](pleos-api-methods.md)에 있습니다.

## 0. 조사 범위와 방법

| 대상 | 방법 | 결과 |
|---|---|---|
| document.pleos.ai 공개 문서 전체 | sitemap의 URL 4,314개를 모두 분류 | 아래 1~7장의 근거 |
| SDK 레퍼런스(클래스·메서드 1,868쪽) | 본문이 브라우저에서 Shadow DOM으로 그려지는 구조라서, 클래스 페이지 244개를 실제 브라우저로 렌더링한 뒤 추출 | 메서드 534개의 시그니처와 `@RequiresPermission` 확보. **이전 분석은 이 본문을 읽지 못했습니다** |
| 제품 소개 페이지 20개 | 링크 대상까지 열어 봄 | Maps, ADAS, NaviProvider 등 문서는 모두 "Page Not Found" |
| SDV OS(CODA OS) 문서 2,197쪽 | 직접 요청 | 전부 404로 리다이렉트 |
| SDK 배포 저장소(Maven) | 로그인 없이 조회 | Vehicle, NaviHelper, STT, TTS, FusedLocation만 배포 중 |
| 공모전 사이트 FAQ | 원문 확인 | 허용 SDK 5종 명시 |

---

## 1. 결론

### 쓸 수 있는 것

| SDK | 버전 | 사용 조건 | 근거 수준 |
|---|---|---|---|
| **Vehicle SDK (조회·구독)** | 2.3.2 | 신청 없이 사용 가능 | 문서 원문 + 배포 확인 |
| **NaviHelper SDK** | 3.0.5 | 신청 없이 사용 가능. 내비 탑재 차량에서만 완전 호환 | 문서 원문 + 배포 확인 |
| **AAOS 표준 Car API** | Android 14 | 차속(`PERF_VEHICLE_SPEED`), 주행 규제(`CarUxRestrictionsManager`), 내비 포커스(`CarAppFocusManager`)는 Pleos 문서가 직접 안내 | 문서 원문 |
| **Gleo Interface** | – | `assets/schema.json`에 기능 최대 3개 정의 → 플랫폼 검토·승인 후 활성화 | 문서 원문 |
| **자체 서버** | – | 제출 가이드가 "외부 서버와 연결이 필요한 앱"을 전제로 안내 | 문서 원문 |

### 조건부 (확인 필요)

| SDK | 버전 | 걸리는 점 |
|---|---|---|
| **SpeechToText** | 3.3.1 | 공모전 허용 목록에 있고 배포도 됐지만, Playground 안내 문서에는 "Coming Soon". 서버 모드는 Client ID/Secret으로 `registerApp` 필요 |
| **TextToSpeech** | 3.3.1 | 위와 같음 |
| **FusedLocation** | 1.1.3 | 위와 같음. 차종마다 지원 여부가 다름(`isSupported()`) |
| **Vehicle SDK 제어(`CONTROL_*`)** | 2.3.2 | App Market 사전 승인을 받은 퍼미션만 선언 가능. 공모전 앱에 승인되는지 미확인 |

### 쓸 수 없는 것

| 대상 | 이유 (원문) |
|---|---|
| LLM SDK | "LLM SDK 제공을 종료합니다." 저장소도 2025-11 이후 갱신 없음 |
| Fleet API | "API 엔드포인트 정보는 Pleos Fleet 사용 계약 후에 담당자를 통해 제공합니다." |
| Vehicle Data API | "현재(2026.8월) 유럽 지역에서만 사용 가능합니다." |
| Pleos Platform SDK | "승인된 privileged 앱에 차량 기능을 System API로 제공합니다." |
| Connect Mobile | "사내용 백오피스", "사내망에서만 접근할 수 있습니다." |
| Maps API/JS, Map·Place SDK, ADAS SDK, NaviProvider SDK, Sync SDK, Universal Wallet, Gleo AI SDK, Messaging, Media Server, VDP | 제품 소개 페이지에 이름만 있고 문서는 모두 "Page Not Found". 저장소에도 라이브러리 없음 |
| SDV OS (CODA OS) | 문서 전체가 404로 리다이렉트 |
| 인앱 결제 | "Payment SDK 서비스는 현재 준비 중" |

---

## 2. Vehicle SDK: 실제로 읽을 수 있는 데이터

메서드 이름, 결과 타입, 퍼미션은 모두 레퍼런스 원문 그대로입니다. "에뮬레이터" 열은 Virtual Console 사용 가이드에 해당 카드가 있는지입니다.

| 데이터 | 메서드 (조회 / 구독) | 결과 타입 | 퍼미션 | 에뮬레이터 |
|---|---|---|---|---|
| 브레이크 페달 밟음 | `getCurrentBrakePedalStatus` / `registerBrakePedalStatus` | `Boolean?` | CAR_DRIVE | ✅ Brake Pedal |
| VIN | `getVINInfo` | `String?` | CAR_INFO | – |
| 온도 표시 단위 | `getCurrentTemperatureDisplayUnit` / `register…` | `TemperatureUnit?` | CAR_INFO | – |
| 도어 열림 정도 (좌석별) | `getCurrentDoorPosition` / `registerDoorPosition` | `Int?` | CAR_DOORS | ✅ Door Position |
| 도어 잠금 (좌석별) | `getDoorLockState` / `registerDoorLockState` | `Boolean?` | CAR_DOORS | ✅ Door Lock |
| 글로브박스 잠금 | `getCurrentGloveBoxLockState` / `register…` | `Boolean?` | CAR_DOORS | ✅ Glove Box |
| 드라이브 모드 | `getCurrentDrivingModeState` / `registerDrivingModeState` | `ECO` / `NORMAL` / `SPORT` | CAR_DRIVE | – |
| 크루즈 상태 | `getCruiseControlState` / `register…` | `ENABLED` / `ACTIVATED` / `SUSPENDED` 등 6종 | CAR_DRIVE | ✅ Cruise Control State |
| 크루즈 목표 속도 | `getCruiseControlTargetSpeed` / `register…` | 숫자 | CAR_DRIVE | ✅ Cruise Target Speed |
| EV 배터리 잔량 | `getEvBatteryStateOfCharge` / `register…` | `Int?` (%) | CAR_ENERGY | ✅ Battery SOC |
| EV 전비 (누적·현재·순간·충전 후) | `get…EvEfficiency` / `register…` | 숫자 | CAR_ENERGY | – |
| 충전 전류 제한, 충전 스위치 상태 | `getEvChargeCurrentDrawLimit`, `getEvChargeSwitchState` | 숫자, 상태 | CAR_ENERGY | ✅ Charge 카드들 |
| 연료 잔량 | `getFuelPercent` / `registerFuelPercent` | `Int?` (%) | CAR_ENERGY | – |
| 연비 (누적·현재·순간·주유 후) | `get…FuelEconomy` / `register…` | 숫자 | CAR_ENERGY | – |
| 공조 전체 상태 (전원, AC, AUTO, 듀얼, 풍량, 풍향, 내기순환, 제상, 공기청정, MAX AC/제상) | `getHvac…` / `registerHvac…` | 상태값 | CAR_CLIMATE | ✅ Climate 카드들 |
| 설정 온도 (좌석별) | `getHvacTemperature` / `registerHvacTemperature` | 숫자 | CAR_CLIMATE | ✅ Target Temperature |
| **실내 현재 온도 (좌석별)** | `getHvacTemperatureCurrent` / `register…` | `Float?` (°C) | CAR_CLIMATE | – |
| 조명 상태 (상향·하향등, 안개등, 미등, 실내등, 독서등) | `getCurrent…LightState` / `register…` | `ON` / `OFF` / `DAYTIME_RUNNING` | CAR_LIGHTS | ✅ 일부 |
| 앰비언트 라이트 (상태·밝기·색상) | `getAmbientLight…` / `register…` | 상태, 숫자 | CAR_LIGHTS | ✅ Ambient 카드 |
| 총 주행거리 | `getCurrentDistanceDriven` / `registerDistanceDriven` | `Float?` (km) | CAR_DRIVE | ✅ Odometer |
| 트립 주행거리·시간 (누적·현재·순간·주유 후) | `getTrip…` / `registerTrip…` | 숫자 | CAR_DRIVE | – |
| **경고등 99종** (운전자 주의, 결빙, 과속, TPMS, 연료 부족, 도어 열림, 안전벨트, 오토홀드 등) | `getTelltalesInfo` (구독은 PRIVATE) | `Telltales`(종류, 상태, 색, 우선순위, 경고 횟수) | CAR_SAFETY | ✅ Telltales |
| 오일·브레이크액·워셔액·파워다운 경고, 에어백 상태 | `get…LevelState`, `has…` / `register…` | 상태, `Boolean` | CAR_SAFETY | – |
| 안전벨트 미착용 경고 | `hasSeatBeltNotBuckledWarning` / `register…` | `Boolean` | CAR_SAFETY | – |
| **좌석별 착좌 여부** | `getCurrentSeatOccupancy` / `registerSeatOccupancy` | `Boolean?` | CAR_SEATS | ✅ Seat Occupancy |
| **좌석별 안전벨트 체결** | `getCurrentSeatBeltBuckledState` / `register…` | `Boolean?` | CAR_SEATS | – |
| 시트 위치 11종, 열선, 통풍 | `getCurrentSeat…Position`, `getHvacSeat…` / `register…` | 숫자, 단계 | CAR_SEATS | ✅ 열선·통풍만 |
| 사이드미러·핸들 열선 | `getHvacSideMirrorHeatState`, `getHvacSteeringWheelHeatState` | 상태 | CAR_MIRRORS, CAR_CLIMATE | ✅ 핸들 열선 |
| 조향각 | `getCurrentSteeringWheelAngle` / `register…` | `Float?` (앞바퀴 조향각, 도) | CAR_DRIVE | ✅ Steering Angle |
| 방향지시등 | `getCurrentTurnSignalState` / `register…` | `LEFT` / `RIGHT` / `OFF` | CAR_LIGHTS | ✅ Turn Signal |
| 창문 위치·잠금, 블라인드, 스마트글래스 | `getCurrentWindow…`, `getCurrentSmartGlass…` / `register…` | 숫자, 상태 | CAR_WINDOWS | ✅ 창문만 |
| 와이퍼 작동 상태 | `getCurrentWiperState` / `registerWiperState` | `ON` / `OFF` / `SERVICE` / `OTHER` | CAR_WINDOWS | ✅ Wiper Switch |
| 하이패스 음성 안내 음량 | `getCurrentEtcsVoiceGuidanceVolume` / `register…` | 숫자 | CAR_TOLL | – |
| 타이어 저압 **판정 기준값** | `getLowTirePressure` | `Float?` (kPa) | CAR_SAFETY | – |

좌석 지정값(`SeatArea`)은 `ROW_1~3` × `LEFT` / `CENTER` / `RIGHT`입니다.

### 주의할 점
- **타이어**: `getLowTirePressure`는 실제 공기압이나 경고가 아니라 "저압으로 판정하는 기준값"입니다. 실제 저압 경고는 경고등(`tpms`)으로 알 수 있습니다.
- **경고등**: 조회(`getTelltalesInfo`)는 일반 퍼미션이지만 구독(`registerTelltales`)과 지원 확인은 `PRIVATE_CAR_SAFETY`입니다. 변화를 실시간으로 받을 수 없고 주기적으로 조회해야 할 가능성이 큽니다.
- **Vehicle SDK에 없는 것**: 차속(삭제됨), 기어, 시동 상태, 주차 브레이크, 외기 온도, 실제 타이어 공기압. 차속은 AAOS `PERF_VEHICLE_SPEED`로 읽으라고 문서가 안내합니다.

### 제어 API (`CONTROL_*` 퍼미션, 사전 승인 필요)
도어 열기·잠금, 글로브박스, 충전 설정(전류·목표 %·포트·시작/중지), 공조 전체, 외부·실내 조명 스위치, 시트 이동·메모리·열선·통풍, 사이드미러·핸들 열선, 창문·블라인드·스마트글래스, 와이퍼, 하이패스 음량, 잔여 주행거리 설정.

**예외로 보이는 것**: 앰비언트 라이트 밝기·색상·스위치 변경(`setAmbientLightBrightness`, `setAmbientLightBrightnessColor`, `setAmbientLightSwitch`)은 레퍼런스에 `CONTROL_`이 아니라 조회용 `CAR_LIGHTS`로 표시돼 있습니다. 같은 도메인의 `setAmbientColorAutoByDriveModeEnabled`는 `CONTROL_CAR_LIGHTS`입니다. 문서 오류일 수 있으니 실물로 확인해야 합니다.

---

## 3. NaviHelper SDK: 요청과 이벤트

### 앱이 요청할 수 있는 것

| 메서드 | 퍼미션 | 내용 |
|---|---|---|
| `requestRoute(RouteInfo)` | NAVI_ROUTE_SEARCH | 목적지 설정 (좌표, POI, 주소, 경로 옵션) |
| `requestRouteWithWaypoints(...)` | NAVI_ROUTE_SEARCH | 목적지 + 경유지 |
| `addWaypoint` / `removeWaypoint` | NAVI_ROUTE_SEARCH | 경유지 추가·삭제 (`WaypointIndex`: FIRST, SECOND) |
| `cancelRoute`, `requestReRoute`, `changeRouteOption` | NAVI_ROUTE_SEARCH | 경로 취소, 재탐색, 옵션 변경 |
| **`requestTrip(TripRequest)`** | NAVI_ROUTE | **임의의 장소까지 미리 계산**: 거리, 도착 시간, 에너지 소모량, 도착 시 예상 배터리 잔량, 경로상 정체 |
| `requestNavigationScreen(...)` | NAVI_CUSTOM_MAP | 내비 화면 표시 요청 |
| `getBookmarkInfo`, `getRecentDestinationInfo`, `getCurrentLocationInfo`, `getDestinationInfo`, `getWaypointInfo`, `getRouteStateInfo`, `getTBTInfo`, `getChargerOperatorInfo` | NAVI_ROUTE | 정보 조회. 결과는 이벤트로 옴 |

### 앱이 받을 수 있는 데이터

| 이벤트 | 담긴 필드 (원문) |
|---|---|
| `onDestinationInfo` | 목적지 이름·주소·좌표, `remainDistance`, `remainTime`, **`tollFare`(통행료)** |
| `onWayPointInfo` | 경유지별 이름·주소·좌표, 남은 거리·시간 |
| `onRouteStarted` | 총 거리, 총 시간, 통행료, 목적지 도착 시 예상 잔여 배터리·연료량 |
| `onDestinationArrived`, `onWayPointArrived` | 도착 지점 이름·주소, 주행 거리·시간 |
| `onTBTInfo` | 회전 종류(`TurnType`, IC 진출입·휴게소 진입·회전교차로 등), 다음 회전까지 거리(m), 설명 |
| `onCurrentRoadSpeedLimit` | 현재 도로 제한속도. **등록 후 1초마다** 수신 |
| `onCameraAlert` | 카메라 종류, 남은 거리, 제한속도, 과속 여부, 구간 평균속도, 구간 과속 여부 |
| `onCautionPointInfo` | 주의 구간 종류(사고다발, 낙석, 안개, 스쿨존, 야생동물, 역주행 등), 남은 거리 |
| `onRouteStateInfo` | 안내 중 여부, 내비 모드(`ROUTE_GUIDING`, `SAFE_GUIDING`, **`SIMULATION_GUIDING`**), 경로 옵션, **교통 상황**(정체 구간별 도로명·평균속도·정체 시간, 사고·재난·공사·도로 파손 이벤트와 위치·시간) |
| `onDrivingInfo` | 목적지·경유지별 거리·시간, **충전소 여부, 추천 충전소 여부, 충전기 수, 최대 충전 출력, 목표 SOC, 충전 계획(시작·종료 SOC, 충전 시간)** |
| `onTripInfo` | 거리, 도착 시간(`eta`), 에너지 소모량, 도착 시 예상 SOC, 경로 지점, 구간별 정체 |
| `onNavigationStatus` | `ROUTE_DRIVE` / `FREE_DRIVE` / `INACTIVE` / `INITIALIZING` |
| `onCurrentLocationInfo` | 현재 좌표, 진행 방향, 주소 |
| `onBookmarkInfo`, `onRecentDestinationInfo` | 즐겨찾기(집·회사·EV 충전소·기본), 최근 목적지 |
| `onChargerOperatorInfo` | 충전 사업자 ID·이름 |

---

## 4. 음성·위치 SDK (조건부)

### SpeechToText 3.3.1 (퍼미션 `STT_SERVICE`)
- `request()`로 차량 마이크 인식 시작, `stop()`으로 종료. 결과는 `ResultListener`의 `onUpdated(텍스트, 완료 여부)`로 받습니다.
- `sendAudio(ByteArray)` + `completeAudioSend()`로 앱이 가진 오디오를 넣을 수도 있습니다(온디바이스 모드 한정).
- 모드: `HYBRID`(기본), `SERVER`, `ON_DEVICE`. 언어: `ko-KR`, `en-US`, `fr-CA`, `es-US`, `es-MX`.
- 서버 모드는 `registerApp(clientID, clientSecret, listener)` 필요.

### TextToSpeech 3.3.1 (퍼미션 `TTS_SERVICE`)
- `speak(text, isFinal)`로 조각 단위 스트리밍 발화, `stop()`으로 중지.
- **`setSpeakerType(type: String)`으로 화자를 바꿀 수 있습니다.** 지정하지 않으면 앱에 저장된 화자 → Gleo AI 화자 설정 → 기본값 `haeun` 순으로 씁니다.
- `setAudioAttributes`, `setAudioZoneId`로 오디오 출력 영역을 지정할 수 있습니다.
- 속도·피치 조절과 합성 오디오 파일 받기는 레퍼런스에 없습니다.

### FusedLocation 1.1.3 (퍼미션 `FUSED_LOCATION`, 보호 수준 dangerous)
- `getLastKnownLocation`, `registerFusedLocationCallback`으로 위치를 받습니다.
- `LocationData` 필드: `latitude`, `longitude`, `altitude`, `heading`, `confidence`, `timestampMillis`. **속도 필드는 없습니다.**
- 지오펜스: `addProximityAlert(ProximityAlertRequest(latitude, longitude, radiusMeters, transitionTypes), listener)`. 진입(`ENTER`)·이탈(`EXIT`) 알림.

---

## 5. Gleo Interface (차량 AI 비서 연동)

- 앱이 `assets/schema.json`에 기능을 최대 3개 정의하면, 사용자가 Gleo에게 말했을 때 Gleo가 발화를 해석해 앱 기능을 파라미터와 함께 호출합니다.
- 호출 방식은 Activity 실행 또는 Broadcast(화면 없이 백그라운드 처리) 두 가지입니다.
- 앱은 결과를 broadcast로 돌려주고, Gleo가 그 결과로 음성 답변을 만듭니다. `asking` 상태로 되묻기가 가능합니다.
- 플랫폼 검토·승인 후 활성화됩니다. 에뮬레이터에서 미리 테스트할 수 있는지는 문서에 없습니다.

---

## 6. 이전 분석에서 바로잡은 것

| 이전 분석 | 실제 (원문 레퍼런스) |
|---|---|
| Tire는 "저압 경고"를 준다 | 저압 **판정 기준값(kPa)**을 준다. 실제 경고는 경고등 `tpms` |
| 경고등을 구독할 수 있다 | 조회만 일반 퍼미션. 구독은 `PRIVATE_CAR_SAFETY` |
| TTS는 화자를 고를 수 없다 | `setSpeakerType`으로 고를 수 있다 |
| NaviHelper 데이터는 TBT·카메라·주의 구간 정도 | 교통 정체·돌발 이벤트, 통행료, EV 충전 계획, 임의 목적지 사전 계산(`requestTrip`)까지 있다 |
| 앰비언트 라이트 변경은 제어 승인 필요 | 레퍼런스상 조회 퍼미션(`CAR_LIGHTS`)으로 표시됨. 확인 필요 |
| 제품 페이지·SDV OS는 범위 밖으로 제외 | 열어 본 결과 모두 비공개(404) |

## 7. 아직 실물로 확인해야 하는 것

1. STT·TTS·FusedLocation이 Playground 콘솔에서 지금도 "Coming Soon"인지
2. 공모전 앱에 `CONTROL_*` 퍼미션이 승인되는지, 앰비언트 라이트 퍼미션 표기가 맞는지
3. 에뮬레이터에 내비 앱이 있는지, `SIMULATION_GUIDING`으로 모의 주행을 할 수 있는지
4. 에뮬레이터에서 차속·GPS를 바꿀 수 있는지
5. AAOS 표준 프로퍼티(기어, 외기 온도 등)를 서드파티 앱이 읽을 수 있는지
6. Gleo Interface를 공모전 기간 안에 승인받고 테스트할 수 있는지
