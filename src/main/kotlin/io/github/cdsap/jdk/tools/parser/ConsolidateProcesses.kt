package io.github.cdsap.jdk.tools.parser

import io.github.cdsap.jdk.tools.parser.model.Process
import io.github.cdsap.jdk.tools.parser.model.TypeProcess
import kotlin.math.pow
import kotlin.math.roundToInt

class ConsolidateProcesses(
    private val jInfoData: JInfoData = JInfoData(),
    private val jStatData: JStatData = JStatData()
) {

    fun consolidate(jStatResult: String, jInfoResult: String, typeProcess: TypeProcess): List<Process> {
        val processesConsolidated = mutableListOf<Process>()
        val jInfoProcesses = jInfoData.process(jInfoResult)
        val jStatProcesses = jStatData.process(jStatResult)

        jStatProcesses.forEach { (pid, jStat) ->
            val jInfo = jInfoProcesses[pid] ?: return@forEach
            processesConsolidated.add(
                Process(
                    pid = pid,
                    max = jInfo.max.toGigsFromBytes(),
                    usage = jStat.usage.toGigsFromKb(),
                    capacity = jStat.capacity.toGigsFromKb(),
                    gcTime = jStat.gcTime.toMinutes(),
                    uptime = jStat.uptime.toMinutes(),
                    typeGc = jInfo.gcType,
                    typeProcess = typeProcess
                )
            )
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
