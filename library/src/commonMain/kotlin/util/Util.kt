package util

import DateTime
import TagTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun <T> Collection<T>.matchAny(): Regex = joinToString("|").replace(".", "\\.").toRegex()

val between31 = "(?<!\\d)(?:0?[1-9]|[12][0-9]|3[01])(?!\\d)".toRegex()


fun LocalDateTime.copy(
    year: Int = -1,
    monthNumber: Int = -1,
    dayOfMonth: Int = -1,
    hour: Int = -1,
    minute: Int = -1,
    second: Int = -1
): LocalDateTime = LocalDateTime(
    (if (year < 0) this.year else year) + (monthNumber - 1) / 12,
    if (monthNumber < 0) this.monthNumber else ((monthNumber - 1) % 12 + 1),
    if (dayOfMonth < 0) this.dayOfMonth else dayOfMonth,
    if (hour < 0) this.hour else hour,
    if (minute < 0) this.minute else minute,
    if (second < 0) this.second else second
)

fun LocalDateTime.copy(
    date: LocalDate? = null, time: LocalTime? = null
): LocalDateTime = LocalDateTime(
    date ?: this.date,
    time ?: this.time
)


fun LocalDateTime.mergeTime(other: LocalDateTime?, tags: Set<TagTime>): LocalDateTime {
    if (other == null) return this

    var time = this
    tags.forEach {
        time = when (it) {
            TagTime.HOUR -> {
                time.copy(hour = other.hour)
            }

            TagTime.MINUTE -> {
                time.copy(minute = other.minute)
            }

            TagTime.SECOND -> {
                time.copy(second = other.second)
            }

            TagTime.DAY -> {
                time.copy(dayOfMonth = other.dayOfMonth)
            }

            TagTime.WEEK -> {
                time // this shouldn't do much?
            }

            TagTime.MONTH -> {
                time.copy(monthNumber = other.monthNumber)
            }

            TagTime.YEAR -> {
                time.copy(year = other.year)
            }

        }
    }

    return time
}

fun getDateTimeWithGeneral(
    generalNumber: Int,
    generalTag: TagTime,
    relativeTo: LocalDateTime?
): LocalDateTime {

    val now = DateTime.nowZeroed

    if (relativeTo == null) {
        return when (generalTag) {
            TagTime.SECOND -> now.copy(second = generalNumber)
            TagTime.MINUTE -> now.copy(minute = generalNumber)
            TagTime.HOUR -> now.copy(hour = generalNumber)
            TagTime.DAY -> now.copy(dayOfMonth = generalNumber)
            TagTime.MONTH -> now.copy(monthNumber = generalNumber)
            TagTime.YEAR -> now.copy(year = generalNumber)
            TagTime.WEEK -> now
        }

    }


    val duration = when (generalTag) {
        TagTime.SECOND -> generalNumber.seconds
        TagTime.MINUTE -> generalNumber.minutes
        TagTime.HOUR -> generalNumber.hours
        TagTime.DAY -> (generalNumber * 24).hours
        TagTime.WEEK -> (generalNumber * 24 * 7).hours
        TagTime.MONTH -> return relativeTo.copy(monthNumber = relativeTo.monthNumber + generalNumber)
        TagTime.YEAR -> return relativeTo.copy(year = relativeTo.year + generalNumber)

    }

    return relativeTo
        .toInstant(TimeZone.currentSystemDefault())
        .plus(duration)
        .toLocalDateTime(TimeZone.currentSystemDefault())

}

fun Set<TagTime>.getRepeatTime(): TagTime? {
    if (contains(TagTime.YEAR)) return null // every 1989 won't make sense
    if (contains(TagTime.MONTH)) return TagTime.YEAR // every april -> repeat once a year
    if (contains(TagTime.WEEK)) return TagTime.WEEK // every monday -> repeat once a week
    if (contains(TagTime.DAY)) return TagTime.MONTH // every 5th -> repeat once a month
    if (contains(TagTime.HOUR)) return TagTime.DAY // every 3am -> repeat once a day
    if (contains(TagTime.MINUTE)) return TagTime.HOUR // every :03 -> repeat once an hour
    if (contains(TagTime.SECOND)) return TagTime.MINUTE // every :--:03 -> repeat once an minute

    return null
}