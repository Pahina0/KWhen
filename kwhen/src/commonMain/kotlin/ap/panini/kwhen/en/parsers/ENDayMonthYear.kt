package ap.panini.kwhen.en.parsers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.TimeUnit
import ap.panini.kwhen.common.parsers.ParserByWord
import ap.panini.kwhen.configs.ENConfig
import ap.panini.kwhen.en.months
import ap.panini.kwhen.en.ordinal
import ap.panini.kwhen.util.between31
import ap.panini.kwhen.util.copy
import ap.panini.kwhen.util.matchAny

/**
 * En day month year finds a day, month, year sequence
 * for the brits
 * 4th of july, 95
 * 6 May 2019
 *
 * @property config
 * @constructor Create empty En day month year
 */
internal class ENDayMonthYear(override val config: ENConfig) : ParserByWord(config) {
    // @formatter:off
    override val matchPattern: Regex
        get() = (
                    "(" +
                        "$between31(?:th|st|rd)?" + // day
                        "|${ordinal.keys.matchAny()}" +
                    ")" +
                    "(?:\\s+(?:of\\s+)?)" +
                    "(${months.keys.matchAny()})" + // months
                    "(?:(?:,|,?\\s+|of)" +
                        "(\\d{2,4})" + // year
                    ")?"
                ).toRegex()
    // @formatter:on

    override fun onMatch(match: MatchResult): DateTime? {
        var date = DateTime()

        // no year
        val year = match.groupValues[3].lowercase()
        if (year.isNotBlank()) {

            // only 2 numbers such as for '90
            date = date.run {
                copy(
                    startTime = if (year.length == 2) { // 19th of aug, 98 -> 1998
                        if (year.toInt() > 50) {
                            startTime.copy(year = 1900 + year.toInt())
                        } else { // 19th of july, 04 -> 2004
                            startTime.copy(year = 2000 + year.toInt())
                        }
                    } else {
                        startTime.copy(year = year.toInt())
                    },

                    tagsTimeStart = date.tagsTimeStart + TimeUnit.YEAR
                )
            }
        }

        val month = match.groupValues[2].lowercase()
        val day = match.groupValues[1].lowercase()
        date = date.run {
            copy(
                startTime = startTime.copy(
                    monthNumber = months[month] ?: return null,
                    dayOfMonth = ordinal[day] ?: day.replace("th|st|rd".toRegex(), "").toInt()
                ), tagsTimeStart = tagsTimeStart + TimeUnit.DAY + TimeUnit.MONTH
            )
        }



        return date
    }
}