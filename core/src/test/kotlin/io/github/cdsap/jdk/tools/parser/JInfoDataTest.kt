package io.github.cdsap.jdk.tools.parser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class JInfoDataTest {

    @Test
    fun testIncorrectOutputGeneratesEmptyMap() {
        val jInfoData = JInfoData()
        val result = jInfoData.process("cxxxxxd")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testEmptyOutputGeneratesEmptyMap() {
        val jInfoData = JInfoData()
        val result = jInfoData.process("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testResultIsNotIncludingMaxHeapSize() {
        val jInfoData = JInfoData()
        val result = jInfoData.process("XX:CICompilerCount=4 -XX:CompressedClassSpaceSize=260046848")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testResultContainingMultipleProcessesIsParsed() {
        val jInfoData = JInfoData()
        val output = """
            XX:CICompilerCount=4 -XX:CompressedClassSpaceSize=260046848 -XX:MaxHeapSize=2 -XX:ClassSpaceSize=26
            12345
            XX:CICompilerCount=4 -XX:CompressedClassSpaceSize=260046848" -XX:MaxHeapSize=1 -XX:ClassSpaceSize=26
            54321
            """.trimIndent()
        val result = jInfoData.process(output)
        assertTrue(result.isNotEmpty())
        assertTrue(result.containsKey("12345") && result.containsKey("54321"))
        assertTrue(result["12345"]?.max == 2.0)
        assertTrue(result["54321"]?.max == 1.0)
    }

    @Test
    fun testResultContainingMultipleProcessesWithEndingEmptyLineIsParsed() {
        val jInfoData = JInfoData()
        val output = """
            XX:CICompilerCount=4 -XX:CompressedClassSpaceSize=260046848 -XX:MaxHeapSize=2 -XX:ClassSpaceSize=26
            12345
            XX:CICompilerCount=4 -XX:CompressedClassSpaceSize=260046848" -XX:MaxHeapSize=1 -XX:ClassSpaceSize=26
            54321

            """.trimIndent()
        val result = jInfoData.process(output)
        assertTrue(result.isNotEmpty())
        assertTrue(result.containsKey("12345") && result.containsKey("54321"))
        assertTrue(result["12345"]?.max == 2.0)
        assertTrue(result["54321"]?.max == 1.0)
    }

    @Test
    fun testResultContainingMultipleTrailingEmptyLinesIsParsed() {
        val jInfoData = JInfoData()
        val output = """
            XX:CICompilerCount=4 -XX:CompressedClassSpaceSize=260046848 -XX:MaxHeapSize=2 -XX:ClassSpaceSize=26
            12345
        """.trimIndent() + "\n\n\n"

        val result = jInfoData.process(output)

        assertEquals(1, result.size)
        assertEquals(2.0, result["12345"]?.max)
    }


    @Test
    fun testResultContainingMultipleProcessesIsParsedButOneIsIncorrect() {
        val jInfoData = JInfoData()
        val output = """
            XX:CICompilerCount=4 -XX:CompressedClassSpaceSize=260046848 -XX:MaxHeapSize=2 -XX:ClassSpaceSize=26
            12345
            XX:CICompilerCount=4 -XX:CompressedClassSpaceSize=260046848 -XX:ClassSpaceSize=26
            54321
            """.trimIndent()
        val result = jInfoData.process(output)
        assertTrue(result.size == 1)
        assertTrue(result.containsKey("12345"))
        assertTrue(result["12345"]?.max == 2.0)
    }

    @Test
    fun testMalformedRecordIsSkippedWhileValidRecordIsParsed() {
        val jInfoData = JInfoData()
        val output = """
            -XX:MaxHeapSize=not-a-number
            12345
            -XX:MaxHeapSize=2
            54321
            """.trimIndent()

        val result = jInfoData.process(output)

        assertEquals(setOf("54321"), result.keys)
        assertEquals(2.0, result["54321"]?.max)
    }

    @Test
    fun testGcTypeIsRetrieved() {
        val jInfoData = JInfoData()
        val output = """
            -XX:CICompilerCount=4 -XX:InitialHeapSize=1073741824 -XX:MaxHeapSize=17179869184 -XX:MinHeapDeltaBytes=2097152 -XX:MinHeapSize=8388608 -XX:NonNMethodCodeHeapSize=5839564 -XX:NonProfiledCodeHeapSize=122909338 -XX:ProfiledCodeHeapSize=122909338 -XX:ReservedCodeCacheSize=251658240 -XX:+SegmentedCodeCache -XX:SoftMaxHeapSize=17179869184 -XX:+UseCompressedClassPointers -XX:-UseCompressedOops -XX:-UseNUMA -XX:-UseNUMAInterleaving -XX:+UseZGC
            12345
            """.trimIndent()
        val result = jInfoData.process(output)
        assertTrue(result.size == 1)
        assertTrue(result.containsKey("12345"))
        assertTrue(result["12345"]?.gcType == "-XX:+UseZGC")
    }

    @Test
    fun testSupportedGcTypesAreRetrieved() {
        val jInfoData = JInfoData()
        val collectorFlags = listOf(
            "-XX:+UseZGC",
            "-XX:+UseSerialGC",
            "-XX:+UseShenandoahGC",
            "-XX:+UseG1GC",
            "-XX:+UseParallelGC",
        )

        collectorFlags.forEach { collectorFlag ->
            val output = "$collectorFlag -XX:MaxHeapSize=1\n12345"

            assertEquals(collectorFlag, jInfoData.process(output)["12345"]?.gcType)
        }
    }

    @Test
    fun testGcTypePreservesCollectorPriority() {
        val jInfoData = JInfoData()
        val output = """
            -XX:+UseParallelGC -XX:+UseG1GC -XX:+UseShenandoahGC -XX:+UseSerialGC -XX:+UseZGC -XX:MaxHeapSize=1
            12345
            """.trimIndent()

        assertEquals("-XX:+UseZGC", jInfoData.process(output)["12345"]?.gcType)
    }

    @Test
    fun testNotSupportedGcTypeIsEmpty() {
        val jInfoData = JInfoData()
        val output = """
            -XX:CICompilerCount=4 -XX:InitialHeapSize=1073741824 -XX:MaxHeapSize=17179869184 -XX:MinHeapDeltaBytes=2097152 -XX:MinHeapSize=8388608 -XX:NonNMethodCodeHeapSize=5839564 -XX:NonProfiledCodeHeapSize=122909338 -XX:ProfiledCodeHeapSize=122909338 -XX:ReservedCodeCacheSize=251658240 -XX:+SegmentedCodeCache -XX:SoftMaxHeapSize=17179869184 -XX:+UseCompressedClassPointers -XX:-UseCompressedOops -XX:-UseNUMA -XX:-UseNUMAInterleaving -XX:+CCGC
            12345
            """.trimIndent()
        val result = jInfoData.process(output)
        assertTrue(result.size == 1)
        assertTrue(result.containsKey("12345"))
        assertTrue(result["12345"]?.gcType == "")
    }
}
