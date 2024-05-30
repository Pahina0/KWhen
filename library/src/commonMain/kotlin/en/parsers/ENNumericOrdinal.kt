package en.parsers

import DateTime
import TagTime
import common.ParserByWord
import en.ENConfig
import en.ordinal
import util.between31
import util.copy
import util.matchAny

/**
 * ex:
 * 12th
 * 15
 * 3:05 am
 * */
class ENNumericOrdinal(override val config: ENConfig) : ParserByWord(config) {
    override val matchPattern: Regex
        get() = "($between31)(?:(th|st|rd|nd)|(?:(?:\\s+|\\s*:\\s*)(\\d{1,2}))?\\s*([ap]\\.?m\\.?)?)|(${ordinal.keys.matchAny()})".toRegex()

    override fun onMatch(match: MatchResult): DateTime? {
        var date = DateTime()
        println(match.value)


        val amPm: String? = match.groupValues[4].let {
            if (it.isBlank()) null
            else it.replace(".", "")
        }

        val timeHour = match.groupValues[1].lowercase()
        val isOrdinal = match.groupValues[2].isNotBlank()
        val minute = match.groupValues[3].lowercase()
        val timeOrdinal = match.groupValues[5].lowercase()

        date = if (timeOrdinal.isNotBlank()) { // is date
            date.copy(
                startTime = date.startTime.copy(
                    dayOfMonth = ordinal[timeOrdinal] ?: return null
                ),

                tagsTimeStart = date.tagsTimeStart + TagTime.DAY
            )


        } else if (isOrdinal) {

            // 3rd:08am it doesn't make sense
            // if (minute.isNotBlank()) return null

            // is date: ex on the 3rd
            date.copy(
                startTime = date.startTime.copy(
                    dayOfMonth = timeHour.toIntOrNull() ?: return null

                ),

                tagsTimeStart = date.tagsTimeStart + TagTime.DAY
            )

        } else {

            val hour = timeHour.toIntOrNull() ?: return null

            // above or equals to 24 hrs
            if (hour >= 24) return null // 13:24am doesn't make sense
            if (hour > 12 && amPm == "am") return null

            if ((minute.toIntOrNull() ?: 0) >= 60) return null

            date.run {
                copy(
                    startTime = startTime.copy(
                        hour = if (config.use24) {
                            hour
                        } else {
                            val calculatedHour = when (amPm) {
                                "am" -> hour
                                "pm" -> hour + 12
                                else -> { // finds the next available time
                                    // at 3 (currently 5am) -> 3pm
                                    // at 5 (currently 4pm) -> 5pm
                                    // at 3 (currently 5pm) -> 3am
                                    val currentHour = startTime.hour
                                    if (currentHour < hour) {
                                        hour
                                    } else if (currentHour < hour + 12) {
                                        hour + 12 % 12
                                    } else {
                                        hour
                                    }
                                }
                            }

                            calculatedHour
                        },
                        minute = if (minute.isBlank()) 0
                        else minute.toIntOrNull() ?: return null
                    ),

                    tagsTimeStart = tagsTimeStart + TagTime.HOUR + TagTime.MINUTE
                )
            }

        }

        return date
    }
}