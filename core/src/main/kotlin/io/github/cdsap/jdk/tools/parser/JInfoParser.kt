package io.github.cdsap.jdk.tools.parser

import io.github.cdsap.jdk.tools.parser.model.ProcessJInfo

interface JInfoParser {
    fun process(result: String): Map<String, ProcessJInfo>
}
