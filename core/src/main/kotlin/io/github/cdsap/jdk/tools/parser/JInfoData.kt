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
        val flags = record.getOrNull(0)?.split("\\s+".toRegex()).orEmpty()
        val heapSize = flags.firstOrNull { it.contains("-XX:MaxHeapSize=") }
            ?.substringAfter("-XX:MaxHeapSize=")
            ?.toDoubleOrNull()
            ?: return null
        val pid = record.getOrNull(1)
            ?.split("\\s+".toRegex())
            ?.firstOrNull()
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        return pid to ProcessJInfo(heapSize, getCollector(flags))
    }

    private fun getCollector(flags: List<String>): String =
        collectorFlags.firstOrNull { (flag, _) -> flags.contains(flag) }?.second.orEmpty()

    private companion object {
        val collectorFlags = listOf(
            "-XX:+UseZGC" to "-XX:+UseZGC",
            "-XX:+UseSerialGC" to "-XX:+UseSerialGC",
            "-XX:+UseShenandoahGC" to "-XX:+UseShenandoahGC",
            "-XX:+UseG1GC" to "-XX:+UseG1GC",
            "-XX:+UseParallelGC" to "-XX:+UseParallelGC",
        )
    }
}
