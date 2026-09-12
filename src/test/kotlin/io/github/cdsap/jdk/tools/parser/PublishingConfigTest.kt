package io.github.cdsap.jdk.tools.parser

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

class PublishingConfigTest {

    private val buildScript = Files.readString(Paths.get("build.gradle.kts"))

    @Test
    fun usesCentralPortalSnapshotAndStagingEndpoints() {
        assertTrue(
            "Expected Central Portal snapshots URL",
            buildScript.contains("https://central.sonatype.com/repository/maven-snapshots/")
        )
        assertTrue(
            "Expected OSSRH Staging API deploy URL",
            buildScript.contains(
                "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/"
            )
        )
        assertFalse(
            "Legacy OSSRH host must not remain in publishing config",
            buildScript.contains("s01.oss.sonatype.org")
        )
        assertFalse(
            "Legacy OSSRH host must not remain in publishing config",
            Regex("""https?://(?:s01\.)?oss\.sonatype\.org""").containsMatchIn(buildScript)
        )
    }

    @Test
    fun releaseAndSnapshotCredentialsAreDistinct() {
        assertTrue(
            "Snapshots must use USERNAME_SNAPSHOT",
            buildScript.contains("USERNAME_SNAPSHOT")
        )
        assertTrue(
            "Snapshots must use PASSWORD_SNAPSHOT",
            buildScript.contains("PASSWORD_SNAPSHOT")
        )
        assertTrue(
            "Release must use USERNAME_RELEASE",
            buildScript.contains("USERNAME_RELEASE")
        )
        assertTrue(
            "Release must use PASSWORD_RELEASE",
            buildScript.contains("PASSWORD_RELEASE")
        )
        assertTrue(
            "Release credentials must also be available as gradle properties",
            buildScript.contains("mavenReleaseUsername") &&
                buildScript.contains("mavenReleasePassword")
        )
        assertTrue(
            "Snapshot credentials must also be available as gradle properties",
            buildScript.contains("mavenSnapshotsUsername") &&
                buildScript.contains("mavenSnapshotsPassword")
        )
    }

    @Test
    fun doesNotReadEnvironmentEagerlyAtConfigurationTime() {
        assertFalse(
            "Publishing credentials must not use System.getenv()",
            buildScript.contains("System.getenv(")
        )
        assertTrue(
            "Credentials should use providers.environmentVariable()",
            buildScript.contains("providers.environmentVariable(")
        )
        assertTrue(
            "Credentials/URLs should use providers.gradleProperty()",
            buildScript.contains("providers.gradleProperty(")
        )
        assertFalse(
            "Signing gate should not use eager extra.has()",
            buildScript.contains("extra.has(")
        )
    }

    @Test
    fun supportsLocalDryRunUrlOverrides() {
        assertTrue(
            "mavenSnapshotsUrl override required for local publish dry-run",
            buildScript.contains("mavenSnapshotsUrl")
        )
        assertTrue(
            "mavenReleaseUrl override required for local publish dry-run",
            buildScript.contains("mavenReleaseUrl")
        )
    }
}
