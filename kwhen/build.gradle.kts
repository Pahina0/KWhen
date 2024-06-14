import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.JavadocJar

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish") version "0.28.0"
    id("signing")
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

/**
 * https://vanniktech.github.io/gradle-maven-publish-plugin/central/
 * follow to publish
 *
 * maven central user & pw should be auth token from maven central
 * */

mavenPublishing {
    coordinates("io.github.pahinaa.kwhen", "kwhen", "0.0.1")

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    configure(KotlinMultiplatform(
        javadocJar = JavadocJar.Dokka("dokkaHtml"),
        sourcesJar = true,
        androidVariantsToPublish = listOf("debug", "release"),
    ))

    pom {
        name.set("KWhen")
        description.set("A natural language processor for date and time written for Kotlin Multiplatform")
        inceptionYear.set("2024")
        url.set("https://github.com/Pahina0/KWhen")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("Pahina")
                name.set("Alexander Wu")
                url.set("https://github.com/Pahina0")
            }
        }
        scm {
            url.set("https://github.com/Pahina0/KWhen")
            connection.set("scm:git:git://github.com/Pahina0/KWhen.git")
            developerConnection.set("scm:git:ssh://git@github.com/Pahina0/KWhen.git")
        }
    }
}

