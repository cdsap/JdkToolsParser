package io.github.cdsap.jdk.tools.parser

private val parserWhitespace = "\\s+".toRegex()

internal class ParserInput(result: String) {
    private val lines = result
        .split("\n")
        .dropLastWhile { it.isBlank() }
        .map { it.trim().replace(parserWhitespace, " ") }

    fun fixedWidthRecords(width: Int): List<List<String>> =
        lines.chunked(width).take(lines.size / width)

    fun tokens(line: String): List<String> = line.split(" ")
}
