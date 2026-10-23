import ap.panini.kwhen.TimeParser
import ap.panini.kwhen.configs.ENConfig
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class MetamorphicMassTest {

    private val utc = TimeZone.UTC

    @Test
    fun test24HourAnchorSweepIn12HourMode() {
        val failures = mutableListOf<String>()
        var totalCases = 0

        // Sweep anchor across all 24 hours of the day (0..23)
        for (anchorHour in 0..23) {
            for (anchorMin in listOf(0, 30)) {
                val anchor = LocalDateTime(2025, 6, 15, anchorHour, anchorMin, 0)
                val anchorInst = anchor.toInstant(utc)
                val config = ENConfig(
                    use24 = false,
                    relativeTo = anchorInst.toEpochMilliseconds(),
                    timeZone = utc
                )
                val parser = TimeParser(config)

                // Test bare hours "at 1" .. "at 12" and colon times "1:15" .. "12:15"
                for (targetHour in 1..12) {
                    for (expr in listOf("at $targetHour", "at $targetHour:15")) {
                        totalCases++
                        val parsed = parser.parse("Meet me $expr")
                        if (parsed.size != 1) {
                            failures += "SIZE MISMATCH at anchor $anchor for '$expr': got ${parsed.size}"
                            continue
                        }

                        val resolved = parsed[0].startTime.first()
                        val resolvedInst = resolved.toInstant(utc)
                        val diffMinutes = (resolvedInst - anchorInst).inWholeMinutes

                        // In 12h mode without am/pm, resolved time should never be in the past
                        // (except when targetHour == 12 at 12:30 where 12+12=24 wraps to 00:xx next day)
                        if (diffMinutes < 0 || diffMinutes > 24 * 60) {
                            failures += "ROLLOVER INVARIANT VIOLATION at anchor $anchor for '$expr': resolved to $resolved (diff=${diffMinutes}m)"
                        }
                    }
                }
            }
        }

        assertTrue(
            failures.isEmpty(),
            "Failed ${failures.size} / $totalCases 24-hour anchor sweep cases:\n" +
                failures.take(30).joinToString("\n")
        )
    }

    @Test
    fun testCalendarBoundaryAndLeapYearSweep() {
        val failures = mutableListOf<String>()
        var totalCases = 0

        val boundaryAnchors = listOf(
            LocalDateTime(2024, 2, 28, 12, 0, 0), // day before leap day
            LocalDateTime(2024, 2, 29, 12, 0, 0), // leap day
            LocalDateTime(2025, 2, 28, 12, 0, 0), // non-leap Feb 28
            LocalDateTime(2025, 1, 31, 23, 30, 0), // Jan 31 late night
            LocalDateTime(2025, 4, 30, 23, 30, 0), // Apr 30 late night
            LocalDateTime(2025, 12, 31, 23, 30, 0) // New Year's Eve late night
        )

        val weekdays = listOf(
            "Monday" to kotlinx.datetime.DayOfWeek.MONDAY,
            "Tuesday" to kotlinx.datetime.DayOfWeek.TUESDAY,
            "Wednesday" to kotlinx.datetime.DayOfWeek.WEDNESDAY,
            "Thursday" to kotlinx.datetime.DayOfWeek.THURSDAY,
            "Friday" to kotlinx.datetime.DayOfWeek.FRIDAY,
            "Saturday" to kotlinx.datetime.DayOfWeek.SATURDAY,
            "Sunday" to kotlinx.datetime.DayOfWeek.SUNDAY
        )

        for (anchor in boundaryAnchors) {
            val anchorMillis = anchor.toInstant(utc).toEpochMilliseconds()
            val parser = TimeParser(ENConfig(relativeTo = anchorMillis, timeZone = utc))

            // 1. "tomorrow" -> anchor.date + 1 day
            totalCases++
            val tmrw = parser.parse("See you tomorrow")
            val expectedTmrw = anchor.date.plus(1, DateTimeUnit.DAY)
            if (tmrw.size != 1 || tmrw[0].startTime.first().date != expectedTmrw) {
                failures += "TOMORROW MISMATCH at $anchor: expected $expectedTmrw, got ${tmrw.firstOrNull()?.startTime}"
            }

            // 2. "next week" -> anchor.date + 7 days
            totalCases++
            val nextWeek = parser.parse("See you next week")
            val expectedNextWeek = anchor.date.plus(7, DateTimeUnit.DAY)
            if (nextWeek.size != 1 || nextWeek[0].startTime.first().date != expectedNextWeek) {
                failures += "NEXT WEEK MISMATCH at $anchor: expected $expectedNextWeek, got ${nextWeek.firstOrNull()?.startTime}"
            }

            // 3. All 7 weekdays
            for ((dayName, expectedDow) in weekdays) {
                totalCases++
                val res = parser.parse("Meeting on $dayName")
                if (res.size != 1) {
                    failures += "WEEKDAY SIZE MISMATCH at $anchor for '$dayName': got ${res.size}"
                    continue
                }
                val actual = res[0].startTime.first()
                if (actual.dayOfWeek != expectedDow) {
                    failures += "WEEKDAY MISMATCH at $anchor for '$dayName': expected $expectedDow, got ${actual.dayOfWeek} ($actual)"
                }
            }
        }

        assertTrue(
            failures.isEmpty(),
            "Failed ${failures.size} / $totalCases Calendar Boundary & Leap Year sweep cases:\n" +
                failures.take(30).joinToString("\n")
        )
    }

    @Test
    fun testDstTransitionSweepAcrossAllWeekdays() {
        val failures = mutableListOf<String>()
        var totalCases = 0
        val zones = listOf(
            TimeZone.of("America/New_York"),
            TimeZone.of("Europe/Belgrade")
        )
        val weekdays = listOf(
            "Monday" to kotlinx.datetime.DayOfWeek.MONDAY,
            "Tuesday" to kotlinx.datetime.DayOfWeek.TUESDAY,
            "Wednesday" to kotlinx.datetime.DayOfWeek.WEDNESDAY,
            "Thursday" to kotlinx.datetime.DayOfWeek.THURSDAY,
            "Friday" to kotlinx.datetime.DayOfWeek.FRIDAY,
            "Saturday" to kotlinx.datetime.DayOfWeek.SATURDAY,
            "Sunday" to kotlinx.datetime.DayOfWeek.SUNDAY
        )

        // Pre-DST dates in 2025:
        // US Spring-Forward: Mar 9, 2025; US Fall-Back: Nov 2, 2025
        // EU Spring-Forward: Mar 30, 2025; EU Fall-Back: Oct 26, 2025
        val preDstDates = listOf(
            Triple(2025, 3, 8),
            Triple(2025, 11, 1),
            Triple(2025, 3, 29),
            Triple(2025, 10, 25)
        )

        for (zone in zones) {
            for ((y, m, d) in preDstDates) {
                for (hour in listOf(0, 1, 12, 23)) {
                    val anchor = LocalDateTime(y, m, d, hour, 30, 0)
                    val anchorMillis = anchor.toInstant(zone).toEpochMilliseconds()
                    val parser = TimeParser(ENConfig(relativeTo = anchorMillis, timeZone = zone))

                    for ((dayName, expectedDow) in weekdays) {
                        totalCases++
                        val parsed = parser.parse("Schedule on $dayName")
                        if (parsed.size != 1) {
                            failures += "DST SIZE MISMATCH ($zone, anchor=$anchor, $dayName): got ${parsed.size}"
                            continue
                        }
                        val actual = parsed[0].startTime.first()
                        if (actual.dayOfWeek != expectedDow) {
                            failures += "DST DOW MISMATCH ($zone, anchor=$anchor, $dayName): expected $expectedDow, got ${actual.dayOfWeek} ($actual)"
                        }
                        if (actual.hour != hour || actual.minute != 30) {
                            failures += "DST HOUR DRIFT ($zone, anchor=$anchor, $dayName): expected ${hour}:30, got ${actual.hour}:${actual.minute} ($actual)"
                        }
                    }
                }
            }
        }

        assertTrue(
            failures.isEmpty(),
            "Failed ${failures.size} / $totalCases DST Transition sweep cases:\n" +
                failures.take(30).joinToString("\n")
        )
    }

    @Test
    fun testPaddingAndCasingMetamorphicInvariants() {
        val failures = mutableListOf<String>()
        var totalCases = 0
        val anchorMillis = LocalDateTime(2025, 6, 11, 9, 0, 0).toInstant(utc).toEpochMilliseconds()
        val parser = TimeParser(ENConfig(use24 = true, relativeTo = anchorMillis, timeZone = utc))

        val expressions = listOf(
            "today",
            "tomorrow at 3:15pm",
            "yesterday in the evening",
            "on July 4th, 2026",
            "on the 1st of Feb",
            "on 09/25 at 10:30am",
            "from 8am to 4pm",
            "in 45 minutes",
            "in 3 days",
            "every Monday at 9am",
            "every other Tuesday",
            "biweekly",
            "next month"
        )

        val paddings = listOf(
            "" to "",
            "Please remember: " to "!",
            "Meeting scheduled " to " in the conference room.",
            "Urgent — " to " — do not miss it",
            "İstanbul office sync " to " with the team" // Includes Unicode 'İ'
        )

        fun alternateCase(s: String): String =
            s.mapIndexed { idx, ch -> if (idx % 2 == 0) ch.uppercaseChar() else ch.lowercaseChar() }.joinToString("")

        for (baseExpr in expressions) {
            val baselineList = parser.parse(baseExpr)
            if (baselineList.size != 1) {
                failures += "BASELINE FAILED on '$baseExpr': got ${baselineList.size}"
                continue
            }
            val baseline = baselineList.first()

            val caseVariants = listOf(
                baseExpr,
                baseExpr.lowercase(),
                baseExpr.uppercase(),
                alternateCase(baseExpr)
            )

            for ((prefix, suffix) in paddings) {
                for (casedExpr in caseVariants) {
                    val input = "$prefix$casedExpr$suffix"
                    totalCases++

                    val parsed = try {
                        parser.parse(input)
                    } catch (e: Exception) {
                        failures += "CRASH on '$input': ${e::class.simpleName}: ${e.message}"
                        continue
                    }

                    if (parsed.size != 1) {
                        failures += "METAMORPHIC SIZE MISMATCH on '$input': expected 1, got ${parsed.size} (${parsed.map { it.text }})"
                        continue
                    }

                    val item = parsed.first()
                    if (item.startTime != baseline.startTime) {
                        failures += "START_TIME INVARIANT VIOLATION on '$input': expected ${baseline.startTime}, got ${item.startTime}"
                    }
                    if (item.endTime != baseline.endTime) {
                        failures += "END_TIME INVARIANT VIOLATION on '$input': expected ${baseline.endTime}, got ${item.endTime}"
                    }
                    if (item.tagsTimeStart != baseline.tagsTimeStart) {
                        failures += "TAGS INVARIANT VIOLATION on '$input': expected ${baseline.tagsTimeStart}, got ${item.tagsTimeStart}"
                    }
                    if (item.repeatTag != baseline.repeatTag || item.repeatOften != baseline.repeatOften) {
                        failures += "REPEAT INVARIANT VIOLATION on '$input': expected (${baseline.repeatTag}, ${baseline.repeatOften}), got (${item.repeatTag}, ${item.repeatOften})"
                    }
                    if (item.text != input.substring(item.range)) {
                        failures += "SUBSTRING MISMATCH on '$input': item.text='${item.text}' != substring='${input.substring(item.range)}'"
                    }
                    if (!item.text.trim().equals(casedExpr.trim(), ignoreCase = true)) {
                        failures += "EXTRACTED TEXT MISMATCH on '$input': expected '$casedExpr', got '${item.text.trim()}'"
                    }
                }
            }
        }

        assertTrue(
            failures.isEmpty(),
            "Failed ${failures.size} / $totalCases Metamorphic Padding & Casing invariant cases:\n" +
                failures.take(30).joinToString("\n")
        )
    }
}
