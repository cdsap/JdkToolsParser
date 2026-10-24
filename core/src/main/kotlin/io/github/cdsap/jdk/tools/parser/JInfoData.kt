package io.github.cdsap.jdk.tools.parser

import io.github.cdsap.jdk.tools.parser.model.ProcessJInfo


open class JInfoData : JInfoParser {

    open override fun process(result: String): Map<String, ProcessJInfo> {
        val processP = mutableMapOf<String, ProcessJInfo>()
        for (record in result.fixedWidthRecords(2)) {
            parseRecord(record)?.let { (pid, processJInfo) ->
                processP[pid] = processJInfo
            }
        }
        return processP
    }

    private fun parseRecord(record: List<String>): Pair<String, ProcessJInfo>? {
        val flags = record.getOrNull(0)?.split(" ").orEmpty()
        val heapSize = flags.firstOrNull { it.contains("-XX:MaxHeapSize=") }
            ?.substringAfter("-XX:MaxHeapSize=")
            ?.toDoubleOrNull()
            ?: return null
        val pid = record.getOrNull(1)
            ?.split(" ")
            ?.firstOrNull()
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        return pid to ProcessJInfo(heapSize, getCollector(flags))
    }

    private fun getCollector(flags: List<String>): String =
        supportedCollectorFlags.firstOrNull(flags::contains).orEmpty()

    private companion object {
        val supportedCollectorFlags = listOf(
            "-XX:+UseZGC",
            "-XX:+UseSerialGC",
            "-XX:+UseShenandoahGC",
            "-XX:+UseG1GC",
            "-XX:+UseParallelGC",
        )
    }
}
