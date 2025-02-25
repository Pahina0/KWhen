package ap.panini.kwhen.en.parsers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.TimeUnit
import ap.panini.kwhen.common.parsers.ParserByWord
import ap.panini.kwhen.configs.Config
import ap.panini.kwhen.en.weekdays
import ap.panini.kwhen.util.matchAny

/**
 * En day of week finds a day of week such as monday, tues, wens
 *
 * @property config
 * @constructor Create empty En day of week
 */
internal class ENDayOfWeek(override val config: Config) : ParserByWord(config) {
    override val matchPattern: Regex
        get() = weekdays.keys.matchAny()

    override fun onMatch(match: MatchResult): DateTime = config.getDateTime(
        tagsDayOfWeek = setOf(weekdays[match.value]!!),
        tagsTimeStart = setOf(TimeUnit.WEEK)
    )

}