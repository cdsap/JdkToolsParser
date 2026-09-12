package io.github.cdsap.jdk.tools.parser

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

internal object RepoRoot {
    fun find(): Path {
        var dir = Paths.get("").toAbsolutePath().normalize()
        while (true) {
            if (Files.isRegularFile(dir.resolve("settings.gradle.kts")) &&
                Files.isRegularFile(dir.resolve("gradlew"))
            ) {
                return dir
            }
            dir = dir.parent
                ?: error("Could not locate repository root from ${Paths.get("").toAbsolutePath()}")
        }
    }

    fun resolve(first: String, vararg more: String): Path =
        find().resolve(Paths.get(first, *more))
}
