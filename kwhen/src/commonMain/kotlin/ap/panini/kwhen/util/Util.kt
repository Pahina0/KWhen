package ap.panini.kwhen.util

import ap.panini.kwhen.TimeUnit
import ap.panini.kwhen.configs.Config
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Match any takes a list and turns it into a regex that matches any item in that list
 *
 * @param T the tpe of list
 * @return a regex string that matches any value in the list
 */
internal fun <T> Collection<T>.matchAny(): Regex = joinToString("|").replace(".", "\\.").toRegex()

internal val between31 = "(?<!\\d)(?:0?[1-9]|[12][0-9]|3[01])(?!\\d)".toRegex()

/**
 * Copies a local date time with modified values
 * values can flow over to the next
 *
 * @param year
 * @param monthNumber
 * @param dayOfMonth
 * @param hour
 * @param minute
 * @param second
 * @return The copied date time with the given modifications
 */
internal fun LocalDateTime.copy(
    year: Int = -1,
    monthNumber: Int = -1,
    dayOfMonth: Int = -1,
    hour: Int = -1,
    minute: Int = -1,
    second: Int = -1,
): LocalDateTime {

    val mSec = if (second < 0) this.second else second

    val mMinute = if (minute < 0) this.minute else minute

    val mHour = if (hour < 0) this.hour else hour

    val mDayOfMonth = (if (dayOfMonth < 0) this.dayOfMonth else dayOfMonth) - 1

    // gets the month and year info
    var inst = LocalDateTime(
        (if (year < 0) this.year else year) + (monthNumber - 1) / 12,
        if (monthNumber < 0) this.monthNumber else ((monthNumber - 1) % 12 + 1),
        1, // month can't be 0
        0,
        0,
        0,
    ).toInstant(TimeZone.UTC)


    // adds month days years ect.
    inst = inst.plus(mDayOfMonth.days)
    inst = inst.plus(mHour.hours)
    inst = inst.plus(mMinute.minutes)
    inst = inst.plus(mSec.seconds)

    return inst.toLocalDateTime(TimeZone.UTC)

}

/**
 * Copies a date time with only a local date and local time
 *
 * @param date
 * @param time
 * @return The copied date time with given modifications
 */
internal fun LocalDateTime.copy(
    date: LocalDate? = null, time: LocalTime? = null
): LocalDateTime = LocalDateTime(
    date ?: this.date,
    time ?: this.time
)


/**
 * Merge time merges two times together by time units
 *
 * @param other the other time to merge into
 * @param tags any types of tags you want one to merge into the other for
 * @return a merged date time with merged tags
 */
internal fun LocalDateTime.mergeTime(
    other: LocalDateTime?,
    tags: Set<TimeUnit>
): LocalDateTime {
    if (other == null) return this

    var time = this

    // has to be sorted or else you may try to set day before month which may cause
    // out of bounds like feb 31st
    tags.sortedDescending().forEach {
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

/**
 * Get date time with general finds a date that would represent a number and a time unit
 * ex: 3 and hour increases the time relative to by 3 hours
 *
 * @param generalNumber the general number you want to use
 * @param generalTag what time unit you want your general number to change
 * @param relativeTo what time your general number and tag should be based off of
 * @return a time that got changed number and tags off from relative time
 */
internal fun getDateTimeWithGeneral(
    generalNumber: Double,
    generalTag: TimeUnit,
    relativeTo: LocalDateTime?,
    config: Config
): LocalDateTime {
    // decimal is a whole number
    if (generalNumber.rem(1).equals(0.0)) {
        return getDateTimeWithGeneral(generalNumber.toInt(), generalTag, relativeTo, config)
    }

    // finds a whole number time using a partial time
    val (tag, num) = generalTag.partial(generalNumber)

    return getDateTimeWithGeneral(num, tag, relativeTo, config)
}

/**
 * Get date time with general finds the date time based off a general number but only takes in an int
 *
 * @param generalNumber
 * @param generalTag
 * @param relativeTo
 * @return
 */
private fun getDateTimeWithGeneral(
    generalNumber: Int,
    generalTag: TimeUnit,
    relativeTo: LocalDateTime?,
    config: Config
): LocalDateTime {

    val now = config.nowZeroed()

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



    return relativeTo
        .toInstant(config.timeZone)
        .plus(
            DateTimePeriod(
                seconds = if (generalTag == TimeUnit.SECOND) generalNumber else 0,
                minutes = if (generalTag == TimeUnit.MINUTE) generalNumber else 0,
                hours = if (generalTag == TimeUnit.HOUR) generalNumber else 0,
                days = if (generalTag == TimeUnit.DAY) generalNumber else if (generalTag == TimeUnit.WEEK) 7 * generalNumber else 0,
                months = if (generalTag == TimeUnit.MONTH) generalNumber else 0,
                years = if (generalTag == TimeUnit.YEAR) generalNumber else 0,
            ),
            config.timeZone
        )
        .toLocalDateTime(config.timeZone)

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