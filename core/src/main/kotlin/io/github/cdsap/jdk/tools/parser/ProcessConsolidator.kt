package io.github.cdsap.jdk.tools.parser

import io.github.cdsap.jdk.tools.parser.model.Process
import io.github.cdsap.jdk.tools.parser.model.ProcessJInfo
import io.github.cdsap.jdk.tools.parser.model.ProcessJstat
import io.github.cdsap.jdk.tools.parser.model.TypeProcess

internal class ProcessConsolidator(
    private val processMapper: ProcessMapper = ProcessMapper()
) {
    fun consolidate(
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
