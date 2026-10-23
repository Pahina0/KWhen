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
                    "(?:" +
                        "(" +
                            "${months.keys.matchAny()}" +
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
                        ")?" +
                    ")|(?:" +
                        "($between31)" +
                        "(?:" +
                            "/($between31)(?:/(\\d{2,4}))?" +
                            "|-($between31)-(\\d{2,4})" +
                        ")" +
                    ")"
                ).toRegex()
    // @formatter:on

    override fun onMatch(match: MatchResult): DateTime? {
        var date = config.getDateTime()

        val namedMonth = match.groupValues[1].lowercase()
        val year = if (namedMonth.isNotBlank()) {
            match.groupValues[3].lowercase()
        } else {
            match.groupValues[6].ifBlank { match.groupValues[8] }.lowercase()
        }

        // checking year
        if (year.isNotBlank()) {
            val yearInt = year.toIntOrNull() ?: return null
            date = date.run {
                copy(
                    startTime = if (year.length == 2) { // 19th of aug, 98 -> 1998
                        if (yearInt > 50) {
                            startTime.copy(year = 1900 + yearInt)
                        } else { // 19th of july, 04 -> 2004
                            startTime.copy(year = 2000 + yearInt)
                        }
                    } else {
                        startTime.copy(year = yearInt)
                    },
                    tagsTimeStart = date.tagsTimeStart + TimeUnit.YEAR
                )
            }
        }

        if (namedMonth.isNotBlank()) {
            val monthNum = months[namedMonth] ?: return null
            val day = match.groupValues[2].lowercase()

            date = if (day.isBlank()) {
                date.run {
                    copy(
                        startTime = startTime.copy(monthNumber = monthNum),
                        tagsTimeStart = tagsTimeStart + TimeUnit.MONTH
                    )
                }
            } else {
                val dayNum = ordinal[day]
                    ?: day.replace("th|st|rd|nd".toRegex(), "").toIntOrNull()
                    ?: return null
                date.run {
                    copy(
                        startTime = startTime.copy(
                            monthNumber = monthNum,
                            dayOfMonth = dayNum
                        ),
                        tagsTimeStart = tagsTimeStart + TimeUnit.DAY + TimeUnit.MONTH
                    )
                }
            }
        } else {
            val firstNum = match.groupValues[4].toIntOrNull() ?: return null
            val secondNum = match.groupValues[5].ifBlank { match.groupValues[7] }.toIntOrNull() ?: return null

            val monthNum = if (config.useEuropeanDate) secondNum else firstNum
            val dayNum = if (config.useEuropeanDate) firstNum else secondNum

            if (monthNum !in 1..12 || dayNum !in 1..31) return null

            date = date.run {
                copy(
                    startTime = startTime.copy(
                        monthNumber = monthNum,
                        dayOfMonth = dayNum
                    ),
                    tagsTimeStart = tagsTimeStart + TimeUnit.DAY + TimeUnit.MONTH
                )
            }
        }

        return date
    }
}