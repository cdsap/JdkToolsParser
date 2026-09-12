plugins {
    kotlin("jvm") version "2.0.21"
    application
    `maven-publish`
    `signing`
}

group = "io.github.cdsap"
version = "0.1.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

kotlin {
    jvmToolchain(17)
}


configure<JavaPluginExtension> {
    withJavadocJar()
    withSourcesJar()
}


fun org.gradle.api.artifacts.repositories.MavenArtifactRepository.credentialsFrom(
    usernameProperty: String,
    passwordProperty: String,
    usernameEnv: String,
    passwordEnv: String,
) {
    val usernameProvider = providers.gradleProperty(usernameProperty)
        .orElse(providers.environmentVariable(usernameEnv))
    val passwordProvider = providers.gradleProperty(passwordProperty)
        .orElse(providers.environmentVariable(passwordEnv))
    // Only attach credentials when supplied — file:// dry-runs reject Authentication.
    if (usernameProvider.isPresent || passwordProvider.isPresent) {
        credentials {
            username = usernameProvider.getOrElse("")
            password = passwordProvider.getOrElse("")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "Snapshots"
            // Central Portal snapshots (OSSRH sunset June 30, 2025):
            // https://central.sonatype.org/publish/publish-portal-snapshots/
            url = uri(
                providers.gradleProperty("mavenSnapshotsUrl")
                    .getOrElse("https://central.sonatype.com/repository/maven-snapshots/")
            )
            credentialsFrom(
                usernameProperty = "mavenSnapshotsUsername",
                passwordProperty = "mavenSnapshotsPassword",
                usernameEnv = "USERNAME_SNAPSHOT",
                passwordEnv = "PASSWORD_SNAPSHOT",
            )
        }
        maven {
            name = "Release"
            // Portal OSSRH Staging API for maven-publish (OSSRH sunset June 30, 2025):
            // https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/
            url = uri(
                providers.gradleProperty("mavenReleaseUrl")
                    .getOrElse(
                        "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/"
                    )
            )
            credentialsFrom(
                usernameProperty = "mavenReleaseUsername",
                passwordProperty = "mavenReleasePassword",
                usernameEnv = "USERNAME_RELEASE",
                passwordEnv = "PASSWORD_RELEASE",
            )
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

if (providers.gradleProperty("signing.keyId").isPresent) {
    afterEvaluate {
        configure<SigningExtension> {
            (extensions.getByName("publishing") as
                PublishingExtension).publications.forEach {
                sign(it)
            }
        }
    }
}
