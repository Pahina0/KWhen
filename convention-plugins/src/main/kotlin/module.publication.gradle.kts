import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`

plugins {
    `maven-publish`
    signing
}

publishing {
    // Configure all publications
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(tasks.register("${name}JavadocJar", Jar::class) {
            archiveClassifier.set("javadoc")
            archiveAppendix.set(this@withType.name)
        })

        // Provide artifacts information required by Maven Central
        pom {
            // TODO change stuff here
            name.set("KWhen")
            description.set("A natural language processor for date and time")
            url.set("https://github.com/Pahina0/KWhen")

            licenses {
                license {
                    name.set("Apache 2.0")
                    url.set("https://opensource.org/license/apache-2-0")
                }
            }

            developers {
                developer {
                    name.set("Alexander Wu")
                }
            }
            scm {
                url.set("https://github.com/Pahina0/KWhen")
            }
        }
    }
}

signing {
    if (project.hasProperty("signing.gnupg.keyName")) {
        useGpgCmd()
        sign(publishing.publications)
    }
}
