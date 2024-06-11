import en.ENConfig
import en.ENController


class TimeParser(val config: ENConfig = ENConfig()) {
    fun parse(input: String): List<DateTime> {
        return ENController(config).parse(input).filter { it.generalTimeTag == null && it.generalNumber == null }
    }

    fun parseAndMerge(input: String): List<DateTime> {
        return ENController(config).let { it.merge(input, it.parse(input)) }
    }

    fun parseMergeProcess(input: String): List<Processed> {
        return ENController(config).let { it.finalize(it.merge(input, it.parse(input))) }
    }
}
