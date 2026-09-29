package io.github.cdsap.jdk.tools.parser

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files

class PublishingConfigTest {

    private val buildScript = Files.readString(RepoRoot.resolve("core", "build.gradle.kts"))

    @Test
    fun usesCentralPortalSnapshotAndStagingEndpoints() {
        assertTrue(
            buildScript.contains("https://central.sonatype.com/repository/maven-snapshots/"),
            "Expected Central Portal snapshots URL"
        )
        assertTrue(
            buildScript.contains(
                "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/"
            ),
            "Expected OSSRH Staging API deploy URL"
        )
        assertFalse(
            buildScript.contains("s01.oss.sonatype.org"),
            "Legacy OSSRH host must not remain in publishing config"
        )
        assertFalse(
            Regex("""https?://(?:s01\.)?oss\.sonatype\.org""").containsMatchIn(buildScript),
            "Legacy OSSRH host must not remain in publishing config"
        )
    }

    @Test
    fun releaseAndSnapshotCredentialsAreDistinct() {
        assertTrue(
            buildScript.contains("USERNAME_SNAPSHOT"),
            "Snapshots must use USERNAME_SNAPSHOT"
        )
        assertTrue(
            buildScript.contains("PASSWORD_SNAPSHOT"),
            "Snapshots must use PASSWORD_SNAPSHOT"
        )
        assertTrue(
            buildScript.contains("USERNAME_RELEASE"),
            "Release must use USERNAME_RELEASE"
        )
        assertTrue(
            buildScript.contains("PASSWORD_RELEASE"),
            "Release must use PASSWORD_RELEASE"
        )
        assertTrue(
            buildScript.contains("mavenReleaseUsername") &&
                buildScript.contains("mavenReleasePassword"),
            "Release credentials must also be available as gradle properties"
        )
        assertTrue(
            buildScript.contains("mavenSnapshotsUsername") &&
                buildScript.contains("mavenSnapshotsPassword"),
            "Snapshot credentials must also be available as gradle properties"
        )
    }

    @Test
    fun doesNotReadEnvironmentEagerlyAtConfigurationTime() {
        assertFalse(
            buildScript.contains("System.getenv("),
            "Publishing credentials must not use System.getenv()"
        )
        assertTrue(
            buildScript.contains("providers.environmentVariable("),
            "Credentials should use providers.environmentVariable()"
        )
        assertTrue(
            buildScript.contains("providers.gradleProperty("),
            "Credentials/URLs should use providers.gradleProperty()"
        )
        assertFalse(
            buildScript.contains("extra.has("),
            "Signing gate should not use eager extra.has()"
        )
    }

    @Test
    fun supportsLocalDryRunUrlOverrides() {
        assertTrue(
            buildScript.contains("mavenSnapshotsUrl"),
            "mavenSnapshotsUrl override required for local publish dry-run"
        )
        assertTrue(
            buildScript.contains("mavenReleaseUrl"),
            "mavenReleaseUrl override required for local publish dry-run"
        )
    }
}
