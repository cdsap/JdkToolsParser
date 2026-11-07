package io.github.cdsap.jdk.tools.parser

fun interface ProcessParser<T> {
    fun process(result: String): Map<String, T>
}
