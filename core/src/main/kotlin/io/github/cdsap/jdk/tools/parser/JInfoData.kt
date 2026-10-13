package io.github.cdsap.jdk.tools.parser

import io.github.cdsap.jdk.tools.parser.model.ProcessJInfo


open class JInfoData : JInfoParser, ProcessParser<ProcessJInfo> {

    open override fun process(result: String): Map<String, ProcessJInfo> {
        val processP = mutableMapOf<String, ProcessJInfo>()
        val lines = result.split("\n")
        if (lines.last().trim() == "") {
            lines.dropLast(1)
        }
        val xNumberOfProcess = lines.size / 2
        var auxIndex = 0
        for (i in 0 until xNumberOfProcess) {
            val flags = lines[auxIndex].split("\\s+".toRegex())
            val gcType = getCollector(flags)
            if (!flags.last().contains("-XX:MaxHeapSize=")) {
                flags.dropLast(1)
            }

            val heapSizeFlag = flags.firstOrNull { it.contains("-XX:MaxHeapSize=") }?.replace("-XX:MaxHeapSize=", "")
            val process = lines[++auxIndex].split("\\s+".toRegex())
            if (heapSizeFlag != null) {
                auxIndex++
                processP[process.first()] = ProcessJInfo(heapSizeFlag.toDouble(), gcType)
            }
        }
        return processP
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
