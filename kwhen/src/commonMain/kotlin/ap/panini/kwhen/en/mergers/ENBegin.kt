package ap.panini.kwhen.en.mergers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.TimeUnit
import ap.panini.kwhen.common.mergers.MergerWhitespaceTrimmed
import ap.panini.kwhen.configs.ENConfig
import ap.panini.kwhen.util.copy
import ap.panini.kwhen.util.getDateTimeWithGeneral
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.number
import kotlinx.datetime.plus

/**
 * En begin finds words that come before times that can be merged with times
 *
 * @property config
 * @constructor Create empty En begin
 */
internal class ENBegin(override val config: ENConfig) : MergerWhitespaceTrimmed(config) {
    override val prefixMatchPattern: Regex
        get() = "(?:starting\\s+)?(from|on|at|during|in|after)(?:\\s+the)?|the|next".toRegex()

    override val mergePrefixWithLeft: Boolean
        get() = true

    override fun onMatch(
        left: DateTime?,
        right: DateTime?,
        prefix: MatchResult?,
        between: MatchResult?,
    ): DateTime? {
        if (left == null || prefix == null) return null

        if (prefix.value.trim() == "next" && left.generalTimeTag != null) {

            return left.copy(
                startTime = getDateTimeWithGeneral(
                    left.generalNumber ?: 1.0,
                    left.generalTimeTag,
                    left.startTime,
                    config
                ),
                tagsTimeStart = left.tagsTimeStart + left.generalTimeTag,
                generalTimeTag = null,
                generalNumber = null,
            )
        }

        if (left.generalNumber == null) {
            return left.copy()
        }

        if (setOf(
                "from",
                "on",
                "at",
                "during",
                "after"
            ).contains(prefix.value.trim())
        ) {
            if (left.text == "a" || left.text == "an") return null // "on a boat" isn't a time

            // "after 3 hours" / "after 2 days" (with an explicit unit) is a relative offset, not an absolute date/clock-hour
            if (prefix.value.trim() == "after" && left.generalTimeTag != null) {
                return left.copy(
                    startTime = getDateTimeWithGeneral(
                        left.generalNumber,
                        left.generalTimeTag,
                        left.endTime ?: left.startTime,
                        config
                    ),
                    tagsTimeStart = left.tagsTimeStart + left.generalTimeTag,
                    generalTimeTag = null,
                    generalNumber = null,
                )
            }

            if (left.generalTimeTag == null) {
                if (prefix.value.trim() == "on") return null
                if (left.generalNumber !in 0.0..24.0) return null
            }

            if (left.generalTimeTag == TimeUnit.HOUR || left.generalTimeTag == null) {
                var additionalDays = 0

                val currentHour =
                    config.now().hour
                val hour = if (config.use24) {
                    left.generalNumber
                } else {
                    if (currentHour < left.generalNumber) {
                        left.generalNumber
                    } else if (currentHour < left.generalNumber + 12) {
                        additionalDays = ((left.generalNumber + 12) / 24).toInt()
                        (left.generalNumber + 12) % 24
                    } else {
                        additionalDays = 1
                        left.generalNumber
                    }
                }

                val tempDate = left.copy(
                    startTime = getDateTimeWithGeneral(
                        hour,
                        TimeUnit.HOUR,
                        null,
                        config
                    ).copy(
                        minute = 0
                    ),
                    tagsTimeStart = left.tagsTimeStart + TimeUnit.HOUR + TimeUnit.MINUTE,
                    generalTimeTag = null,
                    generalNumber = null,
                )

                // calculates the next day since things like at 4, when its already 5, you would want to go next day
                return if (additionalDays > 0) {
                    val newTime = tempDate.startTime.copy(
                        date = tempDate.startTime.date.plus(additionalDays, DateTimeUnit.DAY)
                    )

                    val newTags = tempDate.tagsTimeStart.toMutableSet()
                    newTags += TimeUnit.DAY

                    if (tempDate.startTime.month.number != newTime.month.number) {
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

            return left.copy(
                startTime = getDateTimeWithGeneral(
                    left.generalNumber,
                    left.generalTimeTag,
                    null,
                    config
                ),
                tagsTimeStart = left.tagsTimeStart + left.generalTimeTag,
                generalTimeTag = null,
                generalNumber = null,
            )
        }

        return left.copy(
            startTime = getDateTimeWithGeneral(
                left.generalNumber,
                left.generalTimeTag ?: TimeUnit.HOUR,
                left.endTime ?: left.startTime,
                config
            ),
            tagsTimeStart = left.tagsTimeStart + (left.generalTimeTag ?: TimeUnit.HOUR),
            generalTimeTag = null,
            generalNumber = null,
        )


    }
}