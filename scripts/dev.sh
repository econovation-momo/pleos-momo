#!/bin/sh
# 개발 보조 명령. 사용법: ./scripts/dev.sh <emulator|console|run|log|apk>
set -e
cd "$(dirname "$0")/.."

SDK="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
ADB="$SDK/platform-tools/adb"
export JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/amazon-corretto-21.jdk/Contents/Home}"

case "$1" in
  emulator)
    # Cold boot 필수(Quick boot이면 VHAL 사용 불가). -allow-host-audio가 없으면 마이크 입력이 0으로 채워져 STT 테스트가 안 됨
    "$SDK/emulator/emulator" -avd Pleos_Connect_v2.1 -no-snapshot -gpu host -allow-host-audio ;;
  console)
    # Pleos Connect Virtual Console: 기어, 가속·브레이크(차속), 도어, 와이퍼, 공조 등
    "$ADB" forward tcp:8088 tcp:8088 && open http://127.0.0.1:8088/ ;;
  run)
    ./gradlew :app:installDebug -q && "$ADB" shell am start -n app.momo/.MainActivity ;;
  log)
    "$ADB" logcat -s PleosCheck:D ;;
  apk)
    # 제출 규칙: apk/팀명_앱이름_release.apk
    name="${2:-econovation_MOMO}"
    ./gradlew :app:assembleRelease -q
    mkdir -p apk && cp app/build/outputs/apk/release/app-release.apk "apk/${name}_release.apk"
    echo "apk/${name}_release.apk" ;;
  *)
    echo "사용법: $0 <emulator|console|run|log|apk [econovation_MOMO]>"; exit 1 ;;
esac
