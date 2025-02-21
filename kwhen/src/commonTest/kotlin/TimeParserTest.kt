import ap.panini.kwhen.DateTime
import ap.panini.kwhen.Parsed
import ap.panini.kwhen.configs.ENConfig


internal class TimeParserTest(private val config: ENConfig = ENConfig()) {
    fun parse(input: String): List<DateTime> {
        return config.instance().parse(input).filter { it.generalTimeTag == null && it.generalNumber == null }
    }

    fun parseAndMerge(input: String): List<DateTime> {
        return config.instance().let { it.merge(input, it.parse(input)) }.flatten().sortedByDescending { it.points }
    }

    fun parseMergeProcess(input: String): List<Parsed> {
        return config.instance().let { it.finalize(it.merge(input, it.parse(input))) }
    }
}
