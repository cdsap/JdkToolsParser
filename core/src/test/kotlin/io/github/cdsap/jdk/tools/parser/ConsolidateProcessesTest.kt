package io.github.cdsap.jdk.tools.parser

import io.github.cdsap.jdk.tools.parser.model.ProcessJInfo
import io.github.cdsap.jdk.tools.parser.model.ProcessJstat
import io.github.cdsap.jdk.tools.parser.model.TypeProcess
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class ConsolidateProcessesTest {
    @Test
    fun testConsolidate() {
        val jStatResult = """
            Timestamp    S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT    CGC    CGCT     GCT
                1117.8  0.0   30720.0  0.0   30720.0 1135616.0 755712.0  865280.0   546816.0  195184.0 189433.3 22208.0 20357.8     22    0.682   0      0.000  12      0.070    0.752
            28743
        """.trimIndent()

        val jInfoResult = """
            -XX:+UseParallelGC -XX:MaxHeapSize=536870912
            28743
            """.trimIndent()

        val consolidateProcesses = ConsolidateProcesses()

        val consolidatedList = consolidateProcesses.consolidate(jStatResult, jInfoResult, TypeProcess.Gradle)

        assertEquals(1, consolidatedList.size)

        val consolidatedProcess = consolidatedList[0]
        assertEquals("28743", consolidatedProcess.pid)
        assertEquals(0.5, consolidatedProcess.max, 0.01)
        assertEquals(1.27, consolidatedProcess.usage, 0.01)
        assertEquals(1.94, consolidatedProcess.capacity, 0.01)
        assertEquals(0.01, consolidatedProcess.gcTime, 0.01)
        assertEquals(18.63, consolidatedProcess.uptime, 0.01)
        assertEquals("-XX:+UseParallelGC", consolidatedProcess.typeGc)
        assertEquals(TypeProcess.Gradle, consolidatedProcess.typeProcess)
    }

    @Test
    fun testConsolidateThreeJstatsAndTwoJinfoReturnsOnlyTwoProcesses() {
        val jStatResult = """
            Timestamp    S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT    CGC    CGCT     GCT
                1117.8  0.0   30720.0  0.0   30720.0 1135616.0 755712.0  865280.0   546816.0  195184.0 189433.3 22208.0 20357.8     22    0.682   0      0.000  12      0.070    0.752
            28743
            Timestamp    S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT    CGC    CGCT     GCT
                1117.8  0.0   30720.0  0.0   30720.0 1135616.0 755712.0  865280.0   546816.0  195184.0 189433.3 22208.0 20357.8     22    0.682   0      0.000  12      0.070    0.752
            11111
            Timestamp    S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT    CGC    CGCT     GCT
                1117.8  0.0   30720.0  0.0   30720.0 1135616.0 755712.0  865280.0   546816.0  195184.0 189433.3 22208.0 20357.8     22    0.682   0      0.000  12      0.070    0.752
            32222
        """.trimIndent()

        val jInfoResult = """
            -XX:+UseParallelGC -XX:MaxHeapSize=536870912
            28743
            -XX:+UseParallelGC -XX:MaxHeapSize=536870912
            11111
            """.trimIndent()

        val consolidateProcesses = ConsolidateProcesses()

        val consolidatedList = consolidateProcesses.consolidate(jStatResult, jInfoResult, TypeProcess.Gradle)

        assertEquals(2, consolidatedList.size)

        assertTrue(consolidatedList.any { it.pid == "28743" })
        assertTrue(consolidatedList.any { it.pid == "11111" })

    }

    @Test
    fun testConsolidateParsedJoinsMatchingProcessesWithoutRawText() {
        val jStatProcesses = mapOf(
            "123" to ProcessJstat(
                usage = 1048576.0,
                capacity = 2097152.0,
                gcTime = 120.0,
                uptime = 600.0
            ),
            "456" to ProcessJstat(
                usage = 1.0,
                capacity = 1.0,
                gcTime = 1.0,
                uptime = 1.0
            )
        )
        val jInfoProcesses = mapOf(
            "123" to ProcessJInfo(max = 1073741824.0, gcType = "-XX:+UseG1GC")
        )

        val consolidatedList = ConsolidateProcesses().consolidateParsed(
            jStatProcesses = jStatProcesses,
            jInfoProcesses = jInfoProcesses,
            typeProcess = TypeProcess.Kotlin
        )

        assertEquals(1, consolidatedList.size)
        val consolidatedProcess = consolidatedList[0]
        assertEquals("123", consolidatedProcess.pid)
        assertEquals(1.0, consolidatedProcess.max, 0.01)
        assertEquals(1.0, consolidatedProcess.usage, 0.01)
        assertEquals(2.0, consolidatedProcess.capacity, 0.01)
        assertEquals(2.0, consolidatedProcess.gcTime, 0.01)
        assertEquals(10.0, consolidatedProcess.uptime, 0.01)
        assertEquals("-XX:+UseG1GC", consolidatedProcess.typeGc)
        assertEquals(TypeProcess.Kotlin, consolidatedProcess.typeProcess)
    }

    @Test
    fun testConsolidateWithInjectedParsersJoinsWithoutRawParsing() {
        val jInfoStub = object : JInfoData() {
            override fun process(result: String): Map<String, ProcessJInfo> {
                return mapOf(
                    "123" to ProcessJInfo(max = 1073741824.0, gcType = "-XX:+UseG1GC")
                )
            }
        }
        val jStatStub = object : JStatData() {
            override fun process(result: String): Map<String, ProcessJstat> {
                return mapOf(
                    "123" to ProcessJstat(
                        usage = 1048576.0,
                        capacity = 2097152.0,
                        gcTime = 120.0,
                        uptime = 600.0
                    ),
                    "456" to ProcessJstat(
                        usage = 1.0,
                        capacity = 1.0,
                        gcTime = 1.0,
                        uptime = 1.0
                    )
                )
            }
        }

        val consolidateProcesses = ConsolidateProcesses(jInfoStub, jStatStub)
        val consolidatedList = consolidateProcesses.consolidate(
            jStatResult = "ignored",
            jInfoResult = "ignored",
            typeProcess = TypeProcess.Kotlin
        )

        assertEquals(1, consolidatedList.size)
        val consolidatedProcess = consolidatedList[0]
        assertEquals("123", consolidatedProcess.pid)
        assertEquals(1.0, consolidatedProcess.max, 0.01)
        assertEquals(1.0, consolidatedProcess.usage, 0.01)
        assertEquals(2.0, consolidatedProcess.capacity, 0.01)
        assertEquals(2.0, consolidatedProcess.gcTime, 0.01)
        assertEquals(10.0, consolidatedProcess.uptime, 0.01)
        assertEquals("-XX:+UseG1GC", consolidatedProcess.typeGc)
        assertEquals(TypeProcess.Kotlin, consolidatedProcess.typeProcess)
    }
}
