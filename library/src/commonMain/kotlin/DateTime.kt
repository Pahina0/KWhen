import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


/**
 * The result of the parsed string
 * */
data class DateTime(
    val text: String = "",
    val range: IntRange = 0..0,

    val startTime: LocalDateTime = with(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) {
        LocalDateTime(year, month, dayOfMonth, hour, 0, 0, 0)
    },

    val endTime: LocalDateTime = with(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) {
        LocalDateTime(year, month, dayOfMonth, hour, 0, 0, 0)
    },

    val tagsDayOfWeek: Set<TagDayOfWeek> = mutableSetOf(),
    val tagsTime: Set<TagTime> = mutableSetOf(),
    val tagsRepeat: Set<TagTime> = mutableSetOf()
) : Comparable<DateTime> {

    override fun compareTo(other: DateTime): Int = compareValuesBy(this, other,
        { it.range.first },
        { it.range.first - it.range.last })


    fun merge(other: DateTime): DateTime {
        return this
    }
}

enum class TagTime {
    HOUR,
    MINUTE,
    SECOND,
    DAY,
    DAY_OF_WEEK,
    MONTH,
    YEAR,
}

enum class TagDayOfWeek {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
}

enum class TagRepeat {
    MONTH,
    YEAR,
    WEEK,
    DAY,
    MONTH_NAME,
    DAY_NAME,
}

