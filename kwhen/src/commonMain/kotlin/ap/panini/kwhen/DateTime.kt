package ap.panini.kwhen

import ap.panini.kwhen.util.getDateTimeWithGeneral
import ap.panini.kwhen.util.mergeTime
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


/**
 * The result of the parsed string
 * */
internal data class DateTime(
    val text: String = "",
    val range: IntRange = 0..0,

    val startTime: LocalDateTime = nowZeroed,

    val endTime: LocalDateTime? = null,

    val tagsDayOfWeek: Set<DayOfWeek> = mutableSetOf(),
    val tagsTimeStart: Set<TimeUnit> = mutableSetOf(),
    val tagsTimeEnd: Set<TimeUnit> = mutableSetOf(),

    val repeatTag: TimeUnit? = null,
    val repeatOften: Int? = null,

    val generalTimeTag: TimeUnit? = null,
    val generalNumber: Int? = null,

    val points: Int = 1
) : Comparable<DateTime> {


    override fun compareTo(other: DateTime): Int = compareValuesBy(this, other,
        { points + tagsTimeStart.size + tagsTimeEnd.size },
        { it.range.first },
        { it.range.first - it.range.last }
    )


    fun merge(other: DateTime, pureMerge: Boolean = false): DateTime {
        if (generalNumber != null && generalTimeTag != null && !pureMerge) {
            return merge(other, true).let {
                // will set to end time if the general number is an end time
                if (endTime != null) {
                    it.copy(
                        endTime = getDateTimeWithGeneral(
                            generalNumber,
                            generalTimeTag,
                            it.endTime
                        ),
                        tagsTimeEnd = it.tagsTimeEnd + generalTimeTag,
                        generalNumber = null,
                        generalTimeTag = null
                    )
                } else {
                    it.copy(
                        startTime = getDateTimeWithGeneral(
                            generalNumber,
                            generalTimeTag,
                            it.startTime
                        ),
                        tagsTimeStart = it.tagsTimeStart + generalTimeTag,
                        generalNumber = null,
                        generalTimeTag = null
                    )
                }
            }

        }

        return copy(
            startTime = startTime.mergeTime(other.startTime, other.tagsTimeStart),
            endTime = endTime?.mergeTime(other.endTime, other.tagsTimeEnd) ?: other.endTime,
            tagsDayOfWeek = tagsDayOfWeek + other.tagsDayOfWeek,
            tagsTimeStart = tagsTimeStart + other.tagsTimeStart,
            tagsTimeEnd = tagsTimeEnd + other.tagsTimeEnd,
            repeatTag = repeatTag ?: other.repeatTag,
            repeatOften = repeatOften ?: other.repeatOften,
            generalTimeTag = generalTimeTag ?: other.generalTimeTag,
            generalNumber = generalNumber ?: other.generalNumber,
            points = points + other.points
        )
    }

    companion object {
        val nowZeroed = with(
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        ) {
            LocalDateTime(year, month, dayOfMonth, hour, minute, 0, 0)
        }
    }

}

