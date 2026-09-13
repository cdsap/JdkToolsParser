package io.github.cdsap.jdk.tools.parser.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProcessTest {
    @Test
    fun fromConvertsUnitsAndMapsFields() {
        val process = Process.from(
            pid = "123",
            jstat = ProcessJstat(
                usage = 1048576.0,
                capacity = 2097152.0,
                gcTime = 120.0,
                uptime = 600.0
            ),
            jinfo = ProcessJInfo(max = 1073741824.0, gcType = "-XX:+UseG1GC"),
            typeProcess = TypeProcess.Kotlin
        )

        assertEquals("123", process.pid)
        assertEquals(1.0, process.max, 0.01)
        assertEquals(1.0, process.usage, 0.01)
        assertEquals(2.0, process.capacity, 0.01)
        assertEquals(2.0, process.gcTime, 0.01)
        assertEquals(10.0, process.uptime, 0.01)
        assertEquals("-XX:+UseG1GC", process.typeGc)
        assertEquals(TypeProcess.Kotlin, process.typeProcess)
    }
}
