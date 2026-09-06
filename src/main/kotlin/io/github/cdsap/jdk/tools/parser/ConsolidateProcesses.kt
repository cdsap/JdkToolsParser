package io.github.cdsap.jdk.tools.parser

import io.github.cdsap.jdk.tools.parser.model.Process
import io.github.cdsap.jdk.tools.parser.model.TypeProcess

class ConsolidateProcesses(
    private val jInfoData: JInfoData = JInfoData(),
    private val jStatData: JStatData = JStatData()
) {

    fun consolidate(jStatResult: String, jInfoResult: String, typeProcess: TypeProcess): List<Process> {
        val processesConsolidated = mutableListOf<Process>()
        val jInfoProcesses = jInfoData.process(jInfoResult)
        val jStatProcesses = jStatData.process(jStatResult)

        jStatProcesses.forEach { (pid, jStat) ->
            jInfoProcesses[pid]?.let { jInfo ->
                processesConsolidated.add(Process.from(pid, jStat, jInfo, typeProcess))
            }
        }
        return processesConsolidated
    }
}
