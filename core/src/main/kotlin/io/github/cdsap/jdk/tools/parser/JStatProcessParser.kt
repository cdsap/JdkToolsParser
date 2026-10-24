package io.github.cdsap.jdk.tools.parser

import io.github.cdsap.jdk.tools.parser.model.ProcessJstat

internal class JStatProcessParser {

    fun parse(header: String, values: String, pid: String): Pair<String, ProcessJstat>? {
        val rawHeaders = header.split(" ")
        val rawValues = values.split(" ")
        if (rawHeaders.size != rawValues.size) {
            return null
        }

        val parsedValues = rawHeaders.zip(rawValues).toMap()
        val process = pid.split(" ")
        return process.first() to ProcessJstat(
            capacity = getCapacity(parsedValues),
            usage = getUsage(parsedValues),
            gcTime = gcTime(parsedValues),
            uptime = uptime(parsedValues)
        )
    }
}

private fun getCapacity(values: Map<String, String>): Double {
    // ZGC is not Current survivor space
    if (values["S0C"] == "-") {
        val oc = values["OC"]
        val mc = values["MC"]
        if (oc != null && mc != null) {
            val ocNumber = oc.jstatValueToDouble()
            val mcNumber = mc.jstatValueToDouble()
            return mcNumber + ocNumber
        } else {
            return 0.0
        }
    } else {
        val ec = values["EC"]
        val oc = values["OC"]
        val soc = values["S0C"]
        val s1c = values["S1C"]
        if (ec != null && oc != null && soc != null && s1c != null) {
            val ecNumber = ec.jstatValueToDouble()
            val ocNumber = oc.jstatValueToDouble()
            val socNumber = soc.jstatValueToDouble()
            val s1cNumber = s1c.jstatValueToDouble()
            return ecNumber + ocNumber + socNumber + s1cNumber

        } else {
            return 0.0
        }
    }
}

private fun getUsage(values: Map<String, String>): Double {
    // ZGC is not using Eden region
    if (values["S0C"] == "-") {
        val ou = values["OU"]
        val mu = values["MU"]
        if (ou != null && mu != null) {
            val ouNumber = ou.jstatValueToDouble()
            val muNumber = mu.jstatValueToDouble()
            return muNumber + ouNumber
        } else {
            return 0.0
        }
    } else {
        val eu = values["EU"]
        val ou = values["OU"]
        val sou = values["S0U"]
        val s1u = values["S1U"]
        if (eu != null && ou != null && sou != null && s1u != null) {
            val euNumber = eu.jstatValueToDouble()
            val ouNumber = ou.jstatValueToDouble()
            val souNumber = sou.jstatValueToDouble()
            val s1uNumber = s1u.jstatValueToDouble()
            return euNumber + ouNumber + souNumber + s1uNumber

        } else {
            return 0.0
        }

    }
}

private fun gcTime(values: Map<String, String>): Double {
    val gct = values["GCT"]
    return gct?.jstatValueToDouble() ?: 0.0
}

private fun uptime(values: Map<String, String>): Double {
    val timeStamp = values["Timestamp"]
    return timeStamp?.jstatValueToDouble() ?: 0.0
}

fun String.jstatValueToDouble(): Double {
    return try {
        this.toDouble()
    } catch (e: java.lang.NumberFormatException) {
        0.0
    }
}
