# MOMO (모빌리티 모멘트)

> 차 안에서 성격 있는 AI 캐릭터와 탑승자 모두가 음성으로 함께 이야기를 만들고, 직접 만든 캐릭터(스토리 봇)를 마켓에서 나누는 이동형 스토리 플랫폼입니다.

현대자동차그룹 차량용 앱 개발 공모전(Code your App into Pleos App Market) 대학생 부문 출품작입니다. Pleos Connect(Android Automotive OS 14) 위에서 동작합니다.

- **팀:** 에코노베이션 (econovation)
- **APK:** [`apk/econovation_MOMO_release.apk`](apk/econovation_MOMO_release.apk)

## 무엇을 하는 앱인가

| 기능 | 내용 | 차량 연동 |
|---|---|---|
| 도착에 맞춰 끝나는 이야기 | 내비 남은 시간으로 장면 수를 정하고(남은 분 ÷ 2), 60초마다 다시 확인해 3분 넘게 바뀌면 재조정합니다. 남은 5분 이하면 결말로 들어가고 도착 이벤트에서 엔딩 카드를 만듭니다. | NaviHelper `getDestinationInfo`, `onDestinationArrived`, `onReRouteEnded` |
| 탑승자 모두가 주인공 | 좌석 착좌·안전벨트로 탑승 인원을 알아내 좌석마다 역할을 나눕니다. 캐릭터가 이름을 불러 차례를 넘기고, 운전자는 원할 때만 짧은 대사의 조연이 됩니다. | Vehicle `Seat.registerSeatOccupancy`, 안전벨트 상태 |
| 음성 롤플레이 | 듣기 → LLM → 문장 단위 TTS로 대화를 이어 갑니다. 예상 밖의 말도 다음 장면에 반영합니다. | SpeechToText·TextToSpeech 3.3.1, Pleos LLM SDK |
| 말로 봇 만들기 | 자유롭게 말하면 캐릭터·말투·세계관·목소리 네 칸이 채워지고, 빈 칸만 다시 묻습니다. 역할과 경로 길이별 시작 설정은 자동으로 만듭니다. | Pleos LLM SDK |
| 스토리 봇 마켓 | 경로 시간(15분/45분/2시간)·인원·키즈 등급으로 걸러 봇을 고르고, 내가 만든 봇을 검수 요청해 올립니다. | 기본 필터 = 내비 남은 시간 |
| 주행/정차 화면 전환 | 주행 중에는 대표 이미지 1장 고정과 짧은 대사만 보여 주고, 정차하면 장면 그림을 크게 보여 줍니다. 마켓·만들기·설정은 정차 중에만 열립니다. 정차해도 이야기는 멈추지 않습니다. | `CarUxRestrictions`, `distractionOptimized` |
| 키즈 모드·안전 | 키즈 등급 봇만 보여 주고 입력과 출력을 모두 유해 필터로 거릅니다. 창문 밖으로 손·머리를 내미는 행동처럼 차 안에서 위험한 유도는 모드와 상관없이 막습니다. 신고 기능이 있고, 키즈 모드를 끌 때는 부모 PIN을 확인합니다. | |
| 오디오 공존 | 내비 안내가 나오면 볼륨만 줄이고, 통화·음성 명령이 오면 멈췄다가 끊긴 문장부터 다시 말합니다. | `AudioFocusRequest` (`AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK`) |

## 실행 방법

### 1. 개발 환경
| 항목 | 버전 |
|---|---|
| JDK | 17 이상 (개발 기준 Amazon Corretto 21) |
| Gradle | 8.10.2 (wrapper 포함) |
| Android Gradle Plugin | 8.8.2 |
| Kotlin | 2.0.0 (Jetpack Compose) |
| compileSdk / targetSdk / minSdk | 35 / 35 / 28 |
| Pleos SDK | Vehicle 2.3.2, NaviHelper 3.0.5, SpeechToText 3.3.1, TextToSpeech 3.3.1, FusedLocation 1.1.3, LLM 2.1.3.2 |

Pleos SDK는 `https://nexus-playground.pleos.ai/repository/maven-releases/`에서 받습니다(`settings.gradle.kts`에 등록되어 있습니다).

### 2. 빌드와 설치
```bash
./scripts/dev.sh emulator   # 에뮬레이터 실행 (마이크 입력 허용 포함)
./scripts/dev.sh run        # 디버그 빌드 → 설치 → 실행
./scripts/dev.sh apk        # release APK 생성 → apk/econovation_MOMO_release.apk
```
release APK만 설치할 때는 `adb install -r apk/econovation_MOMO_release.apk`를 씁니다.

## 테스트 환경

- **에뮬레이터:** Pleos Connect Emulator v2.1.0 (API 34) 전용 AVD, 1920×1080. 설치는 [Pleos Connect Emulator 설치 가이드](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator)를 따릅니다.
  - Mac(Apple Silicon) 이미지: `https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-arm64-sys-img.xml`
  - AVD 설정: Automotive, RAM 4GB, **Cold boot**(Quick boot이면 차량 신호를 쓸 수 없습니다)
- **개발 머신:** macOS (Apple Silicon)

### 시연 방법
1. 앱을 실행하면 차량 연결 화면(S0)이 내비 목적지와 좌석을 읽습니다. 목적지를 받으면 1.5초 뒤 홈으로 넘어갑니다.
2. 에뮬레이터에 내비 목적지나 좌석 신호가 없으면 화면 우측 하단의 **'시연'** 버튼으로 시뮬레이터 패널을 엽니다. 이 패널에서 목적지(할머니 댁 45분 등), 주행/정차, 시간 배속(1x/10x/30x), 도착 5분 전, 도착, 좌석별 착좌·벨트를 바꿉니다. 실제 SDK에서 받은 값은 패널에 `SDK`로 표시되고, 시뮬레이터로 바꾼 항목만 덮어씁니다.
3. 주행 상태는 Virtual Console(`./scripts/dev.sh console`)에서 기어 D + ACCEL로도 바꿀 수 있습니다.
4. 에뮬레이터 마이크가 동작하지 않으면 adb로 사용자 발화를 넣습니다. 정차 중 화면의 시연 입력 버튼도 같은 효과를 냅니다. 공백이 잘리지 않도록 따옴표를 아래처럼 겹쳐 씁니다. `-allow-host-audio`로 켠 에뮬레이터는 개발 머신 주변 소리를 그대로 인식하므로, 시연 영상을 찍을 때는 조용한 곳에서 찍거나 마이크를 끄고 이 명령을 씁니다.
   ```bash
   adb shell "am broadcast -a app.momo.SAY --es text '어서 도망가!' -p app.momo"
   ```
5. 디버그용 SDK 신호 검증 화면: `adb shell am start -n app.momo/.pleos.SdkCheckActivity`

## APK 위치
`apk/econovation_MOMO_release.apk` (생성: `./scripts/dev.sh apk`)

## 코드 구조
```
app/src/main/java/app/momo/
├── MainActivity.kt, Graph.kt   진입점과 전역 객체 연결
├── core/        봇·세션 모델, 시드 봇 6종, 좌석→역할 배정, 앱 상태(AppStore)
├── engine/      세션 엔진(페이싱·단계·차례·엔딩), 프롬프트 빌더, 안전 필터,
│                LLM 어댑터(PleosLlm ↔ ScriptedLlm 자동 전환), 말로 봇 만들기(BotCreator)
├── car/         차량 신호 어댑터: PleosCarSource(실제 SDK) + SimCarSource(시연) 병합, 시연 패널, 포그라운드 서비스
├── voice/       PleosVoice(STT·TTS·오디오 포커스), TextVoice(백업), adb 발화 주입 리시버
├── pleos/       Pleos SDK 래퍼(Navi·Vehicle·Car·Voice)와 SDK 검증 화면
└── ui/          Compose 화면(S0~S8), 모달(만들기·신고·키즈 PIN·가족 공유), 공용 조각(MomoKit)
```

## 현재 한계와 확인할 점

- **Pleos LLM:** `PleosLlm`으로 연동 코드는 넣었지만, 에뮬레이터에서는 ON_DEVICE 모드가 "on-device LLM is disabled"로 응답하지 않고 SERVER 모드는 앱 등록 토큰이 필요합니다. 그래서 첫 응답이 4초 안에 오지 않으면 미리 써 둔 분기 대본(`ScriptedLlm`)으로 자동 전환하고, 화면에 어느 쪽이 응답했는지 표시합니다. 대회용 LLM 사용 방법은 공지 확인이 필요합니다. 외부 LLM API는 같은 `LlmClient` 인터페이스로 바꿔 끼울 수 있습니다.
- **이미지 생성:** Pleos SDK에 이미지 생성 API가 없고 외부 통신 허용 여부도 공지 확인이 필요해서, 지금은 말한 특징의 키워드로 프리셋 그림을 고릅니다.
- **결제·정산·가족 공유:** 화면만 있고 실제 결제, 포인트 적립, 차량 간 전송은 구현하지 않았습니다(기획 범위).
- **데이터 저장:** 서버가 없어서 만든 봇과 엔딩 카드는 앱이 켜져 있는 동안만 메모리에 남습니다.
- **서명:** release 빌드는 아직 디버그 키로 서명합니다. 제출용 키를 정하면 `app/build.gradle.kts`의 `signingConfig`를 바꿉니다.
- Pleos Connect SDK License Agreement 3.4에 따라 SDK를 디컴파일하거나 리버스 엔지니어링하지 않았습니다. API는 공식 문서와 공개된 사용 예시를 기준으로 썼습니다.
- 제어용 `CONTROL_CAR_*` 퍼미션은 App Market 사전 승인 없이 선언하면 심사를 통과할 수 없어 선언하지 않았습니다.

## 개발 메모: 에뮬레이터 실측 결과

Pleos Connect Emulator v2.1.0에서 직접 확인한 결과입니다(2026-11-08, 11-14).

| 항목 | 결과 | 비고 |
|---|---|---|
| Vehicle SDK 초기화 | ✅ | |
| 좌석별 착좌, 안전벨트 (1·2열 좌우) | ✅ | Virtual Console에 벨트 카드는 없지만 API는 값을 줍니다 |
| 주행 규제 `CarUxRestrictions` | ✅ | 기어 D + 주행 시 `0x50` (NO_VIDEO + NO_SETUP) |
| 차속 `PERF_VEHICLE_SPEED`, 기어 `GEAR_SELECTION` (AAOS 표준) | ✅ | CAR_SPEED는 실행 중 권한 요청이 필요합니다 |
| NaviHelper 연결, 목적지·남은 시간, 경로 요청 | ✅ | `requestRoute`를 부르면 내비 앱이 앞으로 나옵니다 |
| TextToSpeech (ON_DEVICE) | ✅ | 초기화와 발화 모두 성공했습니다 |
| SpeechToText (ON_DEVICE) | ⚠️ | 초기화와 대기(onReady)까지 확인했습니다. 실제 인식은 `-allow-host-audio`로 마이크를 열어야 합니다 |
| Pleos LLM (ON_DEVICE / SERVER) | ❌ | ON_DEVICE는 비활성, SERVER는 토큰이 없어 응답이 없습니다. 대본 폴백으로 동작합니다 |
| 경고등 조회 `getTelltalesInfo` | ❌ | `SecurityException: need Permission` (CAR_SAFETY만으로는 부족합니다) |
| `adb root`, `inject-vhal-event` | ❌ | user 빌드라 막혀 있습니다 |

다른 앱(예: 내비)이 앞으로 나오면 앱 프로세스가 얼어 차량 신호를 바로 처리하지 못합니다. 그래서 앱이 차량 신호를 받기 시작하면 포그라운드 서비스(`MomoService`)로 프로세스를 유지합니다.
