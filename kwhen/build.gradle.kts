import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("module.publication")
}

kotlin {
    kotlin.applyDefaultHierarchyTemplate()
    jvm()
    androidTarget {
        publishLibraryVariants("release")

        tasks.withType<KotlinJvmCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(JvmTarget.DEFAULT)
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    linuxX64()

    mingwX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                // dependencies for main
                implementation(libs.kotlinx.datetime)
            }
        }
        val commonTest by getting {
            dependencies {
                // dependencies for tests
                implementation(libs.kotlin.test)
            }
        }
    }
}

android {
    namespace = "io.github.pahinaa"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
