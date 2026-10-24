import ap.panini.kwhen.TimeParser
import ap.panini.kwhen.TimeUnit
import ap.panini.kwhen.configs.ENConfig
import ap.panini.kwhen.util.copy
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class GrammarMassTest {

    // Fixed anchor: Wednesday, June 11, 2025 08:00:00 UTC
    private val zone = TimeZone.UTC
    private val anchorDateTime = LocalDateTime(2025, 6, 11, 8, 0, 0)
    private val anchorMillis = anchorDateTime.toInstant(zone).toEpochMilliseconds()

    private data class DateSpec(
        val phrase: String,
        val expectedYear: Int,
        val expectedMonth: Int,
        val expectedDay: Int,
        val expectedTags: Set<TimeUnit>,
        val useEuropeanDate: Boolean = false
    )

    private data class TimeSpec(
        val phrase: String,
        val expectedHour: Int,
        val expectedMinute: Int,
        val expectedTags: Set<TimeUnit>,
        val use24: Boolean = false
    )

    private val dateSpecs = listOf(
        DateSpec("today", 2025, 6, 11, setOf(TimeUnit.DAY)),
        DateSpec("tdy", 2025, 6, 11, setOf(TimeUnit.DAY)),
        DateSpec("tomorrow", 2025, 6, 12, setOf(TimeUnit.DAY)),
        DateSpec("tmrw", 2025, 6, 12, setOf(TimeUnit.DAY)),
        DateSpec("2morrow", 2025, 6, 12, setOf(TimeUnit.DAY)),
        DateSpec("yesterday", 2025, 6, 10, setOf(TimeUnit.DAY)),
        DateSpec("yest", 2025, 6, 10, setOf(TimeUnit.DAY)),
        DateSpec("on July 4th", 2025, 7, 4, setOf(TimeUnit.MONTH, TimeUnit.DAY)),
        DateSpec("on Feb 2nd", 2025, 2, 2, setOf(TimeUnit.MONTH, TimeUnit.DAY)),
        DateSpec("on Aug. 23rd, 2026", 2026, 8, 23, setOf(TimeUnit.YEAR, TimeUnit.MONTH, TimeUnit.DAY)),
        DateSpec("on the 1st of January", 2025, 1, 1, setOf(TimeUnit.MONTH, TimeUnit.DAY)),
        DateSpec("on the 2nd of July", 2025, 7, 2, setOf(TimeUnit.MONTH, TimeUnit.DAY)),
        DateSpec("on 22nd May 2026", 2026, 5, 22, setOf(TimeUnit.YEAR, TimeUnit.MONTH, TimeUnit.DAY)),
        DateSpec("on the 3rd of March, 2027", 2027, 3, 3, setOf(TimeUnit.YEAR, TimeUnit.MONTH, TimeUnit.DAY)),
        DateSpec("on 09/25", 2025, 9, 25, setOf(TimeUnit.MONTH, TimeUnit.DAY)),
        DateSpec("on 04/13/26", 2026, 4, 13, setOf(TimeUnit.YEAR, TimeUnit.MONTH, TimeUnit.DAY)),
        DateSpec("on 4-13-2026", 2026, 4, 13, setOf(TimeUnit.YEAR, TimeUnit.MONTH, TimeUnit.DAY)),
        DateSpec("on 25/09/2026", 2026, 9, 25, setOf(TimeUnit.YEAR, TimeUnit.MONTH, TimeUnit.DAY), useEuropeanDate = true),
        DateSpec("on 05/08", 2025, 8, 5, setOf(TimeUnit.MONTH, TimeUnit.DAY), useEuropeanDate = true),
        DateSpec("on the 15th", 2025, 6, 15, setOf(TimeUnit.DAY)),
        DateSpec("on the twenty-first", 2025, 6, 21, setOf(TimeUnit.DAY))
    )

    private val timeSpecs = listOf(
        TimeSpec("at 9am", 9, 0, setOf(TimeUnit.HOUR, TimeUnit.MINUTE)),
        TimeSpec("at 9:00 a.m.", 9, 0, setOf(TimeUnit.HOUR, TimeUnit.MINUTE)),
        TimeSpec("at 3:15pm", 15, 15, setOf(TimeUnit.HOUR, TimeUnit.MINUTE)),
        TimeSpec("at 12pm", 12, 0, setOf(TimeUnit.HOUR, TimeUnit.MINUTE)),
        TimeSpec("at 12:30pm", 12, 30, setOf(TimeUnit.HOUR, TimeUnit.MINUTE)),
        TimeSpec("at 12am", 0, 0, setOf(TimeUnit.HOUR, TimeUnit.MINUTE)),
        TimeSpec("at 12:30am", 0, 30, setOf(TimeUnit.HOUR, TimeUnit.MINUTE)),
        TimeSpec("at 11:59pm", 23, 59, setOf(TimeUnit.HOUR, TimeUnit.MINUTE)),
        TimeSpec("at 00:30", 0, 30, setOf(TimeUnit.HOUR, TimeUnit.MINUTE), use24 = true),
        TimeSpec("at 14:45", 14, 45, setOf(TimeUnit.HOUR, TimeUnit.MINUTE), use24 = true),
        TimeSpec("in the morning", 9, 0, setOf(TimeUnit.HOUR)),
        TimeSpec("in the afternoon", 15, 0, setOf(TimeUnit.HOUR)),
        TimeSpec("in the evening", 18, 0, setOf(TimeUnit.HOUR)),
        TimeSpec("at night", 20, 0, setOf(TimeUnit.HOUR)),
        TimeSpec("at noon", 12, 0, setOf(TimeUnit.HOUR)),
        TimeSpec("at midday", 12, 0, setOf(TimeUnit.HOUR)),
        TimeSpec("at 10", 10, 0, setOf(TimeUnit.HOUR, TimeUnit.MINUTE)),
        TimeSpec("at 4", 16, 0, setOf(TimeUnit.HOUR, TimeUnit.MINUTE)) // anchor is 08:00, so 4 -> 16:00
    )

    @Test
    fun testMassDateAndTimeCombinations() {
        val failures = mutableListOf<String>()
        var totalCases = 0

        for (date in dateSpecs) {
            for (time in timeSpecs) {
                val config = ENConfig(
                    use24 = time.use24,
                    useEuropeanDate = date.useEuropeanDate,
                    relativeTo = anchorMillis,
                    timeZone = zone
                )
                val parser = TimeParser(config)

                // Test both "{Date} {Time}" and "{Time} {Date}"
                for (sentence in listOf(
                    "We have a meeting ${date.phrase} ${time.phrase}",
                    "We have a meeting ${time.phrase} ${date.phrase}"
                )) {
                    totalCases++
                    val parsed = try {
                        parser.parse(sentence)
                    } catch (e: Exception) {
                        failures += "CRASH on '$sentence': ${e::class.simpleName}: ${e.message}"
                        continue
                    }

                    if (parsed.size != 1) {
                        failures += "SIZE MISMATCH on '$sentence': expected 1 Parsed, got ${parsed.size} (${parsed.map { it.text }})"
                        continue
                    }

                    val item = parsed.first()
                    val actual = item.startTime.first()
                    val expected = LocalDateTime(
                        date.expectedYear,
                        date.expectedMonth,
                        date.expectedDay,
                        time.expectedHour,
                        time.expectedMinute,
                        0
                    )

                    if (actual != expected) {
                        failures += "TIME MISMATCH on '$sentence': expected $expected, got $actual"
                    }

                    val expectedTags = date.expectedTags + time.expectedTags
                    if (item.tagsTimeStart != expectedTags) {
                        failures += "TAGS MISMATCH on '$sentence': expected $expectedTags, got ${item.tagsTimeStart}"
                    }
                }
            }
        }

        assertTrue(
            failures.isEmpty(),
            "Failed ${failures.size} / $totalCases Date×Time grammar combinations:\n" +
                failures.take(30).joinToString("\n")
        )
    }

    @Test
    fun testMassRelativeOffsetsAndPartials() {
        val failures = mutableListOf<String>()
        var totalCases = 0
        val parser = TimeParser(ENConfig(relativeTo = anchorMillis, timeZone = zone))

        val prepositions = listOf("in", "after", "starting in")
        val numberSpec = listOf(
            "1" to 1,
            "2" to 2,
            "5" to 5,
            "10" to 10,
            "a" to 1,
            "one" to 1,
            "two" to 2,
            "three" to 3,
            "four" to 4,
            "five" to 5,
            "six" to 6,
            "seven" to 7,
            "eight" to 8,
            "nine" to 9,
            "ten" to 10
        )
        val unitSpec = listOf(
            Triple("second", "seconds", TimeUnit.SECOND),
            Triple("sec", "secs", TimeUnit.SECOND),
            Triple("minute", "minutes", TimeUnit.MINUTE),
            Triple("min", "mins", TimeUnit.MINUTE),
            Triple("hour", "hours", TimeUnit.HOUR),
            Triple("hr", "hrs", TimeUnit.HOUR),
            Triple("day", "days", TimeUnit.DAY),
            Triple("week", "weeks", TimeUnit.WEEK),
            Triple("wk", "wks", TimeUnit.WEEK),
            Triple("month", "months", TimeUnit.MONTH),
            Triple("mo", "mos", TimeUnit.MONTH),
            Triple("year", "years", TimeUnit.YEAR),
            Triple("yr", "yrs", TimeUnit.YEAR)
        )

        for (prep in prepositions) {
            for ((numWord, numVal) in numberSpec) {
                for ((singular, plural, unit) in unitSpec) {
                    val unitWord = if (numVal == 1) singular else plural
                    // Skip "a secs" / "a hours" (use "an hour" separately)
                    if (numWord == "a" && unitWord.startsWith("h")) continue

                    val sentence = "The alarm will trigger $prep $numWord $unitWord"
                    totalCases++

                    val parsed = try {
                        parser.parse(sentence)
                    } catch (e: Exception) {
                        failures += "CRASH on '$sentence': ${e::class.simpleName}: ${e.message}"
                        continue
                    }

                    if (parsed.size != 1) {
                        failures += "SIZE MISMATCH on '$sentence': expected 1, got ${parsed.size} (${parsed.map { it.text }})"
                        continue
                    }

                    val expectedTime = anchorDateTime.toInstant(zone).plus(
                        DateTimePeriod(
                            seconds = if (unit == TimeUnit.SECOND) numVal else 0,
                            minutes = if (unit == TimeUnit.MINUTE) numVal else 0,
                            hours = if (unit == TimeUnit.HOUR) numVal else 0,
                            days = when (unit) {
                                TimeUnit.DAY -> numVal
                                TimeUnit.WEEK -> numVal * 7
                                else -> 0
                            },
                            months = if (unit == TimeUnit.MONTH) numVal else 0,
                            years = if (unit == TimeUnit.YEAR) numVal else 0
                        ),
                        zone
                    ).toLocalDateTime(zone)

                    val actualTime = parsed[0].startTime.first()
                    if (actualTime != expectedTime) {
                        failures += "OFFSET MISMATCH on '$sentence': expected $expectedTime, got $actualTime"
                    }
                }
            }
        }

        // Fractional / partial durations
        val partialCases = listOf(
            "in half a minute" to DateTimePeriod(seconds = 30),
            "in a quarter of a minute" to DateTimePeriod(seconds = 15),
            "in half an hour" to DateTimePeriod(minutes = 30),
            "in a quarter of an hour" to DateTimePeriod(minutes = 15),
            "in half a day" to DateTimePeriod(hours = 12),
            "in a quarter of a day" to DateTimePeriod(hours = 6),
            "in half a week" to DateTimePeriod(hours = 84),
            "in a quarter of a week" to DateTimePeriod(hours = 42)
        )

        for ((phrase, period) in partialCases) {
            totalCases++
            val sentence = "See you $phrase"
            val parsed = parser.parse(sentence)
            if (parsed.size != 1) {
                failures += "PARTIAL SIZE MISMATCH on '$sentence': got ${parsed.size}"
                continue
            }
            val expected = anchorDateTime.toInstant(zone).plus(period, zone).toLocalDateTime(zone)
            val actual = parsed[0].startTime.first()
            if (actual != expected) {
                failures += "PARTIAL MATH MISMATCH on '$sentence': expected $expected, got $actual"
            }
        }

        assertTrue(
            failures.isEmpty(),
            "Failed ${failures.size} / $totalCases Relative Offset grammar combinations:\n" +
                failures.take(30).joinToString("\n")
        )
    }

    @Test
    fun testMassRangesAndRecurrence() {
        val failures = mutableListOf<String>()
        var totalCases = 0
        val parser = TimeParser(ENConfig(use24 = true, relativeTo = anchorMillis, timeZone = zone))

        val startHours = listOf("8am" to 8, "10am" to 10, "1pm" to 13)
        val endConnectors = listOf("to", "until", "till", "-")
        val endHours = listOf("4pm" to 16, "6pm" to 18, "9pm" to 21)
        val recurrences = listOf(
            "every day" to (TimeUnit.DAY to 1),
            "every other day" to (TimeUnit.DAY to 2),
            "every week" to (TimeUnit.WEEK to 1),
            "every other week" to (TimeUnit.WEEK to 2),
            "every 3 days" to (TimeUnit.DAY to 3),
            "daily" to (TimeUnit.DAY to 1),
            "weekly" to (TimeUnit.WEEK to 1),
            "biweekly" to (TimeUnit.WEEK to 2),
            "monthly" to (TimeUnit.MONTH to 1),
            "yearly" to (TimeUnit.YEAR to 1)
        )

        for ((startStr, startHr) in startHours) {
            for (conn in endConnectors) {
                for ((endStr, endHr) in endHours) {
                    for ((recStr, recExpected) in recurrences) {
                        val sentence = "Work shift from $startStr $conn $endStr $recStr"
                        totalCases++

                        val parsed = try {
                            parser.parse(sentence)
                        } catch (e: Exception) {
                            failures += "CRASH on '$sentence': ${e::class.simpleName}: ${e.message}"
                            continue
                        }

                        if (parsed.size != 1) {
                            failures += "SIZE MISMATCH on '$sentence': expected 1, got ${parsed.size} (${parsed.map { it.text }})"
                            continue
                        }

                        val item = parsed[0]
                        if (item.startTime.first().hour != startHr) {
                            failures += "START HOUR MISMATCH on '$sentence': expected $startHr, got ${item.startTime.first().hour}"
                        }
                        if (item.endTime?.hour != endHr) {
                            failures += "END HOUR MISMATCH on '$sentence': expected $endHr, got ${item.endTime?.hour}"
                        }
                        if (item.repeatTag != recExpected.first || item.repeatOften != recExpected.second) {
                            failures += "REPEAT MISMATCH on '$sentence': expected $recExpected, got (${item.repeatTag}, ${item.repeatOften})"
                        }
                    }
                }
            }
        }

        assertTrue(
            failures.isEmpty(),
            "Failed ${failures.size} / $totalCases Range×Recurrence combinations:\n" +
                failures.take(30).joinToString("\n")
        )
    }

    @Test
    fun testMassMultiExpressionSentences() {
        val failures = mutableListOf<String>()
        var totalCases = 0
        val parser = TimeParser(ENConfig(use24 = true, relativeTo = anchorMillis, timeZone = zone))

        // Expressions with varying tag counts / points (1 tag vs 2 tags vs 3 tags)
        val expressions = listOf(
            "today",
            "tomorrow",
            "yesterday",
            "on Monday",
            "in the morning",
            "at 3:30pm",
            "on July 4th",
            "on December 25th, 2025",
            "on 04/13/2026",
            "on the 1st of Feb 2026",
            "tomorrow at 9am",
            "from 8am to 4pm",
            "in 3 hours",
            "every Friday"
        )

        for (i in expressions.indices) {
            for (j in expressions.indices) {
                if (i == j) continue
                val first = expressions[i]
                val second = expressions[j]
                for (sentence in listOf(
                    "Primary schedule is $first, and the backup option is $second.",
                    "We have a meeting $first and a follow-up call $second."
                )) {
                    totalCases++

                    val parsed = try {
                        parser.parse(sentence)
                    } catch (e: Exception) {
                        failures += "CRASH on '$sentence': ${e::class.simpleName}: ${e.message}"
                        continue
                    }

                    if (parsed.size != 2) {
                        failures += "MULTI-EXPR COUNT MISMATCH on '$sentence': expected 2 expressions ('$first' and '$second'), got ${parsed.size}: ${parsed.map { "'${it.text}'" }}"
                        continue
                    }

                    if (!parsed[0].text.trim().equals(first, ignoreCase = true)) {
                        failures += "FIRST EXPR MISMATCH on '$sentence': expected '$first', got '${parsed[0].text.trim()}'"
                    }
                    if (!parsed[1].text.trim().equals(second, ignoreCase = true)) {
                        failures += "SECOND EXPR MISMATCH on '$sentence': expected '$second', got '${parsed[1].text.trim()}'"
                    }
                }
            }
        }

        assertTrue(
            failures.isEmpty(),
            "Failed ${failures.size} / $totalCases Multi-Expression combinations:\n" +
                failures.take(30).joinToString("\n")
        )
    }

    @Test
    fun testMassDistractorNumbersAndArticlesSurroundingExpressions() {
        val failures = mutableListOf<String>()
        var totalCases = 0
        val parser = TimeParser(ENConfig(use24 = true, relativeTo = anchorMillis, timeZone = zone))

        val expressions = listOf(
            "today",
            "tomorrow at 3:15pm",
            "on Monday",
            "on July 4th",
            "on the 2nd of July",
            "on 09/25 at 10:30am",
            "from 8am to 4pm",
            "in 5 minutes",
            "in 2 hours",
            "in a week",
            "for 3 hours",
            "for 2 days",
            "every Friday at 9am"
        )

        // Contexts packed with distractor 'a', 'an', bare numbers, and preposition+distractor traps
        val distractorFrames = listOf(
            "We are in a meeting " to " with 2 people",
            "Going to a movie " to " and a dinner after",
            "Waiting for a friend " to " in room 401",
            "Table for 2 " to " for a birthday party",
            "There are 3 groups in 2 rooms " to " and a guest",
            "The 5 dogs and the next 2 people arrive " to " at a park",
            "After a long talk, " to " a bell rings",
            "In a rush, " to " when 3 people arrived",
            "With 40 guests, " to " while 50 people waited",
            "Book 1 ticket " to ", a seat is open",
            "Buy 4 items " to " and a gift"
        )

        for (expr in expressions) {
            val baseline = parser.parse(expr)
            if (baseline.size != 1) {
                failures += "BASELINE FAILED on '$expr': got ${baseline.size}"
                continue
            }
            val expected = baseline.first()

            for ((prefix, suffix) in distractorFrames) {
                val sentence = "$prefix$expr$suffix"
                totalCases++

                val parsed = try {
                    parser.parse(sentence)
                } catch (e: Exception) {
                    failures += "CRASH on '$sentence': ${e::class.simpleName}: ${e.message}"
                    continue
                }

                if (parsed.size != 1) {
                    failures += "DISTRACTOR COUNT MISMATCH on '$sentence': expected only ['$expr'], got ${parsed.size}: ${parsed.map { "'${it.text}'" }}"
                    continue
                }

                val actual = parsed.first()
                if (!actual.text.trim().equals(expr, ignoreCase = true)) {
                    failures += "DISTRACTOR TEXT CORRUPTION on '$sentence': expected '$expr', got '${actual.text.trim()}'"
                }
                if (actual.startTime != expected.startTime) {
                    failures += "DISTRACTOR START_TIME CORRUPTION on '$sentence': expected ${expected.startTime}, got ${actual.startTime}"
                }
                if (actual.endTime != expected.endTime) {
                    failures += "DISTRACTOR END_TIME CORRUPTION on '$sentence': expected ${expected.endTime}, got ${actual.endTime}"
                }
            }
        }

        assertTrue(
            failures.isEmpty(),
            "Failed ${failures.size} / $totalCases Distractor Number & Article cases:\n" +
                failures.take(30).joinToString("\n")
        )
    }
}
