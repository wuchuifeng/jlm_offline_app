import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
    id("therouter")
}

android {
    namespace = "com.jlm.translator"
    compileSdk = libs.versions.compileSdk.get().toIntOrNull()

    defaultConfig {
        applicationId = "com.jlm.translator"
        minSdk = libs.versions.minSdk.get().toIntOrNull()
        targetSdk = libs.versions.targetSdk.get().toIntOrNull()
        versionCode = 9
        versionName = "6.1"

        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
//        ndk {
//            // 支持的CPU架构
//            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
//        }
    }

    // 可选：为不同架构生成单独的APK文件（减小APK大小）
//    splits {
//        abi {
//            isEnable = true
//            reset()
//            include("armeabi-v7a", "arm64-v8a")
//            isUniversalApk = true // 同时生成包含所有架构的通用APK
//        }
//    }

    signingConfigs {
        create("release") {
            storeFile = file("./jlm-offline-translator.jks")
            storePassword = "jlm123456"
            keyAlias = "jlm"
            keyPassword = "jlm123456"
            enableV1Signing = true
            enableV2Signing = true
        }
    }
    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
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
    lint {
        baseline = file("lint-baseline.xml")
        checkDependencies = true
        abortOnError = false
    }
    packaging {
        resources {
            excludes.add("META-INF/DEPENDENCIES")
        }
        resources {
            excludes.add("META-INF/NOTICE")
        }
        resources {
            excludes.add("META-INF/LICENSE")
        }
        resources {
            excludes.add("META-INF/LICENSE.txt")
        }
        resources {
            excludes.add("META-INF/NOTICE.txt")
        }
        resources {
            excludes.add("META-INF/io.netty.versions.properties")
        }
        resources {
            excludes.add("META-INF/INDEX.LIST")
        }
    }
}
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar", "*.aar"), "dir" to "libs")))
    implementation(project(":common"))
    implementation(libs.lottie)
//    implementation(libs.libpag)
    implementation(libs.rangeSeekBar)
    implementation(libs.bundles.room)
    implementation(libs.androidx.interpolator)

    ksp(libs.therouter.apt)
    ksp(libs.room.compiler)

    // Compose
    val composeBom = platform(libs.androidx.compose.bom)
    api(composeBom)
    androidTestApi(composeBom)
    api(libs.bundles.compose.library)
    debugApi(libs.compose.ui.tooling)


    // 阿里相关
//    implementation(libs.alimt20181012)
//    implementation(libs.aliASRCommon)
//    implementation(libs.aliASRNls)
//    implementation(libs.tea.openapi)
//    implementation(libs.aliDashscope)

    // Speech SDK
//    implementation(libs.azureSpeech)
    
    // EasyFloat悬浮窗库
//    implementation(libs.easyFloat)
}
fun checkRelease(): Boolean {
    val runTasks = gradle.startParameter.taskNames
    return runTasks.any { it.contains("assembleRelease") && !it.contains("Debug") }
}

val getVerCode: Int
    get() {
        var code = file("version.properties").takeIf { it.canRead() }?.run {
            val versionProps = Properties()
            versionProps.load(inputStream())
            val currentCode = versionProps["versionCode"].toString().toIntOrNull() ?: 1
            currentCode
        } ?: 1
        if (checkRelease()) {
            println("当前versionCode：$code")
            file("version.properties").takeIf { it.canWrite() }?.run {
                val props = Properties()
                props.load(inputStream())
                code++
                println("自增后versionCode：$code")
                props["versionCode"] = code.toString()
                props.store(this.writer(), null)
            }
        }
        return code
    }