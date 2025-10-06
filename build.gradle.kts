// Top-level build.gradle.kts

plugins {
    // Android Gradle Plugin (updated to 8.9.1)
    id("com.android.application") version "8.9.1" apply false
    id("com.android.library") version "8.9.1" apply false

    // Kotlin Plugin
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false

    // Google Services (Firebase)
    id("com.google.gms.google-services") version "4.4.2" apply false
}
