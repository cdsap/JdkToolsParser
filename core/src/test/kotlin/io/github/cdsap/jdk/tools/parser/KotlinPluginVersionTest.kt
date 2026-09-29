package io.github.cdsap.jdk.tools.parser

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files

class KotlinPluginVersionTest {

    @Test
    fun kotlinJvmPluginUsesVersionWithoutLegacyUsageDeprecation() {
        val catalog = Files.readString(RepoRoot.resolve("gradle", "libs.versions.toml"))
        val versionMatch = Regex("""(?m)^kotlin\s*=\s*"([^"]+)"""")
            .find(catalog)
        assertTrue(
            versionMatch != null,
            "Expected Kotlin version declaration in gradle/libs.versions.toml"
        )

        val version = versionMatch!!.groupValues[1]
        val parts = version.split(".").mapNotNull { it.toIntOrNull() }
        assertTrue(
            parts.size >= 2 && (parts[0] > 2 || (parts[0] == 2 && parts[1] >= 2)),
            "Expected Kotlin JVM plugin 2.2.x or newer (avoids Gradle 10 legacy Usage " +
                "attribute deprecations from 2.0.21), but found: $version"
        )
    }
}
