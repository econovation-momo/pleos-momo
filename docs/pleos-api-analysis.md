# Pleos Playground API·데이터 분석 (2026-10-21 기준)

> **2026-10-22 갱신**: 이 문서의 API 관련 내용(0, 2, 3, 4장)은 [pleos-usable-apis.md](pleos-usable-apis.md)로 대체됐습니다. 이 문서에는 레퍼런스 원문을 읽기 전에 쓴 오류(타이어 저압 경고, 경고등 구독, TTS 화자 선택 불가)가 남아 있습니다. 공모전 요강(1장), 설계 제약(5장), 개발 환경(6장)은 여전히 유효합니다.

공모전 기획 전에 "무엇을 쓸 수 있고, 어떤 데이터를 다룰 수 있는지"를 정리한 문서입니다.
document.pleos.ai의 가이드 문서 235개(클래스 레퍼런스 약 1,900쪽은 메서드 목록만 추출)와 공모전 사이트(appdevchallenge.auto-hmg.io)를 모두 읽고 정리했습니다.
"추측"이라고 표시한 부분은 문서에 근거가 없는 해석입니다.

---

## 0. 한눈에 보는 결론

### 공모전 앱이 실제로 쓸 수 있는 것

| 구분 | 상태 | 비고 |
|---|---|---|
| **Vehicle SDK** (차량 상태 조회·구독·일부 제어) | 바로 사용 가능 | 조회(Vehicle State)는 신청 없이 사용 가능합니다. 제어(Vehicle Control)는 "Coming Soon" 상태입니다 |
| **NaviHelper SDK** (내비 연동) | 바로 사용 가능 | 목적지·경유지 설정, TBT·과속카메라·주의 구간 이벤트 수신 |
| **SpeechToText / TextToSpeech SDK** | 문서상 "Coming Soon" | 공모전 FAQ는 허용 SDK로 명시합니다. 사용 시점을 확인해야 합니다 |
| **FusedLocation SDK** (위치·지오펜스) | 문서상 "Coming Soon" | 위와 같습니다 |
| **Gleo Interface** (차량 AI 비서 연동) | 스키마 검토·승인 후 사용 | "글레오, ○○ 해줘"라는 발화로 앱 기능(최대 3개)을 호출합니다 |
| **AAOS 표준 Car API** (`CarPropertyManager`, `CarUxRestrictionsManager`) | 사용 가능 | 차속, 기어, 주행 규제 상태 같은 표준 데이터는 여기서 읽습니다 |

### 공모전 앱이 쓸 수 없거나 쓰기 어려운 것

| 구분 | 이유 |
|---|---|
| **LLM SDK** | 2026-10-19 릴리스에서 **제공이 종료**됐습니다. Gleo Interface로 대체하라고 안내합니다 |
| **Fleet API** (차량 관제 REST/Webhook) | Pleos Fleet 사용 계약을 맺은 기업만 쓸 수 있습니다. 엔드포인트 주소도 계약 후에 받습니다 |
| **Vehicle Data API** (커넥티드카 텔레매틱스) | **유럽 지역 전용**입니다. 한국은 지원 국가 목록에 없습니다 |
| **Pleos Platform SDK** (CloudHub, 텔레포니, OTA 등) | `signature|privileged` 권한이 필요한 시스템 앱 전용입니다 |
| **Connect Mobile** | 42dot 사내 테스트용 앱과 사내망 콘솔입니다. 외부 개발자용 모바일 SDK나 API가 없습니다 |

> 공모전 FAQ가 명시한 허용 SDK도 Vehicle, NaviHelper, TTS, STT, FusedLocation 다섯 가지뿐입니다. Fleet API와 Vehicle Data API는 이 목록에 없습니다.

### 기획에 직접 영향을 주는 사실 일곱 가지

1. **차속은 Vehicle SDK에서 삭제됐습니다.** 차속, 기어, 점화 상태는 AAOS `CarPropertyManager`(`PERF_VEHICLE_SPEED` 등)로 읽어야 합니다.
2. **에뮬레이터에서 차속과 GPS를 바꿀 수 있는지는 문서끼리 다릅니다.** Virtual Console 사용 가이드의 카드 목록에는 차속 카드와 위치 카드가 없습니다. 그런데 셋업 가이드 소개에는 "차량 속도, 도어, 조명 등 차량 상태 변경"이라는 문장이 있습니다. 주행 규제 상태는 ADB 명령(`driving_regulation on/off`)으로 켜고 끌 수 있습니다. 실제 에뮬레이터를 띄워서 확인해야 합니다.
3. **주행 중에는 앱 대부분이 막힙니다.** DO(Distraction Optimization)를 선언하지 않은 앱은 주행 중에 아예 쓸 수 없습니다. DO를 선언해도 동영상, 키보드, 설정 화면, 긴 텍스트는 막힙니다.
4. **제어(set) API는 승인이 필요합니다.** 제어용 `CONTROL_CAR_*` 퍼미션은 App Market과 사전 협의해서 승인받아야 하고, 승인받지 않은 퍼미션을 선언하면 심사에서 탈락합니다.
5. **Google Play Services가 없는 환경(non-GAS AAOS)입니다.** Firebase, Google Maps SDK, Google 로그인을 쓸 수 없을 가능성이 높습니다(추측).
6. **인앱 결제 SDK는 "준비 중"입니다.** 결제가 핵심인 비즈니스 모델은 시연 범위에서 구현하기 어렵습니다.
7. **차량 화면은 창 2개 구조입니다.** 앱은 portrait(창 1개)와 landscape(창 2개) 레이아웃을 모두 지원해야 하고, 옆에는 보통 내비가 떠 있습니다.

---

## 1. 공모전 요구사항 요약 (appdevchallenge.auto-hmg.io)

### 일정

| 날짜 | 내용 |
|---|---|
| 2026-10-21 ~ 11-15 | 접수 (이메일) |
| 2026-10-28 (수) 오후 | 온라인 설명회 (질의응답 포함, 다시보기 제공) |
| 2026-11-23 | 1차 서류심사 결과 발표 (20팀 선정) |
| 2026-12-03 ~ 12-04 | 2차 오프라인 PT 심사 (현대차 강남대로사옥) |
| 2026-12-18 | 최종 10팀 발표 |
| 2027-02 | 현대자동차그룹 행사 전시·시상 |

### 제출물

- **필수**: 접수양식(=기획서, DOCX)과 팀원 전원의 개인정보 동의서
- **선택(가산점)**: 시연 영상(목업, 프로토타입, 화면 녹화 인정), GitHub 저장소(소스 + `apk/팀명_앱이름_release.apk`)
- 접수양식 5번 "개발 계획"에는 Pleos Connect 환경에서의 구현 방법, 주요 개발 범위, **SDK/API 사용 계획**을 구체적으로 써야 합니다.
- 1차 통과 팀은 PT 전까지 **실제 동작하는 앱**을 완성해야 합니다. 시연은 에뮬레이터 화면도 인정됩니다.

### 1차 심사 기준 (배점 비공개)

| 항목 | 내용 |
|---|---|
| 차량특화성 | 모바일 대비 차량 환경만의 가치가 있는가, 주행·정차 맥락과 차량 기능에 유기적으로 결합됐는가 |
| 창의성 | 독창적인가, 운전자·동승자의 Pain Point가 명확하고 해결 방식이 분명한가 |
| 기술 활용성 | Pleos SDK·API를 앱의 핵심 기능과 경험에 적절히 활용했는가 |
| 사업성 | 타깃 고객, 예상 MAU, Retention, 확장 계획 |
| 가산점 | 시연 영상, GitHub 저장소(APK 포함) |

### 주최 측이 예시로 든 주제

- 모빌리티 & 라이프 편의 (주문, 픽업, 주차)
- 인카 엔터테인먼트 (주행 맥락과 결합된 미디어·사운드)
- 스마트 드라이빙 & 안전 (이상 징후 조기 인지, 안전운전 피드백)
- 패밀리 & 동승자 케어 (어린이, 가족 동승자)
- 개발 환경 페이지 예시: 음성 메모 앱(STT), 안전운전 점수 앱(Vehicle), 배달 기사용 앱(NaviHelper)

---

## 2. Vehicle SDK: 다룰 수 있는 차량 데이터

- 의존성: `ai.pleos.playground:Vehicle:2.3.2` (Maven: `https://nexus-playground.pleos.ai/repository/maven-releases/`)
- 흐름: `Vehicle(context)` → `initialize()` → `getDoor()` 같은 도메인 객체 → 호출 → `release()`
- 호출 형태는 네 가지입니다.
  - `checkXxxCapabilities`: 이 차량이 지원하는지, 읽기/쓰기 가능한지(`NONE`/`READ`/`WRITE`/`READ_WRITE`), 값 범위
  - `getXxx`: 현재 값 조회
  - `registerXxx` / `unregisterXxx`: 값이 바뀔 때마다 콜백 수신
  - `setXxx` / `moveXxx`: 제어 요청 (승인 필요)
- 차종마다 지원 항목이 다르므로 항상 capability를 먼저 확인해야 합니다.

### 도메인별 데이터 맵

범례: 읽기 = get, 구독 = register, 제어 = set/move

| 도메인 | 읽기·구독 가능한 데이터 | 제어 가능한 항목 | 조회 퍼미션 |
|---|---|---|---|
| Brake | 브레이크 페달 상태 | – | `CAR_DRIVE` |
| CarInfo | VIN (CRN은 폐기) | – | `CAR_INFO` |
| Display | 온도 표시 단위 | – | `CAR_INFO` |
| Door | 도어 열림/닫힘, 도어 잠금, 글로브박스 잠금 | 도어 열기·닫기, 잠금, 글로브박스 | `CAR_DOORS` |
| DrivingMode | 크루즈 컨트롤 상태, 크루즈 목표 속도, 드라이브 모드 | – | `CAR_DRIVE` |
| EvBattery | 배터리 SOC, 전비(누적·현재·순간·충전 후), 충전 전류 제한, 충전 스위치 | 충전 전류 제한, 충전 목표 %, 충전 포트 열기, 충전 시작/중지 | `CAR_ENERGY` |
| Fuel | 연료량 %, 연비(누적·현재·순간·주유 후) | – | `CAR_ENERGY` |
| HVAC | 공조 전원, AC, AUTO, 듀얼, 풍량, 풍향, 내기순환, 성에 제거, 공기청정, 설정 온도, **실내 현재 온도** | 거의 전 항목 (실내 현재 온도 제외) | `CAR_CLIMATE` |
| Light | 앰비언트 라이트(상태·밝기·색상·주행모드 연동), 실내등, 독서등, 상향·하향등, 안개등, 미등 | 앰비언트 밝기·색상, 실내등·독서등, 외부 조명 스위치 | `CAR_LIGHTS` |
| Odometer | 총 주행거리, 트립 주행거리·시간(누적·현재·순간·주유 후) | 잔여 주행가능거리 설정 | `CAR_DRIVE` |
| Safety | 에어백 상태·전개, 브레이크액·엔진오일·변속기오일 상태, 워셔액 부족, 파워다운 경고, 안전벨트 미착용 경고, **경고등(Telltales) 전체** | – (읽기 전용) | `CAR_SAFETY` |
| Seat | 시트 위치 11종(등받이, 전후, 높이, 헤드레스트, 요추 등), 안전벨트 착용, **착좌 여부**, 열선, 통풍 | 시트 이동(move), 시트 메모리 호출, 열선, 통풍 | `CAR_SEATS` |
| SideMirror | 사이드미러 열선 | 열선 | `CAR_MIRRORS` |
| SteeringWheel | 조향각, 핸들 열선 | 핸들 열선 | (표에 없음, 확인 필요) |
| Tire | 저압 경고 | – | `CAR_SAFETY` |
| Toll | 하이패스(ETCS) 음성안내 음량 | 음량 | `CAR_TOLL` |
| TurnSignal | 방향지시등 상태 | – | `CAR_LIGHTS` |
| Window | 창문 위치, 창문 잠금, 블라인드, 스마트글래스 | 창문 위치·잠금, 블라인드, 스마트글래스 | `CAR_WINDOWS` |
| Wiper | 와이퍼 상태·스위치 | 와이퍼 스위치 | `CAR_WINDOWS` |

- 퍼미션 문자열은 모두 `pleos.car.permission.` 접두사를 붙입니다. 제어용은 `CONTROL_CAR_*` 형태입니다(문서로 확인된 것은 `CONTROL_CAR_DOORS`뿐이고 나머지는 같은 규칙으로 추측한 것입니다).
- **Vehicle SDK에 없는 데이터**: 차속(삭제됨), 기어, 점화, 주차 브레이크, 습도. 표준 데이터는 AAOS `CarPropertyManager`로 읽습니다.
- 주행 중에는 일부 API가 `ERROR_DRIVING_RESTRICTED`로 실패할 수 있습니다.
- 제어 요청이 성공했다고 실제 값이 바뀐 것은 아니므로, 다시 조회하거나 구독으로 확인해야 합니다.

---

## 3. NaviHelper SDK: 내비게이션 연동

- 의존성: `ai.pleos.playground:NaviHelper:3.0.5`
- 퍼미션: `NAVI_ROUTE`(정보 조회), `NAVI_ROUTE_SEARCH`(경로 요청), `NAVI_CUSTOM_MAP`(내비 화면 요청)
- 내비게이션이 탑재된 차량에서만 완전히 동작합니다. 앱이 지도를 직접 그리는 SDK가 아니라, **차량 기본 내비 앱에 요청을 보내고 이벤트를 받는 SDK**입니다.

### 앱이 요청할 수 있는 동작

| API | 내용 |
|---|---|
| `requestRoute(RouteInfo)` | 목적지 설정 (좌표, POI 이름, 주소, 경로 옵션) |
| `requestRouteWithWaypoints(...)` | 목적지 + 경유지 |
| `addWaypoint` / `removeWaypoint` | 경유지 추가·삭제 (**최대 2개**) |
| `cancelRoute()`, `requestReRoute()` | 경로 취소, 재탐색 |
| `changeRouteOption(RouteOption)` | 추천, 최단거리, 최소시간, 무료, 고속도로, 편안한 길, 어린이보호구역 회피 |
| `requestTrip()` | 트립 정보 요청 |
| `requestNavigationScreen()` | 내비 화면 표시 요청 |

### 앱이 받을 수 있는 데이터(이벤트)

| 분류 | 데이터 |
|---|---|
| 경로 | 경로 시작·완료·취소·재탐색, 경로 옵션 변경, 경로 상태 |
| 목적지·경유지 | 남은 거리·시간, 목적지 변경·도착, 경유지 변경·도착 |
| 주행 안내 | **TBT(회전 안내)**, 주행 정보, **현재 도로 제한속도** |
| 안전 | **과속 카메라**(고정·이동·구간단속·스쿨존 등 10종, 남은 거리, 과속 여부), **주의 구간**(사고다발, 낙석, 안개, 급경사, 미끄러운 길, 스쿨존, 야생동물, 역주행 경고 등 20여 종) |
| 개인화 | 즐겨찾기(집·회사·EV 충전소), 최근 목적지, 현재 위치 |
| 기타 | 충전 사업자 정보, 트립 정보, 내비 상태 |

- 앱이 경로를 직접 계산해 넣는 기능(`onUserDefinedRoute`)은 삭제됐습니다.
- 에뮬레이터에서 가상 주행으로 TBT·카메라 이벤트를 재현할 수 있는지는 문서에 없습니다.

---

## 4. 음성·위치·AI 비서

### SpeechToText (v3.3.1, 문서상 Coming Soon)
- 차량 마이크로 음성을 받아 텍스트로 바꿉니다. 중간 결과와 최종 결과를 모두 받습니다.
- 모드: `HYBRID`(기본, 서버 우선·실패 시 온디바이스), `SERVER`, `ON_DEVICE`
- 지원 언어: `ko-KR`, `en-US`, `fr-CA`, `es-US`, `es-MX`
- 서버 모드는 Playground My Project의 Client ID/Secret으로 `registerApp()`을 호출해야 합니다.
- **명령을 실행하지는 않습니다.** 텍스트만 돌려줍니다.

### TextToSpeech (v3.3.1, 문서상 Coming Soon)
- 텍스트를 차량 오디오로 바로 재생합니다. 조각 단위 스트리밍 발화가 가능합니다.
- 화자, 속도, 피치를 고르는 API는 문서에 없습니다. 합성된 오디오 데이터를 받는 API도 없습니다.

### FusedLocation (v1.1.3, 문서상 Coming Soon)
- 차량 위치(위도, 경도, 고도, 진행 방향, 신뢰도)를 받고, **지오펜스(특정 영역 진입·이탈 알림)**를 걸 수 있습니다.
- 차종마다 지원 여부가 다릅니다(`isSupported()`).

### Gleo Interface (차량 AI 비서 연동)
- 앱이 `assets/schema.json`에 기능(function)을 **최대 3개** 정의하면, 사용자가 "글레오, ~해줘"라고 말했을 때 Gleo AI가 발화를 해석해서 앱 기능을 파라미터와 함께 호출합니다.
- 앱은 결과를 broadcast로 돌려주고, Gleo가 그 결과로 음성 답변을 만듭니다. `asking` 상태로 되묻기(멀티턴)도 가능합니다.
- 화면 없이 백그라운드로 처리하는 방식(`component: "service"`)도 있어서 주행 중 시나리오에 유리합니다.
- 스키마는 플랫폼 측 **검토·승인**을 거쳐야 활성화됩니다. 에뮬레이터에서 미리 테스트할 수 있는지는 문서에 없습니다.
- LLM SDK가 종료된 지금, 자연어로 앱을 조작하는 **유일한 공식 경로**입니다.

---

## 5. 앱 설계 제약 (App Market 심사 기준 기반)

> 아래는 App Market 등록 심사 기준입니다. 공모전 심사에 그대로 적용된다는 근거는 없지만, PT에서 "실제 출시 가능성"을 보여 주려면 지키는 편이 좋습니다.

### 주행 중 제한
- 앱 카테고리는 8개입니다: Maps/Navigation, Mobility Services, Lifestyle, Entertainment, Business, Music/Audio, Kids, Social/Communication.
- 주행 중 기능이 일부 허용되는 카테고리는 **Music/Audio, Maps/Navigation, Mobility Services** 세 가지뿐입니다.
- DO를 선언하지 않은 앱은 주행 중 사용할 수 없습니다. 주차 상태에서는 모든 기능이 허용됩니다.
- 주행 중에 막히는 것(지역 공통): 동영상, 설정 화면, QR 로그인, 차량 설정, 긴 텍스트. 북미는 키보드와 다이얼패드도 막힙니다.
- 동영상·게임·브라우저 앱은 정차 중에만 쓸 수 있고, 주행이 시작되면 자동으로 일시정지해야 합니다.
- 구현: AAOS `CarUxRestrictionsManager`로 규제 상태를 구독하고 플래그별로 UI를 바꿉니다.

### 화면
- 앱 영역은 창 2개 구조이고, 앱은 최대 2개까지 동시에 뜹니다.
- portrait(창 1개)와 landscape(창 2개 확장) 레이아웃을 모두 설계해야 합니다. portrait에는 핵심 기능을 둡니다.
- 운전자는 1~2초 안에 정보를 파악할 수 있어야 하고, 터치 응답은 0.25초 이내여야 합니다.
- 화면 크기: Connect-S(17, 14.6, 12.9인치), Connect-W(와이드), Connect-WF. 정확한 해상도(px)는 텍스트 문서에 없고, 폼 팩터 테스트 도구나 "Pleos Connect display guide" 파일에서 확인해야 합니다.

### 성능·보안
- CPU: 주행 중 5% 미만(내비 앱 15% 이하), 정차 중 25% 이하
- RAM: 400 MB 이하
- 앱 실행과 콘텐츠 로드: 10초 이내
- HTTPS(TLS 1.2 이상), 민감정보 암호화, Keystore 사용, 디버그 빌드 금지

### 디자인 리소스
- 폰트: Asta Sans (한국어·영어)
- 아이콘: 48×48dp 그리드, Filled/Outlined
- 앱 아이콘: Android Adaptive Icon 형식. Figma 템플릿이 "Pleos Connect Design Assets"에 있습니다.
- 색상 토큰 값, 타이포 크기표, 해상도는 원문이 이미지라서 텍스트로 추출되지 않았습니다. 디자이너가 원문 페이지와 Design Assets를 직접 확인해야 합니다.

---

## 6. 개발·테스트 환경

| 항목 | 내용 |
|---|---|
| 개발 도구 | Android Studio, Kotlin 또는 Java, minSdk 28 이상, Gradle 8.0 이상 |
| 에뮬레이터 | Pleos Connect Emulator v2.1.0 (Android API 34). Mac(arm64)용 이미지도 있습니다 |
| 설치 URL | Mac: `https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-arm64-sys-img.xml` |
| AVD 설정 | Automotive 폼 팩터, RAM 4 GB, **Cold boot 필수**(Quick boot이면 차량 HAL이 동작하지 않음) |
| 권장 사양 | 외장 GPU 8 GB급(RTX 4060 등). Mac 사양은 문서에 없으므로 실제로 돌려 봐야 합니다 |
| Virtual Console | `adb forward tcp:8088 tcp:8088` 후 브라우저에서 `http://127.0.0.1:8088/` |
| 주행 규제 토글 | `adb shell cmd hmg_car_service --service drivingstate driving_regulation on` / `off` |
| 폼 팩터 테스트 | Go 1.22 이상, host-display-bridge로 가상 디스플레이를 만들어 화면 크기·방향별로 확인 |

### Virtual Console로 바꿔 볼 수 있는 차량 상태
- **Motion**: 기어(P/R/N/D), 브레이크 페달, 주행거리, 조향각, 핸들 열선
- **ADAS**: 크루즈 상태, 크루즈 목표 속도
- **Convenience**: 도어, 도어 잠금, 트렁크, 창문, 착좌, 조명, 방향지시등, 와이퍼, 하이패스 시나리오, 글로브박스
- **Safety**: 경고등(주차 브레이크, 안전벨트, ABS, TPMS 등)
- **Energy**: 주행가능거리, 배터리 SOC·용량, 충전 전류·목표, 충전 포트
- **Climate**: 공조 전체, 시트 열선·통풍
- **Vehicle**: 차량 알림 표시
- **사용 가이드 카드 목록에 없는 것**: 차속, GPS 위치, 연료, 시트 위치, 스마트글래스. 단, 셋업 가이드는 "차량 속도 변경"이 가능하다고 소개하므로 실제 화면에서 확인해야 합니다.

---

## 7. 주최 측에 확인해야 할 사항 (10/28 설명회 질문 후보)

1. STT, TTS, FusedLocation, Vehicle Control이 Playground에서 "Coming Soon"인데, 공모전 기간 중 언제 열리는지
2. 공모전 앱에 Vehicle 제어 퍼미션(`CONTROL_CAR_*`)이 승인되는지, 승인 절차와 소요 기간
3. Gleo Interface 스키마 승인을 공모전 기간 안에 받을 수 있는지, 에뮬레이터에서 Gleo를 테스트할 수 있는지
4. 외부 LLM API(OpenAI, Claude 등)를 앱에서 호출해도 심사상 문제가 없는지. 자체 서버 연결은 제출 가이드에 "외부 서버와 연결이 필요한 앱인 경우 심사 중 바로 서비스 연결이 가능하도록…"이라는 문장이 있어서 허용되는 것으로 봅니다.
5. 에뮬레이터에서 차속과 GPS 위치를 주입하는 방법이 있는지 (주행 시나리오 시연용)
6. 에뮬레이터에서 NaviHelper 이벤트(TBT, 과속카메라)를 재현할 수 있는지
7. PT 심사가 에뮬레이터 기준인지, 실차나 실기기 시연 기회가 있는지
8. 2차 PT 심사 기준 (현재 "추후 안내")

---

## 8. 검증 결과 (주장별 근거 수준)

근거 수준
- **A**: 문서가 아닌 실물로 확인했습니다 (배포 저장소, 공모전 사이트).
- **B**: 원문 문장을 직접 찾아 대조했습니다. 해당 문장을 아래에 인용했습니다.
- **C**: 문서끼리 서로 다르거나 문서에 없어서 실제 환경에서 확인해야 합니다.

| 주장 | 수준 | 근거 |
|---|---|---|
| Vehicle 2.3.2, NaviHelper 3.0.5, STT 3.3.1, TTS 3.3.1, FusedLocation 1.1.3 라이브러리를 누구나 받을 수 있다 | A | `nexus-playground.pleos.ai` maven-metadata를 로그인 없이 조회했습니다 (HTTP 200, 2026-10-13~19 갱신) |
| LLM SDK는 종료됐다 | A+B | 저장소의 LLM은 2025-11-21 이후 갱신이 없습니다. 릴리스 노트: "LLM SDK 제공을 종료합니다. Gleo AI와의 연동이 필요한 경우 Gleo Interface를 이용할 수 있습니다." |
| 공모전 허용 SDK는 Vehicle, NaviHelper, TTS, STT, FusedLocation이다 | A | 공모전 FAQ: "Vehicle SDK, NaviHelper SDK, TextToSpeech SDK, SpeechToText SDK, FusedLocation SDK입니다." |
| Vehicle State, NaviHelper는 신청 없이 바로 쓸 수 있다 | B | create-my-project: "Vehicle State와 NaviHelper SDK가 이 상태에 해당합니다. 이 두 SDK는 신청 없이 바로 사용 가능합니다." |
| STT, TTS, FusedLocation, Vehicle Control은 Coming Soon이다 | **C** | create-my-project 한 문서에만 있습니다. 같은 SDK들이 10월에 새 버전으로 배포됐으므로 이 문장이 최신인지 Playground 콘솔에서 확인해야 합니다 |
| Fleet API는 계약이 필요하다 | B | "API 엔드포인트 정보는 Pleos Fleet 사용 계약 후에 담당자를 통해 제공합니다." |
| Vehicle Data API는 유럽 전용이다 | B | "Vehicle Data API는 현재(2026.8월) 유럽 지역에서만 사용 가능합니다." 지원 국가 목록에 한국이 없습니다 |
| Platform SDK는 privileged 앱 전용이다 | B | "Pleos Platform SDK는 승인된 privileged 앱에 차량 기능을 System API로 제공합니다." |
| Connect Mobile은 사내용이다 | B | "Connect Mobile Console은 … 사내용 백오피스입니다." "사내망에서만 접근할 수 있습니다." |
| 차속은 Vehicle SDK에서 삭제됐고 AAOS로 읽는다 | B | SDK 릴리스 노트: "VehicleSpeedListener 삭제, onSpeedUpdated 제거". FAQ: "`CarPropertyManager`로 `PERF_VEHICLE_SPEED`를 읽습니다." |
| 에뮬레이터에서 차속·GPS를 바꿀 수 없다 | **C** | 사용 가이드 카드 목록에는 없지만, 셋업 가이드에 "차량 속도, 도어, 조명 등 차량 상태 변경"이라는 문장이 있습니다 |
| DO 미선언 앱은 주행 중 쓸 수 없다 | B | 주행 규제 정책 표: "Drive(주행) / 사용 불가" |
| 주행 중 일부 허용 카테고리는 Music/Audio, Maps/Navigation, Mobility Services이다 | B | 카테고리 문서의 "주행 중 기능이 제한적으로 허용될 수 있는 카테고리" 절에 이 세 개만 있습니다 |
| 제어 퍼미션은 App Market 사전 승인이 필요하다 | B | "퍼미션은 Pleos App Market과 사전 협의 및 승인된 범위 내에서만 사용할 수 있습니다." |
| 공모전 앱에 제어 퍼미션이 실제로 승인되는지 | **C** | 문서에 없습니다. 주최 측에 문의해야 합니다 |
| Google Play Services가 없는 환경이다 | B | "현재 Pleos Connect의 non-GAS AAOS 환경에서는…". Firebase 등을 쓸 수 없다는 것은 여기서 이끌어 낸 **추론**입니다 |
| Payment SDK는 준비 중이다 | B | "Payment SDK 서비스는 현재 준비 중이며…" |
| 자체 서버를 붙일 수 있다 | B | 제출 가이드: "외부 서버와 연결이 필요한 앱인 경우 심사 중 바로 서비스 연결이 가능하도록…" |
| Gleo Interface 기능은 최대 3개이고 승인 후 활성화된다 | B | "actionCalls(`array`): … function 목록입니다 (최대 3개)." "승인 후에는 통합이 자동으로 활성화됩니다." |
| 에뮬레이터에 Gleo AI와 내비 앱이 들어 있는지 | **C** | 문서에 언급이 없습니다 |
| CPU 주행 중 5% 미만, RAM 400MB 이하 | B | 성능 가이드: "RAM 사용량은 400 MB 이하로 유지합니다." |
| Vehicle 도메인별 메서드 목록 | A | 문서 사이트 sitemap의 레퍼런스 URL 1,168개에서 메서드 이름을 그대로 추출했습니다 |
| 저장소의 `placesense` 아티팩트 | 참고 | 2025-03의 `V1` 하나뿐이고 문서가 없어서 쓸 수 있는 SDK로 보지 않았습니다 |

## 9. 원본 문서 위치

- 개발 문서: https://document.pleos.ai/docs/pleos-only/getting-started
- SDK 레퍼런스: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/
- 앱 기획·설계 가이드: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/intro
- 디자인 가이드: https://document.pleos.ai/docs/connect/guide/docs-design/Introduction/intro
- Virtual Console 가이드: https://document.pleos.ai/docs/pleos-only/app-test-guide/virtual-console-guide
- 공모전: https://appdevchallenge.auto-hmg.io/
