package io.github.cdsap.jdk.tools.parser

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files

class ProjectStructureTest {

    @Test
    fun rootProjectHasNoSourceDirectory() {
        assertFalse(
            Files.exists(RepoRoot.resolve("src")),
            "Source must not live in the root project"
        )
    }

    @Test
    fun sourcesLiveUnderCoreSubproject() {
        assertTrue(
            Files.isDirectory(RepoRoot.resolve("core", "src", "main", "kotlin")),
            "Expected main sources under core/"
        )
        assertTrue(
            Files.isDirectory(RepoRoot.resolve("core", "src", "test", "kotlin")),
            "Expected test sources under core/"
        )
    }

    @Test
    fun settingsIncludesCoreSubproject() {
        val settings = Files.readString(RepoRoot.resolve("settings.gradle.kts"))
        assertTrue(
            Regex("""include\(\s*["']:?core["']\s*\)""").containsMatchIn(settings),
            "settings.gradle.kts must include the core subproject"
        )
    }

    @Test
    fun rootBuildScriptAppliesNoSourceCompilingPlugins() {
        val rootBuild = Files.readString(RepoRoot.resolve("build.gradle.kts"))

        assertFalse(
            Regex("""(?m)^\s*application\s*$""").containsMatchIn(rootBuild),
            "Root must not apply the application plugin"
        )

        val kotlinJvmDeclarations = Regex("""alias\(libs\.plugins\.kotlin\.jvm\)[^\n]*""")
            .findAll(rootBuild)
            .map { it.value.trim() }
            .toList()
        assertTrue(
            kotlinJvmDeclarations.isNotEmpty() && kotlinJvmDeclarations.all { it.contains("apply false") },
            "Root may only declare alias(libs.plugins.kotlin.jvm) with apply false, found: $kotlinJvmDeclarations"
        )
    }

    @Test
    fun coreBuildScriptOwnsCompilationAndPublishing() {
        val coreBuild = Files.readString(RepoRoot.resolve("core", "build.gradle.kts"))
        assertTrue(
            coreBuild.contains("alias(libs.plugins.kotlin.jvm)"),
            "core must apply alias(libs.plugins.kotlin.jvm)"
        )
        assertTrue(
            coreBuild.contains("`maven-publish`") || coreBuild.contains("maven-publish"),
            "core must apply maven-publish"
        )
    }
}
