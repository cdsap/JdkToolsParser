package io.github.cdsap.jdk.tools.parser.model

data class ProcessJstat(
    val usage: Double,
    val capacity: Double,
    val gcTime: Double,
    val uptime: Double
)
