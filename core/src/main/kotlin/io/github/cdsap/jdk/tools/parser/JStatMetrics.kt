package io.github.cdsap.jdk.tools.parser

import io.github.cdsap.jdk.tools.parser.model.ProcessJstat

internal class JStatMetrics {

    fun map(values: Map<String, String>): ProcessJstat = ProcessJstat(
        capacity = getCapacity(values),
        usage = getUsage(values),
        gcTime = values["GCT"]?.jstatValueToDouble() ?: 0.0,
        uptime = values["Timestamp"]?.jstatValueToDouble() ?: 0.0,
    )

    private fun getCapacity(values: Map<String, String>): Double {
        // ZGC is not Current survivor space
        if (values["S0C"] == "-") {
            return values.sumOfJstatValues("OC", "MC")
        }
        return values.sumOfJstatValues("EC", "OC", "S0C", "S1C")
    }

    private fun getUsage(values: Map<String, String>): Double {
        // ZGC is not using Eden region
        if (values["S0C"] == "-") {
            return values.sumOfJstatValues("OU", "MU")
        }
        return values.sumOfJstatValues("EU", "OU", "S0U", "S1U")
    }

    private fun Map<String, String>.sumOfJstatValues(vararg names: String): Double {
        if (names.any { this[it] == null }) {
            return 0.0
        }
        return names.sumOf { this[it].orEmpty().jstatValueToDouble() }
    }
}

fun String.jstatValueToDouble(): Double = toDoubleOrNull() ?: 0.0
