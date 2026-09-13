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
            appliesApplicationPlugin(rootBuild)
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
    fun libraryProjectsDoNotApplyApplicationPlugin() {
        val rootBuild = Files.readString(RepoRoot.resolve("build.gradle.kts"))
        val coreBuild = Files.readString(RepoRoot.resolve("core", "build.gradle.kts"))

        assertFalse(
            "Root must not apply the application plugin (avoids unused startScripts/distTar/distZip)",
            appliesApplicationPlugin(rootBuild)
        )
        assertFalse(
            "core must not apply the application plugin (library has no main entry point)",
            appliesApplicationPlugin(coreBuild)
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
        assertTrue(
            "Published artifactId must remain jdk-tools-parser",
            Regex("""artifactId\s*=\s*"jdk-tools-parser"""").containsMatchIn(coreBuild)
        )
        assertTrue(
            "Publication must still come from the java component",
            coreBuild.contains("""from(components["java"])""")
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
}
