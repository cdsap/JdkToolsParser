package io.github.cdsap.jdk.tools.parser

import io.github.cdsap.jdk.tools.parser.model.Process
import io.github.cdsap.jdk.tools.parser.model.ProcessJInfo
import io.github.cdsap.jdk.tools.parser.model.ProcessJstat
import io.github.cdsap.jdk.tools.parser.model.TypeProcess
import io.github.cdsap.jdk.tools.parser.model.toGigsFromBytes
import io.github.cdsap.jdk.tools.parser.model.toGigsFromKb
import io.github.cdsap.jdk.tools.parser.model.toMinutes

class ProcessMapper {
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
