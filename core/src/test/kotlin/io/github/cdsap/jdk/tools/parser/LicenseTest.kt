package io.github.cdsap.jdk.tools.parser

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.util.jar.JarFile

class LicenseTest {

    @Test
    fun licenseFileExistsAtRepoRoot() {
        assertTrue(
            Files.isRegularFile(RepoRoot.resolve("LICENSE")),
            "Expected LICENSE at repository root for GitHub license detection"
        )
    }

    @Test
    fun licenseFileIsMitMatchingPomDeclaration() {
        val license = Files.readString(RepoRoot.resolve("LICENSE"))
        val coreBuild = Files.readString(RepoRoot.resolve("core", "build.gradle.kts"))

        assertTrue(
            license.startsWith("MIT License"),
            "LICENSE must be the MIT License text"
        )
        assertTrue(
            license.contains("Copyright (c) 2023 Iñaki Villar"),
            "LICENSE must include the copyright notice"
        )
        assertTrue(
            license.contains("Permission is hereby granted, free of charge"),
            "LICENSE must include the MIT permission notice"
        )
        assertTrue(
            coreBuild.contains("""name.set("The MIT License (MIT)")""") &&
                coreBuild.contains("""url.set("https://opensource.org/licenses/MIT")"""),
            "POM must continue declaring The MIT License (MIT)"
        )
    }

    @Test
    fun coreBuildPacksLicenseIntoPublishedJars() {
        val coreBuild = Files.readString(RepoRoot.resolve("core", "build.gradle.kts"))

        assertTrue(
            Regex(
                """tasks\.withType<\s*Jar\s*>\(\)\s*\.configureEach\s*\{[^}]*from\(rootDir\)\s*\{[^}]*include\("LICENSE"\)[^}]*into\("META-INF"\)""",
                RegexOption.DOT_MATCHES_ALL
            ).containsMatchIn(coreBuild),
            "core must package LICENSE into Jar META-INF"
        )
    }

    @Test
    fun builtJarContainsLicenseUnderMetaInf() {
        val libsDir = RepoRoot.resolve("core", "build", "libs")
        assertTrue(Files.isDirectory(libsDir), "Expected core/build/libs after jar task")

        val jarPath = Files.list(libsDir).use { paths ->
            paths.filter { path ->
                val name = path.fileName.toString()
                name.startsWith("core-") &&
                    name.endsWith(".jar") &&
                    !name.contains("-sources") &&
                    !name.contains("-javadoc")
            }.findFirst().orElse(null)
        }
        assertTrue(jarPath != null, "Expected a core-*.jar under core/build/libs")

        JarFile(jarPath!!.toFile()).use { jar ->
            val entry = jar.getEntry("META-INF/LICENSE")
            assertTrue(entry != null, "Published jar must include META-INF/LICENSE")
            val text = jar.getInputStream(entry).bufferedReader().readText()
            assertTrue(
                text.contains("MIT License") && text.contains("Copyright (c) 2023 Iñaki Villar"),
                "META-INF/LICENSE must contain MIT license text"
            )
        }
    }
}
