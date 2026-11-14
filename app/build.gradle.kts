plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "app.momo"
    compileSdk = 35

    // CarUxRestrictionsManager, CarPropertyManager 같은 AAOS 표준 API
    useLibrary("android.car")

    defaultConfig {
        applicationId = "app.momo"
        // 공모전·Playground 요건: 서명 키 로테이션 때문에 API 28 이상
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    // 병렬 작업용: ./gradlew installDebug -PidSuffix=.ui 처럼 세션마다 다른 패키지로 같은 에뮬레이터에 설치한다
    providers.gradleProperty("idSuffix").orNull?.let { suffix -> buildTypes.getByName("debug").applicationIdSuffix = suffix }

    buildTypes {
        release {
            isMinifyEnabled = false
            // ponytail: 제출 전까지는 디버그 키로 서명. 제출용 키는 scripts/README 참고 후 교체
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)

    implementation(libs.pleos.vehicle)
    implementation(libs.pleos.navihelper)
    implementation(libs.pleos.stt)
    implementation(libs.pleos.tts)
    implementation(libs.pleos.fusedlocation)
    implementation(libs.pleos.llm)

    testImplementation(libs.junit)
}
