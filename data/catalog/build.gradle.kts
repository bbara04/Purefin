import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "hu.bbara.purefin.data.catalog"
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
    implementation(project(":core:image"))
    implementation(project(":data:jellyfin"))
    implementation(project(":data:offline"))
    implementation(libs.jellyfin.core)
    implementation(libs.hilt)
    ksp(libs.hilt.compiler)
    implementation(libs.datastore)
    testImplementation(libs.junit)
}
