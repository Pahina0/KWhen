package ap.panini.kwhen

import ap.panini.kwhen.configs.Config
import ap.panini.kwhen.util.getDateTimeWithGeneral
import ap.panini.kwhen.util.mergeTime
import kotlinx.datetime.LocalDateTime


/**
 * The result of the parsed string
 * */
internal data class DateTime(
    val text: String,
    val range: IntRange,

    val startTime: LocalDateTime,

    val endTime: LocalDateTime?,

    val tagsDayOfWeek: Set<DayOfWeek>,
    val tagsTimeStart: Set<TimeUnit>,
    val tagsTimeEnd: Set<TimeUnit>,

    val repeatTag: TimeUnit?,
    val repeatOften: Double?,

    val generalTimeTag: TimeUnit?,
    val generalNumber: Double?,

    val points: Int
) : Comparable<DateTime> {


    override fun compareTo(other: DateTime): Int = compareValuesBy(
        this, other,
        { it.range.first },
        { it.range.first - it.range.last },
        { -(it.points + it.tagsTimeStart.size + it.tagsTimeEnd.size) }
    )


    /**
     * Merge
     *
     * @param other
     * @param pureMerge takes the other time and combines all values blindly if true
     * @return
     */
    fun merge(other: DateTime, config: Config, pureMerge: Boolean = false): DateTime {
        if (generalNumber != null && generalTimeTag != null && !pureMerge) {
            return merge(other, config, true).let {
                // will set to end time if the general number is an end time
                if (endTime != null) {
                    it.copy(
                        endTime = getDateTimeWithGeneral(
                            generalNumber,
                            generalTimeTag,
                            it.endTime,
                            config
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
                            it.startTime,
                            config
                        ),
                        tagsTimeStart = it.tagsTimeStart + generalTimeTag,
                        generalNumber = null,
                        generalTimeTag = null
                    )
                }
            }

        }

        val thisIsDateOnly = TimeUnit.HOUR !in tagsTimeStart && tagsTimeStart.any { it.ordinal >= TimeUnit.DAY.ordinal }
        val otherIsDateOnly = TimeUnit.HOUR !in other.tagsTimeStart && other.tagsTimeStart.any { it.ordinal >= TimeUnit.DAY.ordinal }
        val thisIsTime = TimeUnit.HOUR in tagsTimeStart && tagsDayOfWeek.isEmpty()
        val otherIsTime = TimeUnit.HOUR in other.tagsTimeStart && other.tagsDayOfWeek.isEmpty()

        val mergedStartTime: LocalDateTime
        val mergedTagsTimeStart: Set<TimeUnit>

        if (thisIsDateOnly && otherIsTime && !pureMerge) {
            val otherTimeTags = other.tagsTimeStart.filter { it.ordinal <= TimeUnit.HOUR.ordinal }.toSet()
            mergedStartTime = startTime.mergeTime(other.startTime, otherTimeTags)
            mergedTagsTimeStart = tagsTimeStart + otherTimeTags
        } else if (thisIsTime && otherIsDateOnly && !pureMerge) {
            val thisTimeTags = tagsTimeStart.filter { it.ordinal <= TimeUnit.HOUR.ordinal }.toSet()
            mergedStartTime = config.nowZeroed().mergeTime(startTime, thisTimeTags).mergeTime(other.startTime, other.tagsTimeStart)
            mergedTagsTimeStart = thisTimeTags + other.tagsTimeStart
        } else {
            mergedStartTime = startTime.mergeTime(other.startTime, other.tagsTimeStart)
            mergedTagsTimeStart = tagsTimeStart + other.tagsTimeStart
        }

        return copy(
            startTime = mergedStartTime,
            endTime = endTime?.mergeTime(other.endTime, other.tagsTimeEnd)
                ?: other.endTime,
            tagsDayOfWeek = tagsDayOfWeek + other.tagsDayOfWeek,
            tagsTimeStart = mergedTagsTimeStart,
            tagsTimeEnd = tagsTimeEnd + other.tagsTimeEnd,
            repeatTag = repeatTag ?: other.repeatTag,
            repeatOften = repeatOften ?: other.repeatOften,
            generalTimeTag = generalTimeTag ?: other.generalTimeTag,
            generalNumber = generalNumber ?: other.generalNumber,
            points = points + other.points
        )
    }

}

