package io.github.cdsap.jdk.tools.parser

import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class GradleWrapperTest {

    @Test
    fun gradleWrapperUsesExpectedVersion() {
        val wrapperProperties = RepoRoot.resolve("gradle", "wrapper", "gradle-wrapper.properties")
        val distributionUrl = Files.readAllLines(wrapperProperties)
            .first { it.startsWith("distributionUrl=") }
            .substringAfter("distributionUrl=")
            .replace("\\:", ":")

        assertTrue(
            "Expected Gradle 9.7.1 in wrapper properties, but found: $distributionUrl",
            distributionUrl.contains("gradle-9.7.1-")
        )
    }
}
