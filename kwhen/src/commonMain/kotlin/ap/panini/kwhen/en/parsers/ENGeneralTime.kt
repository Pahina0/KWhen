package ap.panini.kwhen.en.parsers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.common.parsers.ParserByWord
import ap.panini.kwhen.configs.Config
import ap.panini.kwhen.en.generalTimes
import ap.panini.kwhen.util.matchAny


/**
 * En general time finds general time units
 * in 20 `hours`
 * min
 * second
 *
 * @property config
 * @constructor Create empty En general time
 */
internal class ENGeneralTime(override val config: Config) : ParserByWord(config) {
    override val matchPattern: Regex
        get() = "(?:of\\s+)?(?:a|an\\s+)?(${generalTimes.keys.matchAny()})".toRegex()

    override fun onMatch(match: MatchResult): DateTime {
        return DateTime(
            generalTimeTag = generalTimes[match.groupValues[1]]!!
        )
    }
}