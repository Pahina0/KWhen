package en.parsers

import DateTime
import TagTime
import common.ParserByWord
import en.ENConfig
import util.copy

class ENBasicTime(override val config: ENConfig) : ParserByWord(config) {
    override val matchPattern: Regex
        get() = "(evening|morning|afternoon|night|midnight|midday|noon|tonight)".toRegex()

    override fun onMatch(match: MatchResult): DateTime {
        var date = DateTime()

        when (match.groupValues.first().lowercase()) {
            "evening" -> date = date.run {
                copy(
                    startTime = startTime.copy(hour = config.evening),
                    tagsTime = tagsTime + TagTime.HOUR
                )
            }


            "morning" -> date = date.run {
                copy(
                    startTime = startTime.copy(hour = config.morning),
                    tagsTime = tagsTime + TagTime.HOUR
                )
            }

            "afternoon" -> date = date.run {
                copy(
                    startTime = startTime.copy(hour = config.afternoon),
                    tagsTime = tagsTime + TagTime.HOUR
                )
            }

            "night" -> date = date.run {
                copy(
                    startTime = startTime.copy(hour = config.night),
                    tagsTime = tagsTime + TagTime.HOUR
                )
            }

            "tonight" -> date = date.run {
                copy(
                    startTime = startTime.copy(hour = config.night),
                    tagsTime = tagsTime + TagTime.HOUR + TagTime.DAY
                )
            }

            "midnight" -> date = date.run {
                copy(
                    startTime = startTime.copy(hour = 24),
                    tagsTime = tagsTime + TagTime.HOUR
                )
            }


            "midday", "noon" -> date = date.run {
                copy(
                    startTime = startTime.copy(hour = 12),
                    tagsTime = tagsTime + TagTime.HOUR
                )
            }


        }

        return date
    }

}