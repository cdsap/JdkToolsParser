package io.github.cdsap.jdk.tools.parser

import io.github.cdsap.jdk.tools.parser.model.ProcessJstat

open class JStatData : JStatParser, Parser<ProcessJstat> {

    open override fun process(result: String): Map<String, ProcessJstat> {
        val processes = mutableMapOf<String, ProcessJstat>()
        val input = ParserInput(result)
        for (record in parseJstatRecords(input)) {
            parseJstatRow(record, input)?.let { row ->
                processes[row.pid] = row.toProcessJstat()
            }
        }
        return processes
    }

    // More than one Kotlin compiler may exist
    // the format out the output is 3 lines per process:
    // Header: Timestamp    S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT    CGC    CGCT     GCT
    // Values:     1117.8  0.0   30720.0  0.0   30720.0 1135616.0 755712.0  865280.0   546816.0  195184.0 189433.3 22208.0 20357.8     22    0.682   0      0.000  12      0.070    0.752
    // PID 28743
    private fun parseJstatRecords(input: ParserInput): List<JstatRecord> {
        val records = mutableListOf<JstatRecord>()
        for (record in input.fixedWidthRecords(3)) {
            records.add(JstatRecord(header = record[0], values = record[1], pid = record[2]))
        }
        return records
    }

    private fun parseJstatRow(record: JstatRecord, input: ParserInput): JStatRow? {
        val rawHeaders = input.tokens(record.header)
        val rawValues = input.tokens(record.values)
        if (rawHeaders.size != rawValues.size) {
            return null
        }

        val pid = input.tokens(record.pid).firstOrNull()?.takeIf { it.isNotEmpty() } ?: return null
        return JStatRow(pid = pid, values = rawHeaders.zip(rawValues).toMap())
    }

    private data class JstatRecord(
        val header: String,
        val values: String,
        val pid: String,
    )
}

internal data class JStatRow(
    val pid: String,
    val values: Map<String, String>,
) {
    fun toProcessJstat(): ProcessJstat = JStatMetrics().map(values)
}
