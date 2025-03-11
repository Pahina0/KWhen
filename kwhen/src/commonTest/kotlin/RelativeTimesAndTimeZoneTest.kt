import ap.panini.kwhen.TimeParser
import ap.panini.kwhen.common.Parser
import ap.panini.kwhen.configs.ENConfig
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days

class RelativeTimesAndTimeZoneTest {

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

}