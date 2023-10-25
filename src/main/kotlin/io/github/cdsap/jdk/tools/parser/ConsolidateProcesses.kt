package io.github.cdsap.jdk.tools.parser

import io.github.cdsap.jdk.tools.parser.model.Process
import io.github.cdsap.jdk.tools.parser.model.TypeProcess
import kotlin.math.pow
import kotlin.math.roundToInt

class ConsolidateProcesses {

    fun consolidate(jStatResult: String, jInfoResult: String, typeProcess: TypeProcess): List<Process> {
        val processesConsolidated = mutableListOf<Process>()
        val jInfoData = JInfoData().process(jInfoResult)
        val jStatData = JStatData().process(jStatResult)

        jStatData.forEach {
            if (jInfoData.containsKey(it.key)) {
                processesConsolidated.add(
                    Process(
                        pid = it.key,
                        max = jInfoData[it.key]?.max?.toGigsFromBytes()!!,
                        usage = it.value.usage.toGigsFromKb(),
                        capacity = it.value.capacity.toGigsFromKb(),
                        gcTime = it.value.gcTime.toMinutes(),
                        uptime = it.value.uptime.toMinutes(),
                        typeGc = jInfoData[it.key]?.gcType!!,
                        typeProcess = typeProcess
                    )
                )
            }
        }
        return processesConsolidated
    }
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
