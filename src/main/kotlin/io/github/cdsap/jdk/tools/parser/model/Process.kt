package io.github.cdsap.jdk.tools.parser.model


data class Process(
    val pid: String,
    val max: Double,
    val usage: Double,
    val capacity: Double,
    val gcTime: Double,
    val uptime: Double,
    val typeGc: String,
    val typeProcess: TypeProcess
)

enum class TypeProcess {
    Kotlin,
    Gradle,
    Test
}
