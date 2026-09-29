package io.github.cdsap.jdk.tools.parser.model

import kotlin.math.pow
import kotlin.math.roundToInt

data class Process(
    val pid: String,
    val max: Double,
    val usage: Double,
    val capacity: Double,
    val gcTime: Double,
    val uptime: Double,
    val typeGc: String,
    val typeProcess: TypeProcess
) {
    companion object {
        fun from(
            pid: String,
            jstat: ProcessJstat,
            jinfo: ProcessJInfo,
            typeProcess: TypeProcess
        ): Process = Process(
            pid = pid,
            max = jinfo.max.toGigsFromBytes(),
            usage = jstat.usage.toGigsFromKb(),
            capacity = jstat.capacity.toGigsFromKb(),
            gcTime = jstat.gcTime.toMinutes(),
            uptime = jstat.uptime.toMinutes(),
            typeGc = jinfo.gcType,
            typeProcess = typeProcess
        )
    }
}

enum class TypeProcess {
    Kotlin,
    Gradle,
    Test
}

fun Double.roundTo(numFractionDigits: Int): Double {
    val factor = 10.0.pow(numFractionDigits.toDouble())
    return (this * factor).roundToInt() / factor
}

fun Double.toGigsFromBytes(): Double {
    return (this / (1048576 * 1024)).roundTo(2)
}

fun Double.toGigsFromKb(): Double {
    return (this / 1048576).roundTo(2)
}

fun Double.toMinutes(): Double {
    return (this / 60).roundTo(2)
}
