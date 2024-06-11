package en.parsers

import DateTime
import TimeUnit
import common.parsers.ParserByWord
import configs.ENConfig
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import util.copy

internal class ENBasicDate(override val config: ENConfig) : ParserByWord(config) {
    override val matchPattern: Regex
        get() = "(today|tmrw|tmr|tmw|yesterday|now|rn|right now)".toRegex()

    override fun onMatch(match: MatchResult): DateTime {
        var date = DateTime()

        when (match.groupValues.first().lowercase()) {
            "today" -> date = date.copy(tagsTimeStart = setOf(TimeUnit.DAY))

            "tmrw", "tmr", "tmw" -> date = date.run {
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