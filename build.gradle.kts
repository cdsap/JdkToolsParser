plugins {
    kotlin("jvm") version "1.8.0"
    application
    `maven-publish`
    `signing`
}

group = "io.github.cdsap"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

kotlin {
    jvmToolchain(11)
}


configure<JavaPluginExtension> {
    withJavadocJar()
    withSourcesJar()
}


publishing {
    repositories {
        maven {
            name = "Snapshots"
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")

            credentials {
                username = System.getenv("USERNAME_SNAPSHOT")
                password = System.getenv("PASSWORD_SNAPSHOT")
            }
        }
        maven {
            name = "Release"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")

            credentials {
                username = System.getenv("USERNAME_SNAPSHOT")
                password = System.getenv("PASSWORD_SNAPSHOT")
            }
        }
    }
    publications {
        create<MavenPublication>("libPublication") {
            from(components["java"])
            artifactId = "jdk-tools-parser"
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                scm {
                    connection.set("scm:git:git://github.com/cdsap/JdkToolsParser/")
                    url.set("https://github.com/cdsap/JdkToolsParser/")
                }
                name.set("jdk-tools-parser")
                url.set("https://github.com/cdsap/JdkToolsParser/")
                description.set(
                    "Parses outputs of jinfo and jstat"
                )
                licenses {
                    license {
                        name.set("The MIT License (MIT)")
                        url.set("https://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("cdsap")
                        name.set("Inaki Villar")
                    }
                }
            }
        }
    }
}

if (extra.has("signing.keyId")) {
    afterEvaluate {
        configure<SigningExtension> {
            (extensions.getByName("publishing") as
                PublishingExtension).publications.forEach {
                sign(it)
            }
        }
    }
}
