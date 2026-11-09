package io.github.cdsap.jdk.tools.parser

import io.github.cdsap.jdk.tools.parser.model.Process
import io.github.cdsap.jdk.tools.parser.model.ProcessJInfo
import io.github.cdsap.jdk.tools.parser.model.ProcessJstat
import io.github.cdsap.jdk.tools.parser.model.TypeProcess

class ConsolidateProcesses(
    private val jInfoData: JInfoParser = JInfoData(),
    private val jStatData: JStatParser = JStatData()
) {
    private val processConsolidator = ProcessConsolidator()

    fun consolidate(jStatResult: String, jInfoResult: String, typeProcess: TypeProcess): List<Process> {
        return processConsolidator.consolidate(
            jStatProcesses = jStatData.process(jStatResult),
            jInfoProcesses = jInfoData.process(jInfoResult),
            typeProcess = typeProcess
        )
    }
}
