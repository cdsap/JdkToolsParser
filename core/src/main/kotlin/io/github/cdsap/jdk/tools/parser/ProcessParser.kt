package io.github.cdsap.jdk.tools.parser

interface ProcessParser<T> {
    fun process(result: String): Map<String, T>
}
