
plugins {
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    id("org.jetbrains.dokka").version("1.9.20").apply(false)
}

