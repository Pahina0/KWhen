package ap.panini.kwhen.en.parsers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.TimeUnit
import ap.panini.kwhen.common.parsers.ParserByWord
import ap.panini.kwhen.configs.ENConfig
import ap.panini.kwhen.util.copy
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * En basic date finds basic times such as today and tomorrow
 *
 * @property config
 * @constructor Create empty En basic date
 */
internal class ENBasicDate(override val config: ENConfig) : ParserByWord(config) {
    override val matchPattern: Regex
        get() = "(today|tmrw|tmr|tmw|tomorrow|yesterday|now|rn|right now)".toRegex()

    override fun onMatch(match: MatchResult): DateTime {
        var date = config.getDateTime()

        when (match.groupValues.first().lowercase()) {
            "today" -> date = date.copy(tagsTimeStart = setOf(TimeUnit.DAY))

            "tmrw", "tmr", "tmw", "tomorrow" -> date = date.run {
                copy(
                    startTime = startTime.copy(startTime.date.plus(1, DateTimeUnit.DAY)),
                    tagsTimeStart = setOf(TimeUnit.DAY)
                )
            }


            "yesterday" -> date = date.run {
                copy(
                    startTime = startTime.copy(startTime.date.minus(1, DateTimeUnit.DAY)),
                    tagsTimeStart = setOf(TimeUnit.DAY)
                )
            }

            "now", "rn", "right now" -> date = date.copy(tagsTimeStart = setOf(TimeUnit.DAY, TimeUnit.MINUTE, TimeUnit.SECOND))
        }

        return date
    }
}