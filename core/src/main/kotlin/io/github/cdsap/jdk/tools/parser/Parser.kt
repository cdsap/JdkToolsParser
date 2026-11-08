package io.github.cdsap.jdk.tools.parser

fun interface Parser<T> {
    fun process(result: String): Map<String, T>
}
