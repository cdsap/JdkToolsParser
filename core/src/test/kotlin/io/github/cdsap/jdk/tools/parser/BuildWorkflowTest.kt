package io.github.cdsap.jdk.tools.parser

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files

class BuildWorkflowTest {

    private val workflow = Files.readString(RepoRoot.resolve(".github", "workflows", "build.yaml"))

    @Test
    fun usesSetupGradleActionNotDeprecatedBuildAction() {
        assertTrue(
            workflow.contains("uses: gradle/actions/setup-gradle@"),
            "Expected gradle/actions/setup-gradle in the CI workflow"
        )
        assertFalse(
            workflow.contains("gradle/gradle-build-action"),
            "Deprecated gradle/gradle-build-action must not be used"
        )
    }

    @Test
    fun runsBuildNotOnlyTest() {
        assertTrue(
            workflow.contains("./gradlew build"),
            "Expected CI to run ./gradlew build"
        )
        assertFalse(
            Regex("""run:\s*./gradlew test\b""").containsMatchIn(workflow),
            "CI must not run only ./gradlew test"
        )
    }

    @Test
    fun usesCurrentMajorActions() {
        assertTrue(
            Regex("""uses:\s*actions/checkout@v([4-9]|\d{2,})""").containsMatchIn(workflow),
            "Expected actions/checkout@v4 or newer"
        )
        assertTrue(
            Regex("""uses:\s*actions/setup-java@v([4-9]|\d{2,})""").containsMatchIn(workflow),
            "Expected actions/setup-java@v4 or newer"
        )
    }
}
