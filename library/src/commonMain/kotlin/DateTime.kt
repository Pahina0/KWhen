import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import util.mergeTime


/**
 * The result of the parsed string
 * */
data class DateTime(
    val text: String = "",
    val range: IntRange = 0..0,

    val startTime: LocalDateTime = nowZeroed,

    val endTime: LocalDateTime? = null,

    val tagsDayOfWeek: Set<TagDayOfWeek> = mutableSetOf(),
    val tagsTimeStart: Set<TagTime> = mutableSetOf(),
    val tagsTimeEnd: Set<TagTime> = mutableSetOf(),

    val repeatTag: TagTime? = null,
    val repeatOften: Int? = null,

    val generalTimeTag: TagTime? = null,
    val generalNumber: Int? = null,

    val points: Int = 1
) : Comparable<DateTime> {

    override fun compareTo(other: DateTime): Int = compareValuesBy(this, other,
        { points + tagsTimeStart.size + tagsTimeEnd.size },
        { it.range.first },
        { it.range.first - it.range.last }
    )


    fun merge(other: DateTime): DateTime {

        println("MERGING $this with $other")

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
            LocalDateTime(year, month, dayOfMonth, hour, 0, 0, 0)
        }
    }

}

