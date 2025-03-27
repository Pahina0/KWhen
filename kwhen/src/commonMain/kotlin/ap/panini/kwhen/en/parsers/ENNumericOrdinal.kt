package ap.panini.kwhen.en.parsers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.TimeUnit
import ap.panini.kwhen.common.parsers.ParserByWord
import ap.panini.kwhen.configs.ENConfig
import ap.panini.kwhen.en.ordinal
import ap.panini.kwhen.util.between31
import ap.panini.kwhen.util.copy
import ap.panini.kwhen.util.matchAny
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

/**
 * En numeric ordinal finds time units that only have numbers in them
 * ex:
 * 12th
 * 3:05 am
 *
 * @property config
 * @constructor Create empty E n numeric ordinal
 */
internal class ENNumericOrdinal(override val config: ENConfig) : ParserByWord(config) {
    override val matchPattern: Regex
        get() = "($between31)(?:(th|st|rd|nd)|(?:(?:\\s+|\\s*(:)\\s*)(\\d{1,2}))?\\s*([ap]\\.?m\\.?)?)|(${ordinal.keys.matchAny()})".toRegex()

    override fun onMatch(match: MatchResult): DateTime? {
        var date = config.getDateTime()


        val amPm: String? = match.groupValues[5].let {
            if (it.isBlank()) null
            else it.replace(".", "")
        }

        val hasColon = match.groupValues[3].isNotBlank()

        val timeHour = match.groupValues[1].lowercase()
        val isOrdinal = match.groupValues[2].isNotBlank()
        val minute = match.groupValues[4].lowercase()
        val timeOrdinal = match.groupValues[6].lowercase()

        date = if (timeOrdinal.isNotBlank()) { // is date
            date.copy(
                startTime = date.startTime.copy(
                    dayOfMonth = ordinal[timeOrdinal] ?: return null
                ),

                tagsTimeStart = date.tagsTimeStart + TimeUnit.DAY
            )


        } else if (isOrdinal) {
            // is date: ex on the 3rd
            date.copy(
                startTime = date.startTime.copy(
                    dayOfMonth = timeHour.toIntOrNull() ?: return null

                ),

                tagsTimeStart = date.tagsTimeStart + TimeUnit.DAY
            )

        } else {

            val hour = timeHour.toIntOrNull() ?: return null

            // above or equals to 24 hrs
            if (hour >= 24) return null // 13:24am doesn't make sense
            if (amPm == null && !hasColon) return null // remove just numbers
            if (hour > 12 && amPm == "am") return null

            if ((minute.toIntOrNull() ?: 0) >= 60) return null
        var additionalDays = 0
            val tempDate = date.run {
                copy(
                    startTime = startTime.copy(
                        hour = hour.let { hr ->

                            // using am/pm
                            if (amPm == "am") {
                                return@let hr
                            } else if (amPm == "pm") {
                                return@let hr + 12
                            }

                            // next part finds the next days time so its always tmrw
                            // at 3 (currently 5am) -> 3pm
                            // at 5 (currently 4pm) -> 5pm
                            // at 3 (currently 5pm) -> 3am

                            // if they use 24 hr time then you want to check by 24 hrs
                            val addBy = if (config.use24 || hour > 12) 24 else 12
                            val min = minute.toIntOrNull() ?: 0
                            val curTime = startTime.hour * 60 + startTime.minute

                            if (curTime < hour * 60 + min) {
                                hour
                            } else if (curTime < (hour + addBy) * 60 + min) {
                                additionalDays = (hour + addBy) / 24
                                (hour + addBy) % 24
                            } else {
                                additionalDays = 1
                                hour
                            }
                        },
                        minute = if (minute.isBlank()) {
                            0
                        } else {
                            minute.toIntOrNull() ?: return null
                        }
                    ),

                    tagsTimeStart = tagsTimeStart + TimeUnit.HOUR + TimeUnit.MINUTE
                )
            }

            // calculates the next day since things like at 4, when its already 5, you would want to go next day
            return if (additionalDays > 0) {
                val newTime =
                    (tempDate.startTime.toInstant(config.timeZone) + additionalDays.days).toLocalDateTime(
                        config.timeZone
                    )

                val newTags = tempDate.tagsTimeStart.toMutableSet()
                newTags += TimeUnit.DAY

                if (tempDate.startTime.monthNumber != newTime.monthNumber) {
                    newTags += TimeUnit.MONTH
                }

                if (tempDate.startTime.year != newTime.year) {
                    newTags += TimeUnit.YEAR
                }


                tempDate.copy(
                    startTime = newTime,
                    tagsTimeStart = newTags
                )
            } else {
                tempDate
            }
        }

        return date
    }
}