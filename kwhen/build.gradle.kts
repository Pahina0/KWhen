import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
    id("signing")
}



kotlin {
    applyDefaultHierarchyTemplate()
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
        commonMain {
            dependencies {
                // dependencies for main
                implementation(libs.kotlinx.datetime)
            }
        }

        commonTest {
            dependencies {
                // dependencies for tests
                implementation(libs.kotlin.test)
            }
        }
    }

}

tasks.withType<Test> {
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

android {
    namespace = "io.github.pahinaa"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
}

/**
 * https://vanniktech.github.io/gradle-maven-publish-plugin/central/
 * follow to publish
 *
 * maven central user & pw should be auth token from maven central
 * */
mavenPublishing {
    coordinates("io.github.pahinaa.kwhen", "kwhen", "0.1.0")

    publishToMavenCentral()
    signAllPublications()

    configure(
        KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka("dokkaGenerate"),
            sourcesJar = true,
            androidVariantsToPublish = listOf("debug", "release"),
        )
    )

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

