package io.github.cdsap.jdk.tools.parser

import io.github.cdsap.jdk.tools.parser.model.Process
import io.github.cdsap.jdk.tools.parser.model.ProcessJInfo
import io.github.cdsap.jdk.tools.parser.model.ProcessJstat
import io.github.cdsap.jdk.tools.parser.model.TypeProcess

/**
 * Compatibility entry point for callers that used the original process mapper.
 */
class ProcessMapper {
    private val processAssembler = ProcessAssembler()

    fun from(
        pid: String,
        jstat: ProcessJstat,
        jinfo: ProcessJInfo,
        typeProcess: TypeProcess
    ): Process = processAssembler.assemble(pid, jstat, jinfo, typeProcess)
}
