package io.github.cdsap.jdk.tools.parser

internal fun String.fixedWidthRecords(width: Int): List<List<String>> {
    val lines = split("\n")
    return lines.chunked(width).take(lines.size / width)
}
