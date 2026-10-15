package io.github.cdsap.jdk.tools.parser

import io.github.cdsap.jdk.tools.parser.model.Process
import io.github.cdsap.jdk.tools.parser.model.ProcessJInfo
import io.github.cdsap.jdk.tools.parser.model.ProcessJstat
import io.github.cdsap.jdk.tools.parser.model.TypeProcess

class ConsolidateProcesses(
    private val jInfoData: ProcessParser<ProcessJInfo> = JInfoData(),
    private val jStatData: ProcessParser<ProcessJstat> = JStatData()
) {
    private val processMapper = ProcessMapper()

    fun consolidate(jStatResult: String, jInfoResult: String, typeProcess: TypeProcess): List<Process> {
        return consolidateParsed(
            jStatProcesses = jStatData.process(jStatResult),
            jInfoProcesses = jInfoData.process(jInfoResult),
            typeProcess = typeProcess
        )
    }

    internal fun consolidateParsed(
        jStatProcesses: Map<String, ProcessJstat>,
        jInfoProcesses: Map<String, ProcessJInfo>,
        typeProcess: TypeProcess
    ): List<Process> {
        return jStatProcesses.mapNotNull { (pid, jStat) ->
            jInfoProcesses[pid]?.let { jInfo ->
                processMapper.from(pid, jStat, jInfo, typeProcess)
            }
        }
    }
}
