package io.github.cdsap.jdk.tools.parser

import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.util.jar.JarFile

class LicenseTest {

    @Test
    fun licenseFileExistsAtRepoRoot() {
        assertTrue(
            "Expected LICENSE at repository root for GitHub license detection",
            Files.isRegularFile(RepoRoot.resolve("LICENSE"))
        )
    }

    @Test
    fun licenseFileIsMitMatchingPomDeclaration() {
        val license = Files.readString(RepoRoot.resolve("LICENSE"))
        val coreBuild = Files.readString(RepoRoot.resolve("core", "build.gradle.kts"))

        assertTrue(
            "LICENSE must be the MIT License text",
            license.startsWith("MIT License")
        )
        assertTrue(
            "LICENSE must include the copyright notice",
            license.contains("Copyright (c) 2023 Iñaki Villar")
        )
        assertTrue(
            "LICENSE must include the MIT permission notice",
            license.contains("Permission is hereby granted, free of charge")
        )
        assertTrue(
            "POM must continue declaring The MIT License (MIT)",
            coreBuild.contains("""name.set("The MIT License (MIT)")""") &&
                coreBuild.contains("""url.set("https://opensource.org/licenses/MIT")""")
        )
    }

    @Test
    fun coreBuildPacksLicenseIntoPublishedJars() {
        val coreBuild = Files.readString(RepoRoot.resolve("core", "build.gradle.kts"))

        assertTrue(
            "core must package LICENSE into Jar META-INF",
            Regex(
                """tasks\.withType<\s*Jar\s*>\(\)\s*\.configureEach\s*\{[^}]*from\(rootDir\)\s*\{[^}]*include\("LICENSE"\)[^}]*into\("META-INF"\)""",
                RegexOption.DOT_MATCHES_ALL
            ).containsMatchIn(coreBuild)
        )
    }

    @Test
    fun builtJarContainsLicenseUnderMetaInf() {
        val libsDir = RepoRoot.resolve("core", "build", "libs")
        assertTrue("Expected core/build/libs after jar task", Files.isDirectory(libsDir))

        val jarPath = Files.list(libsDir).use { paths ->
            paths.filter { path ->
                val name = path.fileName.toString()
                name.startsWith("core-") &&
                    name.endsWith(".jar") &&
                    !name.contains("-sources") &&
                    !name.contains("-javadoc")
            }.findFirst().orElse(null)
        }
        assertTrue("Expected a core-*.jar under core/build/libs", jarPath != null)

        JarFile(jarPath!!.toFile()).use { jar ->
            val entry = jar.getEntry("META-INF/LICENSE")
            assertTrue("Published jar must include META-INF/LICENSE", entry != null)
            val text = jar.getInputStream(entry).bufferedReader().readText()
            assertTrue(
                "META-INF/LICENSE must contain MIT license text",
                text.contains("MIT License") && text.contains("Copyright (c) 2023 Iñaki Villar")
            )
        }
    }
}
