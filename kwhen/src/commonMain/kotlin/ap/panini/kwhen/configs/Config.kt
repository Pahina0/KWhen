package ap.panini.kwhen.configs

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.DayOfWeek
import ap.panini.kwhen.TimeUnit
import ap.panini.kwhen.common.Controller
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// for things which are uncertain such as evening being somewhere between 6 - 9pm
sealed class Config(
    internal open val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    internal open val relativeTo: Long = Clock.System.now().epochSeconds
) {
    abstract fun instance(): Controller

    internal fun getDateTime(
        text: String = "",
        range: IntRange = 0..0,

        startTime: LocalDateTime = nowZeroed(),

        endTime: LocalDateTime? = null,

        tagsDayOfWeek: Set<DayOfWeek> = mutableSetOf(),
        tagsTimeStart: Set<TimeUnit> = mutableSetOf(),
        tagsTimeEnd: Set<TimeUnit> = mutableSetOf(),

        repeatTag: TimeUnit? = null,
        repeatOften: Double? = null,

        generalTimeTag: TimeUnit? = null,
        generalNumber: Double? = null,

        points: Int = 1
    ) = DateTime(
        text,
        range,
        startTime,
        endTime,
        tagsDayOfWeek,
        tagsTimeStart,
        tagsTimeEnd,
        repeatTag,
        repeatOften,
        generalTimeTag,
        generalNumber,
        points
    )

    internal fun now() =
        Instant.fromEpochMilliseconds(relativeTo).toLocalDateTime(timeZone)

    internal fun nowZeroed() =
        with(Instant.fromEpochMilliseconds(relativeTo).toLocalDateTime(timeZone)) {
            LocalDateTime(year, month, dayOfMonth, hour, minute, 0, 0)
        }


}
