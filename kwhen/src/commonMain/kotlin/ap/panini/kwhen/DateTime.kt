package ap.panini.kwhen

import ap.panini.kwhen.configs.Config
import ap.panini.kwhen.util.copy
import ap.panini.kwhen.util.getDateTimeWithGeneral
import ap.panini.kwhen.util.mergeTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus


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
                val base = if (other.tagsDayOfWeek.isNotEmpty()) {
                    ap.panini.kwhen.util.resolveDayOfWeek(
                        other.startTime,
                        kotlinx.datetime.DayOfWeek.entries[other.tagsDayOfWeek.first().ordinal]
                    )
                } else it.startTime

                if (endTime != null) {
                    val isTimeDuration = generalTimeTag in setOf(TimeUnit.HOUR, TimeUnit.MINUTE, TimeUnit.SECOND)
                    val hasExplicitUnit = if (isTimeDuration) {
                        TimeUnit.HOUR in other.tagsTimeStart
                    } else {
                        other.tagsTimeStart.any { it.ordinal >= TimeUnit.DAY.ordinal } || other.tagsDayOfWeek.isNotEmpty()
                    }

                    it.copy(
                        endTime = getDateTimeWithGeneral(
                            generalNumber,
                            generalTimeTag,
                            base,
                            config
                        ),
                        tagsTimeEnd = it.tagsTimeEnd + generalTimeTag,
                        generalNumber = if (hasExplicitUnit) null else generalNumber,
                        generalTimeTag = if (hasExplicitUnit) null else generalTimeTag
                    )
                } else {
                    it.copy(
                        startTime = getDateTimeWithGeneral(
                            generalNumber,
                            generalTimeTag,
                            base,
                            config
                        ),
                        tagsTimeStart = it.tagsTimeStart + generalTimeTag + (if (other.tagsDayOfWeek.isNotEmpty()) setOf(TimeUnit.DAY) else emptySet()),
                        tagsDayOfWeek = if (other.tagsDayOfWeek.isNotEmpty()) emptySet() else it.tagsDayOfWeek,
                        generalNumber = null,
                        generalTimeTag = null
                    )
                }
            }
        }

        if (other.generalNumber != null && other.generalTimeTag != null && other.endTime != null && !pureMerge) {
            val res = merge(other, config, true)
            val base = if (tagsDayOfWeek.isNotEmpty()) {
                ap.panini.kwhen.util.resolveDayOfWeek(
                    startTime,
                    kotlinx.datetime.DayOfWeek.entries[tagsDayOfWeek.first().ordinal]
                )
            } else res.startTime

            val isTimeDuration = other.generalTimeTag in setOf(TimeUnit.HOUR, TimeUnit.MINUTE, TimeUnit.SECOND)
            val hasExplicitUnit = if (isTimeDuration) {
                TimeUnit.HOUR in tagsTimeStart
            } else {
                tagsTimeStart.any { it.ordinal >= TimeUnit.DAY.ordinal } || tagsDayOfWeek.isNotEmpty()
            }

            return res.copy(
                endTime = getDateTimeWithGeneral(
                    other.generalNumber,
                    other.generalTimeTag,
                    base,
                    config
                ),
                tagsTimeEnd = res.tagsTimeEnd + other.generalTimeTag,
                generalNumber = if (hasExplicitUnit) null else other.generalNumber,
                generalTimeTag = if (hasExplicitUnit) null else other.generalTimeTag
            )
        }


        val thisIsDateOnly = TimeUnit.HOUR !in tagsTimeStart && tagsTimeStart.any { it.ordinal >= TimeUnit.DAY.ordinal }
        val otherIsDateOnly = TimeUnit.HOUR !in other.tagsTimeStart && other.tagsTimeStart.any { it.ordinal >= TimeUnit.DAY.ordinal }
        val thisIsTime = TimeUnit.HOUR in tagsTimeStart && tagsDayOfWeek.isEmpty()
        val otherIsTime = TimeUnit.HOUR in other.tagsTimeStart && other.tagsDayOfWeek.isEmpty()

        val leftHasMinute = TimeUnit.MINUTE in tagsTimeStart
        val rightHasMinute = TimeUnit.MINUTE in other.tagsTimeStart
        val oneIsSpecificTimeAndOtherIsVague = (leftHasMinute && !rightHasMinute && TimeUnit.HOUR in other.tagsTimeStart) ||
                (rightHasMinute && !leftHasMinute && TimeUnit.HOUR in tagsTimeStart)

        val mergedStartTime: LocalDateTime
        val mergedTagsTimeStart: Set<TimeUnit>

        if (oneIsSpecificTimeAndOtherIsVague && !pureMerge) {
            val (specific, vague) = if (leftHasMinute) this to other else other to this
            var specificHour = specific.startTime.hour
            val vagueHour = vague.startTime.hour
            if (vagueHour >= 12 && specificHour in 1..11) {
                specificHour += 12
            } else if (vagueHour < 12 && specificHour in 13..23) {
                specificHour -= 12
            }
            val resolvedTime = specific.startTime.copy(hour = specificHour)
            val dateTagsFromSpecific = specific.tagsTimeStart.filter { it.ordinal >= TimeUnit.DAY.ordinal }.toSet()
            val dateTagsFromVague = vague.tagsTimeStart.filter { it.ordinal >= TimeUnit.DAY.ordinal }.toSet()
            val allDateTags = dateTagsFromSpecific + dateTagsFromVague
            val baseDate = if (dateTagsFromSpecific.isNotEmpty()) specific.startTime else vague.startTime

            mergedStartTime = baseDate.mergeTime(resolvedTime, setOf(TimeUnit.HOUR, TimeUnit.MINUTE))
            mergedTagsTimeStart = tagsTimeStart + other.tagsTimeStart + setOf(TimeUnit.HOUR, TimeUnit.MINUTE) + allDateTags
        } else if (thisIsDateOnly && otherIsTime && !pureMerge) {
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

        var finalEndTime = endTime?.mergeTime(other.endTime, other.tagsTimeEnd) ?: other.endTime
        if (finalEndTime != null && mergedStartTime > finalEndTime && !pureMerge) {
            if (other.tagsTimeEnd.isNotEmpty() && other.tagsTimeEnd.all { it.ordinal <= TimeUnit.HOUR.ordinal }) {
                var newEndDate = mergedStartTime.date
                if (finalEndTime.time < mergedStartTime.time) {
                    newEndDate = newEndDate.plus(1, kotlinx.datetime.DateTimeUnit.DAY)
                }
                finalEndTime = finalEndTime.copy(date = newEndDate)
            }
        }

        return copy(
            startTime = mergedStartTime,
            endTime = finalEndTime,
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

