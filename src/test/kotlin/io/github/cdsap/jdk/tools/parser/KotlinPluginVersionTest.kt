package io.github.cdsap.jdk.tools.parser

import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

class KotlinPluginVersionTest {

    @Test
    fun kotlinJvmPluginUsesVersionWithoutLegacyUsageDeprecation() {
        val buildScript = Files.readString(Paths.get("build.gradle.kts"))
        val versionMatch = Regex("""kotlin\("jvm"\)\s+version\s+"([^"]+)"""")
            .find(buildScript)
        assertTrue(
            "Expected kotlin(\"jvm\") version declaration in build.gradle.kts",
            versionMatch != null
        )

        val version = versionMatch!!.groupValues[1]
        val parts = version.split(".").mapNotNull { it.toIntOrNull() }
        assertTrue(
            "Expected Kotlin JVM plugin 2.2.x or newer (avoids Gradle 10 legacy Usage " +
                "attribute deprecations from 2.0.21), but found: $version",
            parts.size >= 2 && (parts[0] > 2 || (parts[0] == 2 && parts[1] >= 2))
        )
    }
}
