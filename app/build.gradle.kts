plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val appVersionCode = 15
val appVersionBase = "1.5"

fun getGitCommitSummary(): String {
    return try {
        val process = ProcessBuilder("git", "log", "-1", "--pretty=%s").start()
        process.inputStream.bufferedReader().readText().trim()
    } catch (_: Exception) {
        "Lucario"
    }
}

val gitCommit = getGitCommitSummary().ifEmpty { "Lucario: Release" }
val pokemonName = if (gitCommit.contains(":")) {
    gitCommit.substringBefore(":").trim()
} else {
    "Lucario"
}
val computedVersionName = "$appVersionBase - $pokemonName"

android {
    namespace = "com.vajra.launcher"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vajra.launcher"
        minSdk = 29
        targetSdk = 35

        versionCode = appVersionCode
        versionName = computedVersionName

        buildConfigField("String", "POKEMON_CODENAME", "\"$pokemonName\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
