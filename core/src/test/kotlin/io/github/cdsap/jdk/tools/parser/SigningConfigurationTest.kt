package io.github.cdsap.jdk.tools.parser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.util.concurrent.TimeUnit

class SigningConfigurationTest {

    private val coreBuild = Files.readString(RepoRoot.resolve("core", "build.gradle.kts"))

    @Test
    fun coreBuildScriptDoesNotUseAfterEvaluate() {
        assertFalse(
            coreBuild.contains("afterEvaluate"),
            "afterEvaluate defeats task configuration avoidance and is incompatible with the Configuration Cache"
        )
    }

    @Test
    fun signingWiresPublicationsLazilyWithoutCast() {
        assertTrue(
            coreBuild.contains("""pluginManager.withPlugin("signing")"""),
            "Expected pluginManager.withPlugin(\"signing\") so signing reacts without afterEvaluate"
        )
        assertTrue(
            coreBuild.contains("sign(publishing.publications)"),
            "Expected sign(publishing.publications) so later publication additions are signed"
        )
        assertFalse(
            coreBuild.contains("""extensions.getByName("publishing")"""),
            "Signing must not cast the publishing extension by name"
        )
    }

    @Test
    fun configurationCacheWorksWhenSigningPropertiesArePresent() {
        val repoRoot = RepoRoot.find()
        val gradleUserHome = Files.createTempDirectory("jdk-tools-parser-signing-cc")
        try {
            Files.writeString(
                gradleUserHome.resolve("gradle.properties"),
                """
                signing.keyId=00000000
                signing.password=test
                signing.secretKeyRingFile=/dev/null
                """.trimIndent() + "\n"
            )

            val gradlew = repoRoot.resolve("gradlew").toAbsolutePath().toString()
            // Use help (not build) so this check configures signing without re-entering
            // the Gradle test suite that is already running this test.
            val process = ProcessBuilder(
                gradlew,
                "help",
                "--configuration-cache",
                "--no-daemon",
                "-g",
                gradleUserHome.toAbsolutePath().toString()
            )
                .directory(repoRoot.toFile())
                .redirectErrorStream(true)
                .start()

            val finished = process.waitFor(10, TimeUnit.MINUTES)
            val output = process.inputStream.bufferedReader().readText()
            assertTrue(finished, "Gradle timed out while verifying configuration cache with signing properties")
            assertEquals(
                0,
                process.exitValue(),
                "Expected configuration with signing properties and --configuration-cache to succeed.\n$output"
            )
            assertTrue(
                output.contains("Configuration cache entry stored") ||
                    output.contains("Configuration cache entry reused") ||
                    output.contains("Reusing configuration cache"),
                "Expected configuration cache to store or reuse an entry.\n$output"
            )
        } finally {
            gradleUserHome.toFile().deleteRecursively()
        }
    }
}
