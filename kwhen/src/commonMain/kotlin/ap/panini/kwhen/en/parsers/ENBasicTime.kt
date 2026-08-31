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
        get() = "(evening|eve|morning|morn|afternoon|aft|arvo|night|midnight|midnite|midday|noon|tonight|tonite|2nite|2night|tn)".toRegex()

    override fun onMatch(match: MatchResult): DateTime {
        var date = config.getDateTime()

        when (match.groupValues.first().lowercase()) {
            "evening", "eve" -> date = date.run {
                copy(
                    startTime = startTime.copy(hour = config.evening),
                    tagsTimeStart = tagsTimeStart + TimeUnit.HOUR
                )
            }


            "morning", "morn" -> date = date.run {
                copy(
                    startTime = startTime.copy(hour = config.morning),
                    tagsTimeStart = tagsTimeStart + TimeUnit.HOUR
                )
            }

            "afternoon", "aft", "arvo" -> date = date.run {
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

            "tonight", "tonite", "2nite", "2night", "tn" -> date = date.run {
                copy(
                    startTime = startTime.copy(hour = config.night),
                    tagsTimeStart = tagsTimeStart + TimeUnit.HOUR + TimeUnit.DAY
                )
            }

            "midnight", "midnite" -> date = date.run {
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