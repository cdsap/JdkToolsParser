package io.github.cdsap.jdk.tools.parser

import io.github.cdsap.jdk.tools.parser.model.ProcessJInfo


open class JInfoData : JInfoParser, ProcessParser<ProcessJInfo> {

    open override fun process(result: String): Map<String, ProcessJInfo> {
        val processes = mutableMapOf<String, ProcessJInfo>()
        val input = ParserInput(result)
        for (record in input.fixedWidthRecords(2)) {
            parseJInfoRecord(record, input)?.let { (pid, processJInfo) ->
                processes[pid] = processJInfo
            }
        }
        return processes
    }

    private fun parseJInfoRecord(record: List<String>, input: ParserInput): Pair<String, ProcessJInfo>? {
        val flags = record.getOrNull(0)?.let(input::tokens).orEmpty()
        val heapSize = flags.firstOrNull { it.contains("-XX:MaxHeapSize=") }
            ?.substringAfter("-XX:MaxHeapSize=")
            ?.toDoubleOrNull()
            ?: return null
        val pid = record.getOrNull(1)
            ?.let(input::tokens)
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
