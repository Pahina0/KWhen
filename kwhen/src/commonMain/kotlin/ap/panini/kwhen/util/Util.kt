package ap.panini.kwhen.util

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.TimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal fun <T> Collection<T>.matchAny(): Regex = joinToString("|").replace(".", "\\.").toRegex()

internal val between31 = "(?<!\\d)(?:0?[1-9]|[12][0-9]|3[01])(?!\\d)".toRegex()


internal fun LocalDateTime.copy(
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

internal fun LocalDateTime.copy(
    date: LocalDate? = null, time: LocalTime? = null
): LocalDateTime = LocalDateTime(
    date ?: this.date,
    time ?: this.time
)


internal fun LocalDateTime.mergeTime(other: LocalDateTime?, tags: Set<TimeUnit>): LocalDateTime {
    if (other == null) return this

    var time = this
    tags.forEach {
        time = when (it) {
            TimeUnit.HOUR -> {
                time.copy(hour = other.hour)
            }

            TimeUnit.MINUTE -> {
                time.copy(minute = other.minute)
            }

            TimeUnit.SECOND -> {
                time.copy(second = other.second)
            }

            TimeUnit.DAY -> {
                time.copy(dayOfMonth = other.dayOfMonth)
            }

            TimeUnit.WEEK -> {
                time // this shouldn't do much?
            }

            TimeUnit.MONTH -> {
                time.copy(monthNumber = other.monthNumber)
            }

            TimeUnit.YEAR -> {
                time.copy(year = other.year)
            }

        }
    }

    return time
}

internal fun getDateTimeWithGeneral(
    generalNumber: Double,
    generalTag: TimeUnit,
    relativeTo: LocalDateTime?
): LocalDateTime {
    if  (generalNumber.rem(1).equals(0.0)) {
        return getDateTimeWithGeneral(generalNumber.toInt(), generalTag, relativeTo)
    }
    val (tag, num) = generalTag.partial(generalNumber)

    return getDateTimeWithGeneral(num, tag, relativeTo)
}

private fun getDateTimeWithGeneral(
    generalNumber: Int,
    generalTag: TimeUnit,
    relativeTo: LocalDateTime?
): LocalDateTime {

    val now = DateTime.nowZeroed

    if (relativeTo == null) {
        return when (generalTag) {
            TimeUnit.SECOND -> now.copy(second = generalNumber)
            TimeUnit.MINUTE -> now.copy(minute = generalNumber)
            TimeUnit.HOUR -> now.copy(hour = generalNumber)
            TimeUnit.DAY -> now.copy(dayOfMonth = generalNumber)
            TimeUnit.MONTH -> now.copy(monthNumber = generalNumber)
            TimeUnit.YEAR -> now.copy(year = generalNumber)
            TimeUnit.WEEK -> now
        }
    }


    val duration = when (generalTag) {
        TimeUnit.SECOND -> generalNumber.seconds
        TimeUnit.MINUTE -> generalNumber.minutes
        TimeUnit.HOUR -> generalNumber.hours
        TimeUnit.DAY -> (generalNumber * 24).hours
        TimeUnit.WEEK -> (generalNumber * 24 * 7).hours
        TimeUnit.MONTH -> return relativeTo.copy(monthNumber = relativeTo.monthNumber + generalNumber)
        TimeUnit.YEAR -> return relativeTo.copy(year = relativeTo.year + generalNumber)

    }

    return relativeTo
        .toInstant(TimeZone.currentSystemDefault())
        .plus(duration)
        .toLocalDateTime(TimeZone.currentSystemDefault())

}

internal fun Set<TimeUnit>.getRepeatTime(): TimeUnit? {
    if (contains(TimeUnit.YEAR)) return null // every 1989 won't make sense
    if (contains(TimeUnit.MONTH)) return TimeUnit.YEAR // every april -> repeat once a year
    if (contains(TimeUnit.WEEK)) return TimeUnit.WEEK // every monday -> repeat once a week
    if (contains(TimeUnit.DAY)) return TimeUnit.MONTH // every 5th -> repeat once a month
    if (contains(TimeUnit.HOUR)) return TimeUnit.DAY // every 3am -> repeat once a day
    if (contains(TimeUnit.MINUTE)) return TimeUnit.HOUR // every :03 -> repeat once an hour
    if (contains(TimeUnit.SECOND)) return TimeUnit.MINUTE // every :--:03 -> repeat once an minute

    return null
}