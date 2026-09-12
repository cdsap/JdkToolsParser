package io.github.cdsap.jdk.tools.parser

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class ProjectStructureTest {

    @Test
    fun rootProjectHasNoSourceDirectory() {
        assertFalse(
            "Source must not live in the root project",
            Files.exists(RepoRoot.resolve("src"))
        )
    }

    @Test
    fun sourcesLiveUnderCoreSubproject() {
        assertTrue(
            "Expected main sources under core/",
            Files.isDirectory(RepoRoot.resolve("core", "src", "main", "kotlin"))
        )
        assertTrue(
            "Expected test sources under core/",
            Files.isDirectory(RepoRoot.resolve("core", "src", "test", "kotlin"))
        )
    }

    @Test
    fun settingsIncludesCoreSubproject() {
        val settings = Files.readString(RepoRoot.resolve("settings.gradle.kts"))
        assertTrue(
            "settings.gradle.kts must include the core subproject",
            Regex("""include\(\s*["']:?core["']\s*\)""").containsMatchIn(settings)
        )
    }

    @Test
    fun rootBuildScriptAppliesNoSourceCompilingPlugins() {
        val rootBuild = Files.readString(RepoRoot.resolve("build.gradle.kts"))

        assertFalse(
            "Root must not apply the application plugin",
            Regex("""(?m)^\s*application\s*$""").containsMatchIn(rootBuild)
        )

        val kotlinJvmDeclarations = Regex("""kotlin\("jvm"\)[^\n]*""")
            .findAll(rootBuild)
            .map { it.value.trim() }
            .toList()
        assertTrue(
            "Root may only declare kotlin(\"jvm\") with apply false, found: $kotlinJvmDeclarations",
            kotlinJvmDeclarations.isNotEmpty() && kotlinJvmDeclarations.all { it.contains("apply false") }
        )
    }

    @Test
    fun coreBuildScriptOwnsCompilationAndPublishing() {
        val coreBuild = Files.readString(RepoRoot.resolve("core", "build.gradle.kts"))
        assertTrue(
            "core must apply kotlin(\"jvm\")",
            coreBuild.contains("""kotlin("jvm")""")
        )
        assertTrue(
            "core must apply maven-publish",
            coreBuild.contains("`maven-publish`") || coreBuild.contains("maven-publish")
        )
    }

    @Test
    fun dependencyRepositoriesAreCentralizedInSettings() {
        val settings = Files.readString(RepoRoot.resolve("settings.gradle.kts"))
        assertTrue(
            "settings.gradle.kts must set FAIL_ON_PROJECT_REPOS",
            settings.contains("RepositoriesMode.FAIL_ON_PROJECT_REPOS")
        )
        assertTrue(
            "settings.gradle.kts must declare mavenCentral() for dependency resolution",
            Regex("""dependencyResolutionManagement\s*\{[\s\S]*mavenCentral\(\)""")
                .containsMatchIn(settings)
        )

        val coreBuild = Files.readString(RepoRoot.resolve("core", "build.gradle.kts"))
        assertFalse(
            "core/build.gradle.kts must not declare a top-level dependency repositories block",
            Regex("""(?m)^repositories\s*\{""").containsMatchIn(coreBuild)
        )
        assertTrue(
            "publishing repositories must remain in core/build.gradle.kts",
            Regex("""publishing\s*\{[\s\S]*repositories\s*\{""").containsMatchIn(coreBuild)
        )

        val rootBuild = Files.readString(RepoRoot.resolve("build.gradle.kts"))
        assertFalse(
            "root build.gradle.kts must not declare repositories",
            Regex("""(?m)^repositories\s*\{""").containsMatchIn(rootBuild)
        )
    }
}
