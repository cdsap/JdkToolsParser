package io.github.cdsap.jdk.tools.parser

import io.github.cdsap.jdk.tools.parser.model.ProcessJstat

open class JStatData : JStatParser {

    open override fun process(result: String): Map<String, ProcessJstat> {
        val processes = mutableMapOf<String, ProcessJstat>()
        val metrics = JStatMetrics()
        for (record in parseJstatRecords(result)) {
            parseRecord(record)?.let { (pid, measurements) ->
                processes[pid] = metrics.map(measurements)
            }
        }
        return processes
    }

    // More than one Kotlin compiler may exist
    // the format out the output is 3 lines per process:
    // Header: Timestamp    S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT    CGC    CGCT     GCT
    // Values:     1117.8  0.0   30720.0  0.0   30720.0 1135616.0 755712.0  865280.0   546816.0  195184.0 189433.3 22208.0 20357.8     22    0.682   0      0.000  12      0.070    0.752
    // PID 28743
    private fun parseJstatRecords(result: String): List<JstatRecord> {
        val records = mutableListOf<JstatRecord>()
        for (record in result.fixedWidthRecords(3)) {
            records.add(JstatRecord(header = record[0], values = record[1], pid = record[2]))
        }
        return records
    }

    private fun parseRecord(record: JstatRecord): Pair<String, Map<String, String>>? {
        val rawHeaders = record.header.split(" ")
        val rawValues = record.values.split(" ")
        if (rawHeaders.size != rawValues.size) {
            return null
        }

        val pid = record.pid.split(" ").firstOrNull()?.takeIf { it.isNotEmpty() } ?: return null
        return pid to rawHeaders.zip(rawValues).toMap()
    }

    private data class JstatRecord(
        val header: String,
        val values: String,
        val pid: String,
    )
}
