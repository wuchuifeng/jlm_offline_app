plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "cn.chawloo.base"
    compileSdk = libs.versions.compileSdk.get().toIntOrNull()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toIntOrNull()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    api(libs.material)
    api(libs.androidautosize)
    api(libs.fastjson)
    api(libs.kotlinx.serialization.json)
    api(libs.okhttp)
    api(libs.net)
    api(libs.brv)
    api(libs.crashReport)
    api(libs.therouter)
    api(libs.basePopup)
    api(libs.mmkv)
    api(libs.xPermission)
    api(libs.wheelView)
    api(libs.toast)
    api(libs.x5webview)
    api(libs.viewbinding.ktx)
//    api(libs.wechat.sdk.android.without.mta)
    ksp(libs.therouter.apt)
    api(libs.flexbox)
//    api(libs.banner)
    api(libs.joda.time)
    api(libs.shapeView)
    api(libs.voiceWaveAnim)

    api(libs.bundles.kotlin)
    api(libs.bundles.coroutines)
    api(libs.bundles.androidx)
    api(libs.bundles.pictureSelector)
    api(libs.bundles.immersionbar)
    api(libs.bundles.coil)
    api(libs.bundles.saf.log)
    api(libs.bundles.room)
    api(libs.bundles.umeng)

    val composeBom = platform(libs.androidx.compose.bom)
    api(composeBom)
    androidTestApi(composeBom)
    api(libs.bundles.compose.library)
    debugApi(libs.compose.ui.tooling)
}