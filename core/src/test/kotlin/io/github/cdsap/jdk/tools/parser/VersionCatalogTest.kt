package io.github.cdsap.jdk.tools.parser

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files

class VersionCatalogTest {

    private val catalog = Files.readString(RepoRoot.resolve("gradle", "libs.versions.toml"))
    private val rootBuild = Files.readString(RepoRoot.resolve("build.gradle.kts"))
    private val coreBuild = Files.readString(RepoRoot.resolve("core", "build.gradle.kts"))

    @Test
    fun versionCatalogDeclaresKotlinAndJunit() {
        assertTrue(
            Files.isRegularFile(RepoRoot.resolve("gradle", "libs.versions.toml")),
            "Expected gradle/libs.versions.toml"
        )
        assertTrue(
            Regex("""(?m)^kotlin\s*=\s*"[^"]+"""").containsMatchIn(catalog),
            "Catalog must declare a kotlin version"
        )
        assertTrue(
            Regex("""(?m)^junit\s*=\s*"[^"]+"""").containsMatchIn(catalog),
            "Catalog must declare a junit version"
        )
        assertTrue(
            catalog.contains("junit-jupiter"),
            "Catalog must declare junit-jupiter library"
        )
        assertTrue(
            catalog.contains("junit-platform-launcher"),
            "Catalog must declare junit-platform-launcher for Gradle 9+ test runtime"
        )
        assertTrue(
            catalog.contains("kotlin-jvm"),
            "Catalog must declare kotlin-jvm plugin"
        )
    }

    @Test
    fun buildScriptsConsumeCatalogWithoutHardcodedDependencyVersions() {
        assertTrue(
            rootBuild.contains("alias(libs.plugins.kotlin.jvm)"),
            "Root build must use the kotlin-jvm catalog plugin alias"
        )
        assertTrue(
            coreBuild.contains("alias(libs.plugins.kotlin.jvm)"),
            "Core build must use the kotlin-jvm catalog plugin alias"
        )
        assertTrue(
            coreBuild.contains("libs.junit.jupiter"),
            "Core build must use the junit-jupiter catalog library"
        )
        assertTrue(
            coreBuild.contains("libs.junit.platform.launcher"),
            "Core build must use the junit-platform-launcher catalog library"
        )

        val hardcodedDependencyVersion = Regex(
            """(?:implementation|api|compileOnly|runtimeOnly|testImplementation|testRuntimeOnly)\s*\(\s*"[^"]+:[^"]+:[^"]+"\s*\)"""
        )
        val hardcodedPluginVersion = Regex("""(?:id|kotlin)\([^)]+\)\s+version\s+"[^"]+"""")

        assertFalse(
            hardcodedDependencyVersion.containsMatchIn(rootBuild) ||
                hardcodedDependencyVersion.containsMatchIn(coreBuild),
            "Build scripts must not hardcode dependency versions"
        )
        assertFalse(
            hardcodedPluginVersion.containsMatchIn(rootBuild) ||
                hardcodedPluginVersion.containsMatchIn(coreBuild),
            "Build scripts must not hardcode plugin versions"
        )
    }
}
