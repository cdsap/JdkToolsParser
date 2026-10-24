package io.github.cdsap.jdk.tools.parser

private val parserWhitespace = "\\s+".toRegex()

private fun String.normalizedParserLines(): List<String> =
    split("\n")
        .dropLastWhile { it.isBlank() }
        .map { it.trim().replace(parserWhitespace, " ") }

internal fun String.fixedWidthRecords(width: Int): List<List<String>> {
    val lines = normalizedParserLines()
    return lines.chunked(width).take(lines.size / width)
}
