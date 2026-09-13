package io.github.cdsap.jdk.tools.parser

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class BuildWorkflowTest {

    private val workflow = Files.readString(RepoRoot.resolve(".github", "workflows", "build.yaml"))

    @Test
    fun usesSetupGradleActionNotDeprecatedBuildAction() {
        assertTrue(
            "Expected gradle/actions/setup-gradle in the CI workflow",
            workflow.contains("uses: gradle/actions/setup-gradle@")
        )
        assertFalse(
            "Deprecated gradle/gradle-build-action must not be used",
            workflow.contains("gradle/gradle-build-action")
        )
    }

    @Test
    fun runsBuildNotOnlyTest() {
        assertTrue(
            "Expected CI to run ./gradlew build",
            workflow.contains("./gradlew build")
        )
        assertFalse(
            "CI must not run only ./gradlew test",
            Regex("""run:\s*./gradlew test\b""").containsMatchIn(workflow)
        )
    }

    @Test
    fun usesCurrentMajorActions() {
        assertTrue(
            "Expected actions/checkout@v4 or newer",
            Regex("""uses:\s*actions/checkout@v([4-9]|\d{2,})""").containsMatchIn(workflow)
        )
        assertTrue(
            "Expected actions/setup-java@v4 or newer",
            Regex("""uses:\s*actions/setup-java@v([4-9]|\d{2,})""").containsMatchIn(workflow)
        )
    }
}
