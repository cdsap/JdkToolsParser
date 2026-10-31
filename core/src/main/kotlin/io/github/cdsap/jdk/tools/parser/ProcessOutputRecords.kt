package io.github.cdsap.jdk.tools.parser

private val parserWhitespace = "\\s+".toRegex()

private fun String.normalizedParserLines(): List<String> {
    val lines = split("\n")
    val linesWithoutTrailingBlanks = lines.dropLastWhile { it.isBlank() }
    return linesWithoutTrailingBlanks.map { it.trim().replace(parserWhitespace, " ") }
}

internal fun String.fixedWidthRecords(width: Int): List<List<String>> {
    val lines = normalizedParserLines()
    return lines.chunked(width).take(lines.size / width)
}
