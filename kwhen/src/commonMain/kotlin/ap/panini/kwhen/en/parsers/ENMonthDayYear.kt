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
 * En month day year finds the month, day, year time pattern
 * 05/08/24 -> may 8th, 2024
 * june 12th, 2058
 * 06/27
 * 6/28
 * june 11
 * april
 *
 * @property config
 * @constructor Create empty En month day year
 */
internal class ENMonthDayYear(override val config: ENConfig) : ParserByWord(config) {
    // @formatter:off
    override val matchPattern: Regex
        get() = (
                    "(" +
                            "${months.keys.matchAny()}" +
                            "|$between31" +
                    ")" +
                    "(?:" +
                        "(?:\\s+|-|/)" +
                        "(" +
                            "$between31(?:th|st|rd|nd)?" + // day
                            "|${ordinal.keys.matchAny()}" +
                        ")" +
                    ")?" +
                    "(?:(?:,|,?\\s+|-|of|/)" +
                        "(\\d{2,4})" + // year
                    ")?"
                ).toRegex()
    // @formatter:on

    override fun onMatch(match: MatchResult): DateTime? {
        var date = DateTime()

        // checking year
        val year = match.groupValues[3].lowercase()
        if (year.isNotBlank()) {
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

        val month = match.groupValues[1].lowercase()
        val day = match.groupValues[2].lowercase()

        if (month.toIntOrNull() == null && day == "") {
            date = date.run {
                copy(
                    startTime = startTime.copy(
                        monthNumber = months[month]!!,
                    ),
                    tagsTimeStart = tagsTimeStart + TimeUnit.MONTH
                )
            }
        } else if (day != ""){
            date = date.run {
                copy(
                    startTime = startTime.copy(
                        monthNumber = months[month] ?: (month.toInt()),
                        dayOfMonth = ordinal[day] ?: day.replace("th|st|rd|nd".toRegex(), "").toInt(),

                        ),
                    tagsTimeStart = tagsTimeStart + TimeUnit.DAY + TimeUnit.MONTH
                )
            }
        } else {
            return null
        }

        return date
    }
}