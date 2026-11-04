package io.github.cdsap.jdk.tools.parser

private val parserWhitespace = "\\s+".toRegex()

private fun List<String>.withoutTrailingBlankLines(): List<String> =
    dropLastWhile { it.isBlank() }

private fun String.normalizedParserLines(): List<String> {
    val linesWithoutTrailingBlanks = split("\n").withoutTrailingBlankLines()
    return linesWithoutTrailingBlanks.map { it.trim().replace(parserWhitespace, " ") }
}

internal fun String.fixedWidthRecords(width: Int): List<List<String>> {
    val lines = normalizedParserLines()
    return lines.chunked(width).take(lines.size / width)
}
