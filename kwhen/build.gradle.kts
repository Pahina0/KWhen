import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("org.jetbrains.dokka")
    id("maven-publish")
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

val dokkaOutputDir = buildDir.resolve("dokka")
tasks.dokkaHtml { outputDirectory.set(file(dokkaOutputDir)) }
val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") { delete(dokkaOutputDir) }
val javadocJar = tasks.create<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
    from(dokkaOutputDir)
}


group = "io.github.pahinaa"
version = "0.0.1"

publishing {
    publications {
        publications.withType<MavenPublication> {
            artifact(javadocJar)

            pom {
                name.set("KWhen")
                description.set("A natural language processor for date and time written for Kotlin Multiplatform")
                url.set("https://github.com/Pahina0/KWhen")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                issueManagement {
                    system.set("GitHub Issues")
                    url.set("https://github.com/Pahina0/KWhen/issues")
                }

                developers {
                    developer {
                        id.set("Pahina") // Change here
                        name.set("Alexander Wu") // Change here
                        email.set("wly.alexander@gmail.com") // Change here
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/Pahina0/KWhen.git")
                    developerConnection.set("scm:git:ssh://github.com/Pahina0/KWhen.git")
                    url.set("https://github.com/Pahina0/KWhen")
                }
            }
        }
    }
}

signing {
    if (project.hasProperty("signing.gnupg.keyName")) {
        println("Signing lib...")
        useGpgCmd()
        sign(publishing.publications)
    }
}