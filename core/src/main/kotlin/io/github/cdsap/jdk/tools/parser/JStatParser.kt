package io.github.cdsap.jdk.tools.parser

import io.github.cdsap.jdk.tools.parser.model.ProcessJstat

interface JStatParser {
    fun process(result: String): Map<String, ProcessJstat>
}
