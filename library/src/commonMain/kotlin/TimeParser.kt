import en.ENConfig
import en.ENController


class TimeParser(val config: ENConfig = ENConfig()) {
    fun parse(input: String): List<DateTime> {
        return ENController(config).parse(input)
    }

    fun parseAndMerge(input: String): List<DateTime> {
        return ENController(config).let { it.merge(input, it.parse(input)) }
    }
}
