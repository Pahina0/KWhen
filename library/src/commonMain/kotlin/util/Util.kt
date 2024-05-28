package util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

fun <T> Collection<T>.matchAny(): Regex = joinToString("|").replace(".", "\\.").toRegex()

val between31 = "(?<!\\d)(?:0?[1-9]|[12][0-9]|3[01])(?!\\d)".toRegex()


fun LocalDateTime.copy(
    year: Int = -1, monthNumber: Int = -1, dayOfMonth: Int = -1, hour: Int = -1, minute: Int = -1, second: Int = -1
): LocalDateTime = LocalDateTime(
    if (year < 0) this.year else year,
    if (monthNumber < 0) this.monthNumber else monthNumber,
    if (dayOfMonth < 0) this.dayOfMonth else dayOfMonth,
    if (hour < 0) this.hour else hour,
    if (minute < 0) this.dayOfMonth else minute,
    if (second < 0) this.second else second
)

fun LocalDateTime.copy(
    date: LocalDate? = null, time: LocalTime? = null
): LocalDateTime = LocalDateTime(
    date ?: this.date,
    time ?: this.time
)
