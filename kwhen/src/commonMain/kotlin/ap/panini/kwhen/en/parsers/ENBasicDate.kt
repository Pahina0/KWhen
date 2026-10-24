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
                val newDate = startTime.date.plus(1, DateTimeUnit.DAY)
                val newTags = tagsTimeStart.toMutableSet()
                newTags += TimeUnit.DAY
                if (newDate.month != startTime.month) newTags += TimeUnit.MONTH
                if (newDate.year != startTime.year) newTags += TimeUnit.YEAR
                copy(
                    startTime = startTime.copy(newDate),
                    tagsTimeStart = newTags
                )
            }


            "yesterday", "yest", "yst", "yd" -> date = date.run {
                val newDate = startTime.date.minus(1, DateTimeUnit.DAY)
                val newTags = tagsTimeStart.toMutableSet()
                newTags += TimeUnit.DAY
                if (newDate.month != startTime.month) newTags += TimeUnit.MONTH
                if (newDate.year != startTime.year) newTags += TimeUnit.YEAR
                copy(
                    startTime = startTime.copy(newDate),
                    tagsTimeStart = newTags
                )
            }

            "now", "rn", "right now" -> date = date.copy(
                startTime = config.now(),
                tagsTimeStart = setOf(TimeUnit.DAY, TimeUnit.HOUR, TimeUnit.MINUTE, TimeUnit.SECOND)
            )
        }

        return date
    }
}