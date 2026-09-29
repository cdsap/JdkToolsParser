package io.github.cdsap.jdk.tools.parser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files

class ProjectStructureTest {

    @Test
    fun rootProjectNameMatchesPublishedArtifactId() {
        val settings = Files.readString(RepoRoot.resolve("settings.gradle.kts"))
        val coreBuild = Files.readString(RepoRoot.resolve("core", "build.gradle.kts"))

        val rootProjectName = Regex("""rootProject\.name\s*=\s*"([^"]+)"""")
            .find(settings)
            ?.groupValues
            ?.get(1)
            ?: error("rootProject.name not found in settings.gradle.kts")

        val artifactId = Regex("""artifactId\s*=\s*"([^"]+)"""")
            .find(coreBuild)
            ?.groupValues
            ?.get(1)
            ?: error("artifactId not found in core/build.gradle.kts")

        assertEquals(
            "jdk-tools-parser",
            artifactId,
            "Published artifactId must stay jdk-tools-parser (changing it breaks consumers)"
        )
        assertEquals(
            artifactId,
            rootProjectName,
            "rootProject.name must match the published artifactId"
        )
    }

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
            appliesApplicationPlugin(rootBuild),
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
    fun libraryProjectsDoNotApplyApplicationPlugin() {
        val rootBuild = Files.readString(RepoRoot.resolve("build.gradle.kts"))
        val coreBuild = Files.readString(RepoRoot.resolve("core", "build.gradle.kts"))

        assertFalse(
            appliesApplicationPlugin(rootBuild),
            "Root must not apply the application plugin (avoids unused startScripts/distTar/distZip)"
        )
        assertFalse(
            appliesApplicationPlugin(coreBuild),
            "core must not apply the application plugin (library has no main entry point)"
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
        assertTrue(
            Regex("""artifactId\s*=\s*"jdk-tools-parser"""").containsMatchIn(coreBuild),
            "Published artifactId must remain jdk-tools-parser"
        )
        assertTrue(
            coreBuild.contains("""from(components["java"])"""),
            "Publication must still come from the java component"
        )
    }

    private fun appliesApplicationPlugin(buildScript: String): Boolean {
        val patterns = listOf(
            Regex("""(?m)^\s*application\s*$"""),
            Regex("""(?m)^\s*id\(\s*["']application["']\s*\)"""),
            Regex("""(?m)^\s*id\(\s*["']org\.gradle\.application["']\s*\)"""),
            Regex("""(?m)^\s*alias\(\s*[\w.]+\.application\s*\)""")
        )
        return patterns.any { it.containsMatchIn(buildScript) }
    }

    @Test
    fun dependencyRepositoriesAreCentralizedInSettings() {
        val settings = Files.readString(RepoRoot.resolve("settings.gradle.kts"))
        assertTrue(
            settings.contains("RepositoriesMode.FAIL_ON_PROJECT_REPOS"),
            "settings.gradle.kts must set FAIL_ON_PROJECT_REPOS"
        )
        assertTrue(
            Regex("""dependencyResolutionManagement\s*\{[\s\S]*mavenCentral\(\)""")
                .containsMatchIn(settings),
            "settings.gradle.kts must declare mavenCentral() for dependency resolution"
        )

        val coreBuild = Files.readString(RepoRoot.resolve("core", "build.gradle.kts"))
        assertFalse(
            Regex("""(?m)^repositories\s*\{""").containsMatchIn(coreBuild),
            "core/build.gradle.kts must not declare a top-level dependency repositories block"
        )
        assertTrue(
            Regex("""publishing\s*\{[\s\S]*repositories\s*\{""").containsMatchIn(coreBuild),
            "publishing repositories must remain in core/build.gradle.kts"
        )

        val rootBuild = Files.readString(RepoRoot.resolve("build.gradle.kts"))
        assertFalse(
            Regex("""(?m)^repositories\s*\{""").containsMatchIn(rootBuild),
            "root build.gradle.kts must not declare repositories"
        )
    }
}
