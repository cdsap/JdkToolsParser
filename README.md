# Jinfo Jstat output parser
Utility to parse the outputs of the jdk tools jinfo/jstat.
This library is used in Gradle Plugins like:
* Info Kotlin Process https://github.com/cdsap/InfoKotlinProcess

## Dependency
```
dependencies {
    implementation("io.github.cdsap:jdk-tools-parser:0.1.0")
}
```

## Usage
Once we have the output of the commands jstat/jinfo:
```
val jInfoData = JInfoData().process(jInfoResult)
val jStatData = JStatData().process(jStatResult)
```
Additionally, the library defines a `ConsolidateProcesses` that aggregates the information of processes for each entity(jstat/jinfo).
This class returns a list of `Process`:
```
data class Process(
    val pid: String,
    val max: Double,
    val usage: Double,
    val capacity: Double,
    val gcTime: Double,
    val uptime: Double,
    val typeGc: String,
    val typeProcess: TypeProcess
)
```

