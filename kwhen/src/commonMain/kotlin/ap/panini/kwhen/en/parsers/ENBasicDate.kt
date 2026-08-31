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
        get() = "(today|tdy|td|2day|tmrw|tmr|tmw|tom|tomorrow|2morrow|2moro|yesterday|yest|yst|yd|now|rn|right now)".toRegex()

    override fun onMatch(match: MatchResult): DateTime {
        var date = config.getDateTime()

        when (match.groupValues.first().lowercase()) {
            "today", "tdy", "td", "2day" -> date = date.copy(tagsTimeStart = setOf(TimeUnit.DAY))

            "tmrw", "tmr", "tmw", "tom", "tomorrow", "2morrow", "2moro" -> date = date.run {
                copy(
                    startTime = startTime.copy(startTime.date.plus(1, DateTimeUnit.DAY)),
                    tagsTimeStart = setOf(TimeUnit.DAY)
                )
            }


            "yesterday", "yest", "yst", "yd" -> date = date.run {
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