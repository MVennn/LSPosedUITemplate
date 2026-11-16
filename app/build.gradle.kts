import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Miuix（HyperOS 风格 Compose UI）。锁 0.9.3：0.9.4+ 要求 AGP >= 9.1。
val miuixVersion = "0.9.3"

fun flag(name: String): Boolean? = providers.gradleProperty(name).orNull?.toBooleanStrictOrNull()

android {
    namespace = "io.github.MVennn.LsposedUITemplate"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.MVennn.LsposedUITemplate"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        buildConfigField("String", "MIUIX_VERSION", "\"$miuixVersion\"")
        buildConfigField("String", "BUILD_TIME", "\"" + SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date()) + "\"")
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a") }
    }

    buildFeatures { compose = true; buildConfig = true }

    buildTypes {
        debug {
            isDebuggable = flag("tpl.debuggable") ?: false
            val minify = flag("tpl.minifyDebug") ?: true
            isMinifyEnabled = minify
            isShrinkResources = minify
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    androidResources { localeFilters += listOf("zh", "en") }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    packaging { resources { excludes += setOf("META-INF/LICENSE*", "META-INF/NOTICE*") } }
    lint { abortOnError = false; checkReleaseBuilds = false }
}

kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }

dependencies {
    // Xposed API：编译期可见，运行期由框架注入，不能打进包。
    compileOnly("io.github.libxposed:api:102.0.0")
    compileOnly("io.github.libxposed:annotation:1.0.0")
    implementation("io.github.libxposed:service:102.0.0")

    // Compose UI 骨架（与 GSAuxiliary / WxPicLabe 同一套）。
    implementation("top.yukonga.miuix.kmp:miuix-ui:$miuixVersion")
    implementation("top.yukonga.miuix.kmp:miuix-preference:$miuixVersion")
    implementation("androidx.activity:activity-compose:1.12.4")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("io.github.kyant0:backdrop:1.0.6")

    testImplementation("junit:junit:4.13.2")
}
