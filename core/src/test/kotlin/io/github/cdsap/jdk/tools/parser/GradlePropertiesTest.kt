package io.github.cdsap.jdk.tools.parser

import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class GradlePropertiesTest {

    @Test
    fun gradlePropertiesEnablesBuildCacheConfigurationCacheAndUtf8() {
        val properties = Files.readString(RepoRoot.resolve("gradle.properties"))

        assertTrue(
            "Expected kotlin.code.style=official",
            properties.contains("kotlin.code.style=official")
        )
        assertTrue(
            "Expected org.gradle.caching=true",
            Regex("""(?m)^\s*org\.gradle\.caching\s*=\s*true\s*$""").containsMatchIn(properties)
        )
        assertTrue(
            "Expected org.gradle.configuration-cache=true",
            Regex("""(?m)^\s*org\.gradle\.configuration-cache\s*=\s*true\s*$""")
                .containsMatchIn(properties)
        )
        assertTrue(
            "Expected org.gradle.jvmargs to set file.encoding=UTF-8",
            Regex("""(?m)^\s*org\.gradle\.jvmargs\s*=.*-Dfile\.encoding=UTF-8""")
                .containsMatchIn(properties)
        )
    }

    @Test
    fun coreBuildScriptPinsJavaCompileEncodingToUtf8() {
        val coreBuild = Files.readString(RepoRoot.resolve("core", "build.gradle.kts"))

        assertTrue(
            "Expected JavaCompile tasks to set options.encoding to UTF-8",
            coreBuild.contains("tasks.withType<JavaCompile>()") &&
                coreBuild.contains("""options.encoding = "UTF-8"""")
        )
    }
}
