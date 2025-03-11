import ap.panini.kwhen.TimeParser
import ap.panini.kwhen.common.Parser
import ap.panini.kwhen.configs.ENConfig
import kotlin.test.Test
import kotlin.test.assertEquals

class RelativeTimesAndTimeZoneTest {

    @Test
    fun relativeTimeTest() {
        TimeParser(
            ENConfig(
                // Date and time (GMT): Sunday, February 23, 2025 3:34:05 AM
                relativeTo = 1740281645000
            )
        ).parse(
            "I have to go ski tomorrow"
        ).let {
            println(it)
            assertEquals(
                1740281645000, it.first().startTime.first().nanosecond.toLong()
            )
        }
    }

}