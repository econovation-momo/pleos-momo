# (앱 이름 미정)

> 한 줄 소개: (기획 확정 후 작성)

현대자동차그룹 차량용 앱 개발 공모전 출품작입니다. Pleos Connect(Android Automotive OS 14) 위에서 동작합니다.

## 실행 방법

### 1. 개발 환경
| 항목 | 버전 |
|---|---|
| JDK | 17 이상 (개발 기준 Amazon Corretto 21) |
| Gradle | 8.10.2 (wrapper 포함) |
| Android Gradle Plugin | 8.8.2 |
| Kotlin | 2.0.0 |
| compileSdk / targetSdk / minSdk | 35 / 35 / 28 |
| Pleos SDK | Vehicle 2.3.2, NaviHelper 3.0.5, SpeechToText 3.3.1, TextToSpeech 3.3.1, FusedLocation 1.1.3 |

Pleos SDK는 `https://nexus-playground.pleos.ai/repository/maven-releases/`에서 받습니다(`settings.gradle.kts`에 등록됨).

### 2. 에뮬레이터
Pleos Connect Emulator v2.1.0(API 34) 전용 AVD가 필요합니다. 설치는 [Pleos Connect Emulator 설치 가이드](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator)를 따릅니다.
- Mac(Apple Silicon) 이미지: `https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-arm64-sys-img.xml`
- AVD 설정: Automotive, RAM 4GB, **Cold boot**

```bash
./scripts/dev.sh emulator   # 에뮬레이터 실행 (마이크 입력 허용 포함)
./scripts/dev.sh run        # 빌드 → 설치 → 실행
./scripts/dev.sh console    # Virtual Console 열기 (http://127.0.0.1:8088)
./scripts/dev.sh log        # 앱 신호 로그 보기
```

### 3. 테스트 방법
1. 에뮬레이터를 실행하고 앱을 설치합니다.
2. Virtual Console에서 기어를 D로 바꾸고 ACCEL을 누르면 주행 상태(차속, 주행 규제 ON)가 됩니다.
3. 도어, 와이퍼, 착좌 등은 Virtual Console의 각 항목으로 바꿉니다.

## APK 위치
`apk/팀명_앱이름_release.apk` (생성: `./scripts/dev.sh apk 팀명_앱이름`)

---

## 개발 메모: 에뮬레이터 실측 결과 (2026-11-08)

SDK 검증 화면(`MainActivity`)으로 Pleos Connect Emulator v2.1.0에서 직접 확인한 결과입니다.

| 항목 | 결과 | 비고 |
|---|---|---|
| Vehicle SDK 초기화 | ✅ | |
| 좌석별 착좌, 안전벨트 (1·2열 좌우) | ✅ | Virtual Console에 벨트 카드는 없지만 API는 값을 줌 |
| 좌석별 도어, 방향지시등, 브레이크, 와이퍼, 조향각, 배터리 %, 하향등 | ✅ | |
| 실내 현재 온도 | ⚠️ | 운전석만 지원. 뒷좌석 영역은 `not supported` |
| 경고등 조회 `getTelltalesInfo` | ❌ | `SecurityException: need Permission` (CAR_SAFETY만으로 부족) |
| 차속 `PERF_VEHICLE_SPEED`, 기어 `GEAR_SELECTION` (AAOS 표준) | ✅ | CAR_SPEED는 실행 중 권한 요청 필요 |
| 주행 규제 `CarUxRestrictions` | ✅ | 기어 D + 주행 시 `0x50` (NO_VIDEO + NO_SETUP) |
| NaviHelper 연결, 현재 위치, 경로 요청, TBT, 경로 상태 | ✅ | `requestRoute` 시 내비 앱이 앞으로 나옴 |
| TextToSpeech (ON_DEVICE) | ✅ | 초기화·발화 성공. 문서상 "Coming Soon"이지만 에뮬레이터에서 동작 |
| SpeechToText (ON_DEVICE) | ⚠️ | 초기화·대기(onReady)까지 확인. 실제 인식은 `-allow-host-audio`로 마이크를 열고 확인 필요 |
| 공식 ADB 주행 규제 명령 `hmg_car_service` | ❌ | 에뮬레이터에 없음. 대신 Virtual Console의 D + ACCEL 사용 |
| `adb root`, `inject-vhal-event` | ❌ | user 빌드라 막힘 |

**주의할 점**
- 다른 앱(예: 내비)이 앞으로 나오면 우리 앱 프로세스가 얼어서 차량 신호를 바로 처리하지 못합니다. 주행 중 백그라운드 동작이 필요한 기능은 포그라운드 서비스로 옮겨야 합니다.
- 제어용 `CONTROL_CAR_*` 퍼미션은 App Market 사전 승인 없이 선언하면 심사를 통과할 수 없어 선언하지 않았습니다.
- Pleos Connect SDK License Agreement 3.4에 따라 SDK를 디컴파일하거나 리버스 엔지니어링하지 않습니다. API는 공식 문서 기준으로만 사용합니다.

## 코드 구조
```
app/src/main/java/ai/challenge/pleosapp/
├── MainActivity.kt          SDK 검증 화면 (기능 화면을 만들면 디버그 메뉴로 이동)
└── pleos/
    ├── SignalHub.kt         모든 신호를 모으는 곳 (StateFlow)
    ├── VehicleSignals.kt    Vehicle SDK 구독
    ├── NaviSignals.kt       NaviHelper SDK
    ├── CarSignals.kt        AAOS 차속·기어·주행 규제
    └── VoiceIO.kt           STT·TTS
```
