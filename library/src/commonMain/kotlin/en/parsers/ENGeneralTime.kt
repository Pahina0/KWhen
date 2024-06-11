package en.parsers

import DateTime
import configs.Config
import common.parsers.ParserByWord
import en.generalTimes
import util.matchAny

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