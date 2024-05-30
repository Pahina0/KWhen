package en.parsers

import DateTime
import TagTime
import common.ParserByWord
import en.ENConfig
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import util.copy

class ENBasicDate(override val config: ENConfig) : ParserByWord(config) {
    override val matchPattern: Regex
        get() = "(today|tmrw|tmr|tmw|yesterday|now|rn|right now)".toRegex()

    override fun onMatch(match: MatchResult): DateTime {
        var date = DateTime()

        when (match.groupValues.first().lowercase()) {
            "today" -> date = date.copy(tagsTimeStart = setOf(TagTime.DAY))

            "tmrw", "tmr", "tmw" -> date = date.run {
                copy(
                    startTime = startTime.copy(startTime.date.plus(1, DateTimeUnit.DAY)),
                    tagsTimeStart = setOf(TagTime.DAY)
                )
            }


            "yesterday" -> date = date.run {
                copy(
                    startTime = startTime.copy(startTime.date.minus(1, DateTimeUnit.DAY)),
                    tagsTimeStart = setOf(TagTime.DAY)
                )
            }

            "now", "rn", "right now" -> date = date.copy(tagsTimeStart = setOf(TagTime.DAY, TagTime.MINUTE, TagTime.SECOND))
        }

        return date
    }
}