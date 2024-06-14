package ap.panini.kwhen.en.parsers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.common.parsers.ParserByWord
import ap.panini.kwhen.configs.Config
import ap.panini.kwhen.en.generalTimes
import ap.panini.kwhen.util.matchAny


/**
 * in 20 `hours`
 * min
 * second
 * */
internal class ENGeneralTime(override val config: Config) : ParserByWord(config) {
    override val matchPattern: Regex
        get() = generalTimes.keys.matchAny()

    override fun onMatch(match: MatchResult): DateTime {
        return DateTime(
            generalTimeTag = generalTimes[match.value]!!
        )
    }
}