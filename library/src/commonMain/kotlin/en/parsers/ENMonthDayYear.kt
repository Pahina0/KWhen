package en.parsers

import DateTime
import TagTime
import common.ParserByWord
import en.ENConfig
import en.months
import en.ordinal
import util.between31
import util.copy
import util.matchAny

/**
 * 05/08/24 -> may 8th, 2024
 * june 12th, 2058
 * 06/27
 * 6/28
 * june 11
 * */
class ENMonthDayYear(override val config: ENConfig) : ParserByWord(config) {
    // @formatter:off
    override val matchPattern: Regex
        get() = (
                    "(" +
                            "${months.keys.matchAny()}" +
                            "|$between31" +
                    ")" +
                    "(?:\\s+|-|/)" +
                    "(" +
                        "$between31(?:th|st|rd|nd)?" + // day
                        "|${ordinal.keys.matchAny()}" +
                    ")" +
                    "(?:(?:,|,?\\s+|-|of|/)" +
                        "(\\d{2,4})" + // year
                    ")?"
                ).toRegex()
    // @formatter:on

    override fun onMatch(match: MatchResult): DateTime {
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


                    tagsTimeStart = date.tagsTimeStart + TagTime.YEAR

                )
            }
        }

        val month = match.groupValues[1].lowercase()
        val day = match.groupValues[2].lowercase()
        date = date.run {
            copy(
                startTime = startTime.copy(
                    monthNumber = months[month] ?: (month.toInt()),
                    dayOfMonth = ordinal[day] ?: day.replace("th|st|rd".toRegex(), "").toInt(),

                    ),
                tagsTimeStart = tagsTimeStart + TagTime.DAY + TagTime.MONTH
            )
        }

        return date
    }
}