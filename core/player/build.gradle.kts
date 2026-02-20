import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "hu.bbara.purefin.core.player"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(libs.hilt)
    ksp(libs.hilt.compiler)
    implementation(libs.medi3.exoplayer)
    implementation(libs.media3.datasource.okhttp)
    implementation(libs.datastore)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.jellyfin.core)
    implementation(libs.okhttp)
}
