import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    id("org.jetbrains.dokka").version("1.9.20").apply(false)
    id("io.github.gradle-nexus.publish-plugin").version("2.0.0")
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(gradleLocalProperties(rootDir).getProperty("sonatype.username") ?: System.getenv("OSSRH_USERNAME"))
            password.set(gradleLocalProperties(rootDir).getProperty("sonatype.password") ?: System.getenv("OSSRH_PASSWORD"))
            stagingProfileId.set(gradleLocalProperties(rootDir).getProperty("sonatype.stagingProfileId") ?: System.getenv("OSSRH_STAGING_PROFILE_ID"))
        }
    }
}