package ap.panini.kwhen.en.parsers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.TimeUnit
import ap.panini.kwhen.common.parsers.ParserByWord
import ap.panini.kwhen.configs.ENConfig
import ap.panini.kwhen.util.copy

/**
 * En basic time finds words that usually indicate a time of day
 *
 * @property config
 * @constructor Create empty En basic time
 */
internal class ENBasicTime(override val config: ENConfig) : ParserByWord(config) {
    override val matchPattern: Regex
        get() = "(evening|morning|afternoon|night|midnight|midday|noon|tonight)".toRegex()

    override fun onMatch(match: MatchResult): DateTime {
        var date = DateTime()

        when (match.groupValues.first().lowercase()) {
            "evening" -> date = date.run {
                copy(
                    startTime = startTime.copy(hour = config.evening),
                    tagsTimeStart = tagsTimeStart + TimeUnit.HOUR
                )
            }


            "morning" -> date = date.run {
                copy(
                    startTime = startTime.copy(hour = config.morning),
                    tagsTimeStart = tagsTimeStart + TimeUnit.HOUR
                )
            }

            "afternoon" -> date = date.run {
                copy(
                    startTime = startTime.copy(hour = config.afternoon),
                    tagsTimeStart = tagsTimeStart + TimeUnit.HOUR
                )
            }

            "night" -> date = date.run {
                copy(
                    startTime = startTime.copy(hour = config.night),
                    tagsTimeStart = tagsTimeStart + TimeUnit.HOUR
                )
            }

            "tonight" -> date = date.run {
                copy(
                    startTime = startTime.copy(hour = config.night),
                    tagsTimeStart = tagsTimeStart + TimeUnit.HOUR + TimeUnit.DAY
                )
            }

            "midnight" -> date = date.run {
                copy(
                    startTime = startTime.copy(hour = 24),
                    tagsTimeStart = tagsTimeStart + TimeUnit.HOUR
                )
            }


            "midday", "noon" -> date = date.run {
                copy(
                    startTime = startTime.copy(hour = 12),
                    tagsTimeStart = tagsTimeStart + TimeUnit.HOUR
                )
            }


        }

        return date
    }

}