package io.github.cdsap.jdk.tools.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.util.concurrent.TimeUnit

class SigningConfigurationTest {

    private val coreBuild = Files.readString(RepoRoot.resolve("core", "build.gradle.kts"))

    @Test
    fun coreBuildScriptDoesNotUseAfterEvaluate() {
        assertFalse(
            "afterEvaluate defeats task configuration avoidance and is incompatible with the Configuration Cache",
            coreBuild.contains("afterEvaluate")
        )
    }

    @Test
    fun signingWiresPublicationsLazilyWithoutCast() {
        assertTrue(
            "Expected pluginManager.withPlugin(\"signing\") so signing reacts without afterEvaluate",
            coreBuild.contains("""pluginManager.withPlugin("signing")""")
        )
        assertTrue(
            "Expected sign(publishing.publications) so later publication additions are signed",
            coreBuild.contains("sign(publishing.publications)")
        )
        assertFalse(
            "Signing must not cast the publishing extension by name",
            coreBuild.contains("""extensions.getByName("publishing")""")
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
            assertTrue("Gradle timed out while verifying configuration cache with signing properties", finished)
            assertEquals(
                "Expected configuration with signing properties and --configuration-cache to succeed.\n$output",
                0,
                process.exitValue()
            )
            assertTrue(
                "Expected configuration cache to store or reuse an entry.\n$output",
                output.contains("Configuration cache entry stored") ||
                    output.contains("Configuration cache entry reused") ||
                    output.contains("Reusing configuration cache")
            )
        } finally {
            gradleUserHome.toFile().deleteRecursively()
        }
    }
}
