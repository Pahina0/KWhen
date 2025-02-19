package ap.panini.kwhen.en.parsers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.common.parsers.ParserByWord
import ap.panini.kwhen.configs.Config
import ap.panini.kwhen.en.generalTimesRepeatable
import ap.panini.kwhen.util.matchAny

/**
 * En general time finds general time units for repeatable units
 * biweekly
 * hourly
 * monthly
 *
 * @property config
 * @constructor Create empty En general time
 */
internal class ENGeneralTimeRepeatable(override val config: Config) : ParserByWord(config) {
    override val matchPattern: Regex
        get() = "(bi[ |-]?)?(${generalTimesRepeatable.keys.matchAny()})".toRegex()

    override fun onMatch(match: MatchResult): DateTime {
        val bi = match.groupValues[1].isNotBlank()
        return DateTime(
            repeatTag = generalTimesRepeatable[match.groupValues[2]]!!,
            repeatOften = if (bi) 2.0 else 1.0
        )
    }
}
