package io.github.cdsap.jdk.tools.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class GradleWrapperTest {

    private companion object {
        const val EXPECTED_GRADLE_VERSION = "9.7.1"
        // Published at https://gradle.org/release-checksums/ for gradle-9.7.1-bin.zip
        const val EXPECTED_DISTRIBUTION_SHA256 =
            "acd53f1edaf02f1a8ff99879f8a34b302661a057d9b063ae9e35b552f804d20a"
    }

    private val wrapperProperties: Map<String, String> by lazy {
        Files.readAllLines(RepoRoot.resolve("gradle", "wrapper", "gradle-wrapper.properties"))
            .filter { it.isNotBlank() && !it.trimStart().startsWith("#") }
            .associate { line ->
                val key = line.substringBefore("=")
                val value = line.substringAfter("=")
                key to value
            }
    }

    @Test
    fun gradleWrapperUsesExpectedVersion() {
        val distributionUrl = wrapperProperties.getValue("distributionUrl").replace("\\:", ":")

        assertTrue(
            "Expected Gradle $EXPECTED_GRADLE_VERSION in wrapper properties, but found: $distributionUrl",
            distributionUrl.contains("gradle-$EXPECTED_GRADLE_VERSION-")
        )
    }

    @Test
    fun gradleWrapperPinsDistributionSha256Sum() {
        val distributionSha256Sum = wrapperProperties["distributionSha256Sum"]
        assertEquals(
            "distributionSha256Sum must match the published checksum for gradle-$EXPECTED_GRADLE_VERSION-bin.zip",
            EXPECTED_DISTRIBUTION_SHA256,
            distributionSha256Sum
        )
    }
}
