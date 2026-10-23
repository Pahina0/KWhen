import ap.panini.kwhen.TimeParser
import ap.panini.kwhen.configs.ENConfig
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

class RelativeTimesAndTimeZoneTest {

    @OptIn(ExperimentalTime::class)
    @Test
    fun relativeTimeTest() {
        TimeParser(
            ENConfig(
                // Date and time (GMT): Sunday, February 23, 2025 3:34:05 AM
                relativeTo = 1740281640000,
                timeZone = TimeZone.of("Europe/Belgrade")
            )
        ).parse(
            "I have to go ski tomorrow"
        ).let {
            assertEquals(
                1740281640000 + 1.days.inWholeMilliseconds,
                it.first().startTime.first().toInstant(TimeZone.of("Europe/Belgrade"))
                    .toEpochMilliseconds()
            )

        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun dayOfWeekAcrossDstTransitionsTest() {
        val nyZone = TimeZone.of("America/New_York")

        // Saturday, March 8, 2025 00:30 EST (Spring-forward happens Sunday, March 9, 2025 at 02:00)
        val beforeSpringForward = LocalDateTime(2025, 3, 8, 0, 30, 0)
            .toInstant(nyZone)
            .toEpochMilliseconds()

        TimeParser(
            ENConfig(
                relativeTo = beforeSpringForward,
                timeZone = nyZone
            )
        ).parse("We have a meeting on Monday").let {
            assertEquals(1, it.size)
            assertEquals(kotlinx.datetime.DayOfWeek.MONDAY, it[0].startTime.first().dayOfWeek)
            assertEquals(
                LocalDateTime(2025, 3, 10, 0, 30, 0),
                it[0].startTime.first(),
                "Across DST spring-forward, 'on Monday' should keep wall-clock time 00:30 instead of drifting to 01:30"
            )
        }

        // Saturday, November 1, 2025 00:30 EDT (Fall-back happens Sunday, November 2, 2025 at 02:00)
        val beforeFallBack = LocalDateTime(2025, 11, 1, 0, 30, 0)
            .toInstant(nyZone)
            .toEpochMilliseconds()

        TimeParser(
            ENConfig(
                relativeTo = beforeFallBack,
                timeZone = nyZone
            )
        ).parse("We have a meeting on Friday").let {
            assertEquals(1, it.size)
            assertEquals(
                kotlinx.datetime.DayOfWeek.FRIDAY,
                it[0].startTime.first().dayOfWeek,
                "Across DST fall-back from 00:30, 'on Friday' should land on Friday, not Thursday 23:30"
            )
            assertEquals(
                LocalDateTime(2025, 11, 7, 0, 30, 0),
                it[0].startTime.first()
            )
        }
    }

}