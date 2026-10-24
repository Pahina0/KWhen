import ap.panini.kwhen.DayOfWeek
import ap.panini.kwhen.TimeParser
import ap.panini.kwhen.TimeUnit
import ap.panini.kwhen.configs.ENConfig
import ap.panini.kwhen.util.copy
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
class ENTests {
    private lateinit var timeParser: TimeParserTest
    private lateinit var parserFinal: TimeParser

    private val config = ENConfig()
    private val dateTime = config.getDateTime()

    @BeforeTest
    fun setup() {
        timeParser = TimeParserTest()
        parserFinal = TimeParser()
    }


    @Test
    fun testBasicDate() {
        timeParser.parse("today i will go swim").let {
            assertEquals("today", it[0].text)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
        }

        timeParser.parse("it will be a great day tmrw").let {
            assertEquals("tmrw", it[0].text)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run { copy(date.plus(1, DateTimeUnit.DAY)) },
                it[0].startTime
            )
        }



        timeParser.parse("it is like yesterday's weather today").let {
            assertEquals("yesterday", it[0].text)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run { copy(date.minus(1, DateTimeUnit.DAY)) },
                it[0].startTime
            )
        }
    }

    @Test
    fun testBasicTime() {
        timeParser.parse("i will swim in the afternoon").let {
            assertEquals("afternoon", it[0].text)
            assertEquals(setOf(TimeUnit.HOUR), it[0].tagsTimeStart)
            assertEquals(dateTime.startTime.run {
                copy(
                    hour = 15
                )
            }, it[0].startTime)
        }

        timeParser.parse("the morning was nice and cool").let {
            assertEquals("morning", it[0].text)
            assertEquals(setOf(TimeUnit.HOUR), it[0].tagsTimeStart)
            assertEquals(dateTime.startTime.run {
                copy(
                    hour = 9
                )
            }, it[0].startTime)
        }

        timeParser.parse("tonight was kind of boring").let {
            assertEquals("tonight", it[0].text)
            assertEquals(setOf(TimeUnit.HOUR, TimeUnit.DAY), it[0].tagsTimeStart)
            assertEquals(dateTime.startTime.run {
                copy(
                    hour = 20
                )
            }, it[0].startTime)
        }

        TimeParserTest(ENConfig(afternoon = 14)).parse("i will go run in the afternoon today").let {
            assertEquals("afternoon", it[0].text)
            assertEquals(setOf(TimeUnit.HOUR, TimeUnit.HOUR), it[0].tagsTimeStart)
            assertEquals(dateTime.startTime.run {
                copy(
                    hour = 14
                )
            }, it[0].startTime)
        }
    }

    @Test
    fun testDayMonthYear() {
        timeParser.parse("12 june 07 was an extremely hot day").let {
            assertEquals("12 june 07", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH, TimeUnit.YEAR), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run {
                    copy(
                        year = 2007,
                        monthNumber = 6,
                        dayOfMonth = 12
                    )
                },
                it[0].startTime
            )
        }

        timeParser.parse("it will be a blast on the 4th of july").let {
            assertEquals("4th of july", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run { copy(monthNumber = 7, dayOfMonth = 4) },
                it[0].startTime
            )
        }

        timeParser.parse("5 aug. 1448 stuff happened").let {
            assertEquals("5 aug. 1448", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH, TimeUnit.YEAR), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run {
                    copy(
                        year = 1448,
                        monthNumber = 8,
                        dayOfMonth = 5
                    )
                },
                it[0].startTime
            )
        }

        timeParser.parse("18 sep 76 there was (not) a battle").let {
            assertEquals("18 sep 76", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH, TimeUnit.YEAR), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run {
                    copy(
                        year = 1976,
                        monthNumber = 9,
                        dayOfMonth = 18
                    )
                },
                it[0].startTime
            )
        }

        timeParser.parse("a bird crashed on the 1st of    feb").let {
            assertEquals("1st of    feb", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run { copy(monthNumber = 2, dayOfMonth = 1) },
                it[0].startTime
            )
        }

        timeParser.parse("the event is on the 2nd of july").let {
            assertEquals("2nd of july", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run { copy(monthNumber = 7, dayOfMonth = 2) },
                it[0].startTime
            )
        }

        timeParser.parse("we graduated on 22nd may 2024").let {
            assertEquals("22nd may 2024", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH, TimeUnit.YEAR), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run { copy(year = 2024, monthNumber = 5, dayOfMonth = 22) },
                it[0].startTime
            )
        }
    }

    @Test
    fun testMonthDayYear() {
        timeParser.parse(" june  12 07 was an extremely hot day").let {
            assertEquals("june  12 07", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH, TimeUnit.YEAR), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run {
                    copy(
                        year = 2007,
                        monthNumber = 6,
                        dayOfMonth = 12
                    )
                },
                it[0].startTime
            )
        }

        timeParser.parse("it will be a blast on the jul 4th ").let {
            assertEquals("jul 4th", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run { copy(monthNumber = 7, dayOfMonth = 4) },
                it[0].startTime
            )
        }

        timeParser.parse("aug. 5 1448 stuff happened").let {
            assertEquals("aug. 5 1448", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH, TimeUnit.YEAR), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run {
                    copy(
                        year = 1448,
                        monthNumber = 8,
                        dayOfMonth = 5
                    )
                },
                it[0].startTime
            )
        }

        timeParser.parse(" sep-18 76 there was (not) a battle").let {
            assertEquals("sep-18 76", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH, TimeUnit.YEAR), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run {
                    copy(
                        year = 1976,
                        monthNumber = 9,
                        dayOfMonth = 18
                    )
                },
                it[0].startTime
            )
        }

        timeParser.parse("a bird crashed on the feb/1").let {
            assertEquals("feb/1", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run { copy(monthNumber = 2, dayOfMonth = 1) },
                it[0].startTime
            )
        }

        timeParser.parse("04/13/05").let {
            assertEquals("04/13/05", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH, TimeUnit.YEAR), it[0].tagsTimeStart)
            assertEquals(dateTime.startTime.run {
                copy(
                    year = 2005,
                    monthNumber = 4, dayOfMonth = 13
                )
            }, it[0].startTime)
        }

        timeParser.parse("4-13-05").let {
            assertEquals("4-13-05", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH, TimeUnit.YEAR), it[0].tagsTimeStart)
            assertEquals(dateTime.startTime.run {
                copy(
                    year = 2005,
                    monthNumber = 4, dayOfMonth = 13
                )
            }, it[0].startTime)
        }

        timeParser.parse("i'm going to leave in december").let {
            assertEquals("december", it[0].text)
            assertEquals(setOf(TimeUnit.MONTH), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run { copy(monthNumber = 12) },
                it[0].startTime
            )
        }

    }

    @Test
    fun testNumericOrdinal() {
        timeParser.parse("im going to swim at 3am").let {
            assertEquals("3am", it[0].text)
            assertEquals(setOf(TimeUnit.HOUR, TimeUnit.MINUTE), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run { copy(hour = 3, minute = 0) },
                it[0].startTime
            )
        }

        timeParser.parse("im going to swim at 03:25pm").let {
            assertEquals("03:25pm", it[0].text)
            assertEquals(setOf(TimeUnit.HOUR, TimeUnit.MINUTE), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run { copy(hour = 15, minute = 25) },
                it[0].startTime
            )

        }


        TimeParserTest(ENConfig(use24 = true)).parse("the fourth will be crazy").let {
            assertEquals("fourth", it[0].text)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run { copy(dayOfMonth = 4) },
                it[0].startTime
            )

        }
    }

    @Test
    fun testDayOfWeek() {
        TimeParserTest(ENConfig(use24 = true)).parse("i do it every mon, tues, and friday").let {
            assertEquals("mon", it[0].text)
            assertEquals("tues", it[1].text)
            assertEquals("friday", it[2].text)
            assertEquals(setOf(TimeUnit.WEEK), it[0].tagsTimeStart)
            assertEquals(setOf(DayOfWeek.MONDAY), it[0].tagsDayOfWeek)
            assertEquals(setOf(DayOfWeek.TUESDAY), it[1].tagsDayOfWeek)
            assertEquals(setOf(DayOfWeek.FRIDAY), it[2].tagsDayOfWeek)

        }
    }


    @Test
    fun testFillerEndMerge() {
        timeParser.parseAndMerge("i will go party from the 4th to 18th").let {
            assertEquals("from the 4th to 18th", it[0].text.trim())
            assertEquals(
                dateTime.startTime.run { copy(dayOfMonth = 4) },
                it[0].startTime
            )
            assertEquals(
                dateTime.startTime.run { copy(dayOfMonth = 18) },
                it[0].endTime
            )
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeEnd)
        }

        timeParser.parseAndMerge("i have nothing to do today till tmrw").let {
            assertEquals("today till tmrw", it[0].text.trim())
            assertEquals(dateTime.startTime, it[0].startTime)
            assertEquals(
                dateTime.startTime.run { copy(date.plus(1, DateTimeUnit.DAY)) },
                it[0].endTime
            )
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeEnd)
        }

        timeParser.parseAndMerge("in the morning i will go eat until the night").let {
            assertEquals("in the morning", it[0].text.trim())
            assertEquals(dateTime.startTime.copy(hour = 9), it[0].startTime)
            assertEquals(setOf(TimeUnit.HOUR), it[0].tagsTimeStart)

            assertEquals("until the night", it[1].text.trim())
            assertEquals(dateTime.startTime.copy(hour = 20), it[1].endTime)
            assertEquals(setOf(TimeUnit.HOUR), it[1].tagsTimeEnd)
        }
    }

    @Test
    fun testGenericAndMerge() {
        TimeParserTest(config = ENConfig(use24 = true)).parseAndMerge("im busy from 4 to 6").let {
            assertEquals("from 4 to 6", it[0].text.trim())
            assertEquals(
                dateTime.startTime.run { copy(hour = 4, minute = 0) },
                it[0].startTime
            )
            assertEquals(
                dateTime.startTime.run { copy(hour = 6, minute = 0) },
                it[0].endTime
            )
            assertEquals(setOf(TimeUnit.MINUTE, TimeUnit.HOUR), it[0].tagsTimeStart)
            assertEquals(setOf(TimeUnit.MINUTE, TimeUnit.HOUR), it[0].tagsTimeEnd)
        }

        timeParser.parseAndMerge("the diving team will go on a field trip in 18 months").let {
            assertEquals("in 18 months", it[0].text.trim())
            assertEquals(
                ap.panini.kwhen.util.getDateTimeWithGeneral(18.0, TimeUnit.MONTH, dateTime.startTime, config),
                it[0].startTime
            )
            assertEquals(setOf(TimeUnit.MONTH), it[0].tagsTimeStart)
        }

        timeParser.parseAndMerge("im going to japan 06/18 - dec 2025").let {
            assertEquals("06/18 - dec 2025", it[0].text.trim())
            assertEquals(
                dateTime.startTime.run { copy(monthNumber = 6, dayOfMonth = 18) },
                it[0].startTime
            )
            assertEquals(
                dateTime.startTime.run { copy(year = 2025, monthNumber = 12) },
                it[0].endTime
            )
            assertEquals(setOf(TimeUnit.MONTH, TimeUnit.DAY), it[0].tagsTimeStart)
            assertEquals(setOf(TimeUnit.MONTH, TimeUnit.YEAR), it[0].tagsTimeEnd)
        }

    }

    @Test
    fun testRepeatMerge() {


        timeParser.parseAndMerge("i go to school every day").let {
            assertEquals("every day", it[0].text.trim())
            assertEquals(1.0, it[0].repeatOften)
            assertEquals(TimeUnit.DAY, it[0].repeatTag)
        }

        timeParser.parseAndMerge("i go to school every 4 days").let {
            assertEquals("every 4 days", it[0].text.trim())
            assertEquals(4.0, it[0].repeatOften)
            assertEquals(TimeUnit.DAY, it[0].repeatTag)
        }

        timeParser.parseAndMerge("i go to school every other week").let {
            assertEquals("every other week", it[0].text.trim())
            assertEquals(2.0, it[0].repeatOften)
            assertEquals(TimeUnit.WEEK, it[0].repeatTag)
        }

        timeParser.parseAndMerge("there is something special every 4 months from july 8th").let {
            assertEquals("every 4 months from july 8th", it[0].text.trim())
            assertEquals(4.0, it[0].repeatOften)
            assertEquals(TimeUnit.MONTH, it[0].repeatTag)
            assertEquals(
                dateTime.startTime.run { copy(monthNumber = 7, dayOfMonth = 8) },
                it[0].startTime
            )
            assertEquals(setOf(TimeUnit.MONTH, TimeUnit.DAY), it[0].tagsTimeStart)

        }
    }


    @Test
    fun testProcessList() {
        parserFinal.parse("i will go swim on every other 9th  10th, 12th").let {
            assertEquals("on every other 9th  10th, 12th", it[0].text.trim())
            assertEquals(3, it[0].startTime.size)
            assertEquals(TimeUnit.MONTH, it[0].repeatTag)
            assertEquals(2, it[0].repeatOften)
        }

        parserFinal.parse("i swim every mon, tues and fri").let {
            assertEquals("every mon, tues and fri", it[0].text.trim())
            assertEquals(3, it[0].startTime.size)
            assertEquals(setOf(TimeUnit.WEEK), it[0].tagsTimeStart)
            assertEquals(TimeUnit.WEEK, it[0].repeatTag)
            assertEquals(1, it[0].repeatOften)
        }

        parserFinal.parse("the world is boring every june, jul, and aug").let {
            assertEquals("every june, jul, and aug", it[0].text.trim())
            assertEquals(3, it[0].startTime.size)
            assertEquals(setOf(TimeUnit.MONTH), it[0].tagsTimeStart)
            assertEquals(TimeUnit.YEAR, it[0].repeatTag)
            assertEquals(1, it[0].repeatOften)
        }

        parserFinal.parse("the 4th, 5th 6th, 19th, and 20th and may").let {
            assertEquals(2, it.size)
            assertEquals(5, it[0].startTime.size)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
        }
    }

    @Test
    fun testProcessSentence() {
        TimeParser(ENConfig(use24 = true)).parse("Go to Vermont 30 minutes after 6").let {
            assertEquals("30 minutes after 6", it[0].text.trim())
            assertEquals(
                dateTime.startTime.run { copy(hour = 6, minute = 30) },
                it[0].startTime.first()
            )
        }
        parserFinal.parse("Jul 9 is going to be crazy").let {
            assertEquals(1, it.size)
            assertEquals("Jul 9", it[0].text.trim())
            assertEquals(1, it[0].startTime.size)
            assertEquals(
                dateTime.startTime.run { copy(monthNumber = 7, dayOfMonth = 9) },
                it[0].startTime.first()
            )
            assertEquals(setOf(TimeUnit.MONTH, TimeUnit.DAY), it[0].tagsTimeStart)
        }

        parserFinal.parse("Today I will go swim").let {
            assertEquals("Today", it[0].text.trim())
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
        }

        parserFinal.parse("At 9 there is special food").let {
            assertEquals("At 9", it[0].text.trim())
            assertContains(it[0].tagsTimeStart, TimeUnit.HOUR)
            assertContains(it[0].tagsTimeStart, TimeUnit.MINUTE)
        }


        parserFinal.parse("There is an event in 24 hrs").let {
            assertEquals("in 24 hrs", it[0].text.trim())
            assertEquals(setOf(TimeUnit.HOUR), it[0].tagsTimeStart)
        }

        parserFinal.parse("on feb 31st 2025").let {
            assertEquals("on feb 31st 2025", it[0].text)
            assertEquals(setOf(TimeUnit.MONTH, TimeUnit.DAY, TimeUnit.YEAR), it[0].tagsTimeStart)
            assertEquals(3, it[0].startTime.first().dayOfMonth)
            assertEquals(3, it[0].startTime.first().monthNumber)
            assertEquals(2025, it[0].startTime.first().year)
        }

        parserFinal.parse("Im going to swim at 9:18").let {
            assertEquals("at 9:18", it[0].text.trim())
            assertContains(it[0].tagsTimeStart, TimeUnit.HOUR)
            assertContains(it[0].tagsTimeStart, TimeUnit.MINUTE)
            assertEquals(18, it[0].startTime.first().minute)
        }
    }

    @Test
    fun testTimeFullWithRepeat() {
        parserFinal.parse("he has school from 4am to 8pm every day").let {
            assertEquals("from 4am to 8pm every day", it[0].text.trim())
            assertEquals(setOf(TimeUnit.HOUR, TimeUnit.MINUTE), it[0].tagsTimeStart)
            assertEquals(setOf(TimeUnit.HOUR, TimeUnit.MINUTE), it[0].tagsTimeEnd)
            assertEquals(1, it[0].repeatOften)
            assertEquals(TimeUnit.DAY, it[0].repeatTag)
        }

        parserFinal.parse("I have a meeting that recurs every other day").let {
            assertEquals("that recurs every other day", it[0].text.trim())
            assertEquals(2, it[0].repeatOften)
            assertEquals(TimeUnit.DAY, it[0].repeatTag)
        }

        parserFinal.parse("there is a big party recurring every month").let {
            assertEquals("recurring every month", it[0].text.trim())
            assertEquals(1, it[0].repeatOften)
            assertEquals(TimeUnit.MONTH, it[0].repeatTag)
        }
    }

    @Test
    fun testRelativeTimes() {
        parserFinal.parse("i will sleep 30 min from 9am").let {
            assertEquals("30 min from 9am", it.first().text)
            assertEquals(setOf(TimeUnit.HOUR, TimeUnit.MINUTE), it[0].tagsTimeStart)
            assertEquals(9, it[0].startTime.first().hour)
            assertEquals(30, it[0].startTime.first().minute)
        }

        parserFinal.parse("there is a big party 2 days from jul 31st").let {
            assertEquals("2 days from jul 31st", it.first().text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH), it[0].tagsTimeStart)
            assertEquals(2, it[0].startTime.first().dayOfMonth)
            assertEquals(8, it[0].startTime.first().monthNumber)
        }
    }

    @Test
    fun testPartialTime() {
        parserFinal.parse("I gotta leave in half an hour!").also {
            assertEquals("in half an hour", it.first().text.trim())
            assertEquals(setOf(TimeUnit.HOUR), it[0].tagsTimeStart)
        }

        parserFinal.parse("There is a huge party every quarter year").also {
            assertEquals("every quarter year", it.first().text.trim())
            assertEquals(TimeUnit.MONTH, it[0].repeatTag)
            assertEquals(3, it[0].repeatOften)
        }


        parserFinal.parse("a bit of time passes every half minute.").also {
            assertEquals("every half minute", it.first().text.trim())
            assertEquals(TimeUnit.SECOND, it[0].repeatTag)
            assertEquals(30, it[0].repeatOften)
        }

        val fixedAnchor = LocalDateTime(2025, 6, 11, 10, 0, 0)
            .toInstant(TimeZone.UTC)
            .toEpochMilliseconds()
        val fixedParser = TimeParser(ENConfig(relativeTo = fixedAnchor, timeZone = TimeZone.UTC))

        fixedParser.parse("I will return in half a day").also {
            assertEquals(1, it.size)
            assertEquals(
                LocalDateTime(2025, 6, 11, 22, 0, 0),
                it[0].startTime.first(),
                "'in half a day' should add 12 hours, not 30 hours"
            )
        }

        fixedParser.parse("I will return in a quarter of a day").also {
            assertEquals(1, it.size)
            assertEquals(
                LocalDateTime(2025, 6, 11, 16, 0, 0),
                it[0].startTime.first(),
                "'in a quarter of a day' should add 6 hours, not 15 hours"
            )
        }

        fixedParser.parse("I will return in half a week").also {
            assertEquals(1, it.size)
            assertEquals(
                LocalDateTime(2025, 6, 14, 22, 0, 0),
                it[0].startTime.first(),
                "'in half a week' should add 84 hours (3.5 days), not 210 hours"
            )
        }
    }

    @Test
    fun testRepeatingGeneral() {
        timeParser.parse("Have a meeting daily.").let {
            assertEquals("daily", it[0].text)
            assertEquals(1.0, it[0].repeatOften)
            assertEquals(TimeUnit.DAY, it[0].repeatTag)
        }

        timeParser.parse("Have a meeting everyday").let {
            assertEquals("everyday", it[0].text)
            assertEquals(1.0, it[0].repeatOften)
            assertEquals(TimeUnit.DAY, it[0].repeatTag)
        }

        timeParser.parse("Have a meeting biweekly").let {
            assertEquals("biweekly", it[0].text)
            assertEquals(2.0, it[0].repeatOften)
            assertEquals(TimeUnit.WEEK, it[0].repeatTag)
        }

        parserFinal.parse("Have a meeting bi-monthly").let {
            assertEquals("bi-monthly", it[0].text)
            assertEquals(2, it[0].repeatOften)
            assertEquals(TimeUnit.MONTH, it[0].repeatTag)
        }

    }

    @Test
    fun testRangeDuration() {
        parserFinal.parse("go swim every 8pm for 3 days from the third of jun").let {
            assertEquals("every 8pm for 3 days from the third of jun", it[0].text.trim())
            assertEquals(1, it[0].repeatOften)
            assertEquals(TimeUnit.DAY, it[0].repeatTag)
            it[0].startTime[0].let { time ->
                assertEquals(20, time.hour)
                assertEquals(6, time.monthNumber)
                assertEquals(3, time.dayOfMonth)
            }

            it[0].endTime!!.let { time ->
                assertEquals(6, time.monthNumber)
                assertEquals(6, time.dayOfMonth)
            }
        }

        parserFinal.parse("I will go party for a month starting from 04/3").let {
            assertEquals(5, it[0].endTime!!.monthNumber)
            assertEquals(4, it[0].startTime[0].monthNumber)
        }

        parserFinal.parse("he had to go to the hospital at 3pm every other day for 2 years from june 2023")
            .let {
                assertEquals("at 3pm every other day for 2 years from june 2023", it[0].text.trim())
                assertEquals(2, it[0].repeatOften)
                assertEquals(TimeUnit.DAY, it[0].repeatTag)
                it[0].startTime[0].let { time ->
                    assertEquals(15, time.hour)
                    assertEquals(6, time.monthNumber)
                    assertEquals(2023, time.year)
                }

                it[0].endTime!!.let { time ->
                    assertEquals(6, time.monthNumber)
                    assertEquals(2025, time.year)
                }
            }
    }

    @Test
    fun testNext() {
        parserFinal.parse("I have to go do something next week").let {
            assertEquals("next week", it.first().text.trim())
            assertEquals(setOf(TimeUnit.WEEK), it[0].tagsTimeStart)
            assertEquals(
                listOf(dateTime.startTime.run { copy(date.plus(7, DateTimeUnit.DAY)) }),
                it[0].startTime
            )
        }

        parserFinal.parse("Next month there is a big party i have to go to!").let {
            assertEquals("Next month", it.first().text.trim())
            assertEquals(setOf(TimeUnit.MONTH), it[0].tagsTimeStart)
            assertEquals(
                listOf(dateTime.startTime.run { copy(date.plus(1, DateTimeUnit.MONTH)) }),
                it[0].startTime
            )
        }


        parserFinal.parse("the movie coming out next year will be the best").let {
            assertEquals("next year", it.first().text.trim())
            assertEquals(setOf(TimeUnit.YEAR), it[0].tagsTimeStart)
            assertEquals(
                listOf(dateTime.startTime.run { copy(date.plus(1, DateTimeUnit.YEAR)) }),
                it[0].startTime
            )
        }
    }

    @Test
    fun testInterweavingRanges() {
        // these can create multiple possible times
        // caused crash due to out of bounds of items
        parserFinal.parse("I need to go 2 jul 4th").let {
            it.first().let { parsed ->
                assertEquals("2 jul", parsed.text.trim())
                assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH), parsed.tagsTimeStart)
                parsed.startTime[0].let { time ->
                    assertEquals(7, time.monthNumber)
                    assertEquals(2, time.dayOfMonth)
                }

            }
            it[1].let { parsed ->
                assertEquals("jul 4th", parsed.text.trim())
                assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH), parsed.tagsTimeStart)
                parsed.startTime[0].let { time ->
                    assertEquals(7, time.monthNumber)
                    assertEquals(4, time.dayOfMonth)
                }

            }
        }

        // another out of bounds
        parserFinal.parse("june 9 may was truly a day to remember").let {
            it.first().let { parsed ->
                assertEquals("june 9", parsed.text.trim())
                assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH), parsed.tagsTimeStart)
                parsed.startTime[0].let { time ->
                    assertEquals(6, time.monthNumber)
                    assertEquals(9, time.dayOfMonth)
                }

            }

            it[1].let { parsed ->
                assertEquals("9 may", parsed.text.trim())
                assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH), parsed.tagsTimeStart)
                parsed.startTime[0].let { time ->
                    assertEquals(5, time.monthNumber)
                    assertEquals(9, time.dayOfMonth)
                }

            }
        }

        parserFinal.parse("18:25 9 crashed version 0.0.4").let {
            it.first().let { parsed ->
                assertEquals("18:25", parsed.text.trim())
                assertContains(parsed.tagsTimeStart, TimeUnit.HOUR)
                assertContains(parsed.tagsTimeStart, TimeUnit.MINUTE)
                parsed.startTime[0].let { time ->
                    assertEquals(18, time.hour)
                    assertEquals(25, time.minute)
                }

            }
        }

    }

    @Test
    fun testNextDayHours() {
        // Thu Mar 27 2025 16:46:48.065
        TimeParser(
            ENConfig(
                relativeTo = 1743108408065,
                timeZone = TimeZone.of("US/Eastern")
            )
        ).parse("something is happening at 5").let {
            it.first().let { parsed ->
                assertEquals("at 5", parsed.text.trim())
                assertEquals(setOf(TimeUnit.HOUR, TimeUnit.MINUTE), parsed.tagsTimeStart)
                assertEquals(17, parsed.startTime[0].hour)
                assertEquals(27, parsed.startTime[0].dayOfMonth)
            }
        }

        // Thu Mar 27 2025 16:46:48.065
        TimeParser(
            ENConfig(
                relativeTo = 1743108408065,
                timeZone = TimeZone.of("US/Eastern")
            )
        ).parse("something is happening at 4").let {
            it.first().let { parsed ->
                assertEquals("at 4", parsed.text.trim())
                assertEquals(
                    setOf(TimeUnit.HOUR, TimeUnit.MINUTE, TimeUnit.DAY),
                    parsed.tagsTimeStart
                )
                assertEquals(4, parsed.startTime[0].hour)
                assertEquals(28, parsed.startTime[0].dayOfMonth)
            }
        }

        // Thu Mar 27 2025 16:46:48.065
        TimeParser(
            ENConfig(
                relativeTo = 1743108408065,
                timeZone = TimeZone.of("US/Eastern")
            )
        ).parse("something is happening at 4:18").let {
            it.first().let { parsed ->
                assertEquals("at 4:18", parsed.text.trim())
                assertEquals(
                    setOf(TimeUnit.HOUR, TimeUnit.MINUTE, TimeUnit.DAY),
                    parsed.tagsTimeStart
                )
                assertEquals(4, parsed.startTime[0].hour)
                assertEquals(18, parsed.startTime[0].minute)
                assertEquals(28, parsed.startTime[0].dayOfMonth)
            }
        }

        // Sat Dec 31 2022 23:11:00.000
        TimeParser(
            ENConfig(
                relativeTo = 1672546260000,
                timeZone = TimeZone.of("US/Eastern")
            )
        ).parse("december is no more at 1").let {
            it[1].let { parsed ->
                assertEquals("at 1", parsed.text.trim())
                assertEquals(
                    setOf(
                        TimeUnit.HOUR,
                        TimeUnit.MINUTE,
                        TimeUnit.DAY,
                        TimeUnit.MONTH,
                        TimeUnit.YEAR
                    ), parsed.tagsTimeStart
                )
                assertEquals(1, parsed.startTime[0].hour)
                assertEquals(1, parsed.startTime[0].dayOfMonth)
                assertEquals(1, parsed.startTime[0].monthNumber)
                assertEquals(2023, parsed.startTime[0].year)
            }
        }

    }

    @Test
    fun testMultiStart() {
        TimeParser().parse("There is a big event on tues and fri at 6pm")[0].let { parsed ->
            assertEquals("on tues and fri at 6pm", parsed.text.trim())
            assertEquals(
                setOf(
                    TimeUnit.HOUR,
                    TimeUnit.MINUTE,
                    TimeUnit.WEEK,
                ), parsed.tagsTimeStart
            )
            assertEquals(18, parsed.startTime[0].hour)
            assertEquals(18, parsed.startTime[1].hour)
            assertEquals(kotlinx.datetime.DayOfWeek.TUESDAY, parsed.startTime[1].dayOfWeek)
            assertEquals(kotlinx.datetime.DayOfWeek.FRIDAY, parsed.startTime[0].dayOfWeek)
        }

        TimeParser().parse("Every monday and thurs at 9:30pm we have a big dinner!")[0].let { parsed ->
            assertEquals("Every monday and thurs at 9:30pm", parsed.text.trim())
            assertEquals(
                setOf(
                    TimeUnit.HOUR,
                    TimeUnit.MINUTE,
                    TimeUnit.WEEK,
                ), parsed.tagsTimeStart
            )
            assertEquals(21, parsed.startTime[0].hour)
            assertEquals(21, parsed.startTime[1].hour)
            assertEquals(30, parsed.startTime[0].minute)
            assertEquals(30, parsed.startTime[1].minute)
            assertEquals(1, parsed.repeatOften)
            assertEquals(TimeUnit.WEEK, parsed.repeatTag)
            assertEquals(kotlinx.datetime.DayOfWeek.THURSDAY, parsed.startTime[0].dayOfWeek)
            assertEquals(kotlinx.datetime.DayOfWeek.MONDAY, parsed.startTime[1].dayOfWeek)
        }
    }

    @Test
    fun testTextShorthands() {
        // Tonight shorthands
        parserFinal.parse("see you tn").let {
            assertEquals(1, it.size, "'tn' should parse as a time expression")
            assertEquals("tn", it[0].text.trim())
            assertEquals(config.night, it[0].startTime.first().hour)
            assertEquals(setOf(TimeUnit.HOUR, TimeUnit.DAY), it[0].tagsTimeStart)
        }

        parserFinal.parse("party tonite").let {
            assertEquals(1, it.size, "'tonite' should parse as a time expression")
            assertEquals("tonite", it[0].text.trim())
            assertEquals(config.night, it[0].startTime.first().hour)
        }

        parserFinal.parse("let's go 2nite").let {
            assertEquals(1, it.size, "'2nite' should parse as a time expression")
            assertEquals("2nite", it[0].text.trim())
            assertEquals(config.night, it[0].startTime.first().hour)
        }

        parserFinal.parse("meet me 2night").let {
            assertEquals(1, it.size, "'2night' should parse as a time expression")
            assertEquals("2night", it[0].text.trim())
            assertEquals(config.night, it[0].startTime.first().hour)
        }

        // Today shorthands
        timeParser.parse("busy tdy").let {
            assertEquals("tdy", it[0].text)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
        }

        timeParser.parse("free td").let {
            assertEquals("td", it[0].text)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
        }

        timeParser.parse("leaving 2day").let {
            assertEquals("2day", it[0].text)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
        }

        // Tomorrow shorthands
        timeParser.parse("done by 2morrow").let {
            assertEquals("2morrow", it[0].text)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run { copy(date.plus(1, DateTimeUnit.DAY)) },
                it[0].startTime
            )
        }

        timeParser.parse("see you 2moro").let {
            assertEquals("2moro", it[0].text)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run { copy(date.plus(1, DateTimeUnit.DAY)) },
                it[0].startTime
            )
        }

        timeParser.parse("call me tom").let {
            assertEquals("tom", it[0].text)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run { copy(date.plus(1, DateTimeUnit.DAY)) },
                it[0].startTime
            )
        }

        // Yesterday shorthands
        timeParser.parse("saw him yest").let {
            assertEquals("yest", it[0].text)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run { copy(date.minus(1, DateTimeUnit.DAY)) },
                it[0].startTime
            )
        }

        timeParser.parse("was there yst").let {
            assertEquals("yst", it[0].text)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run { copy(date.minus(1, DateTimeUnit.DAY)) },
                it[0].startTime
            )
        }

        timeParser.parse("happened yd").let {
            assertEquals("yd", it[0].text)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
            assertEquals(
                dateTime.startTime.run { copy(date.minus(1, DateTimeUnit.DAY)) },
                it[0].startTime
            )
        }

        // Time of day shorthands
        timeParser.parse("good morn").let {
            assertEquals("morn", it[0].text)
            assertEquals(setOf(TimeUnit.HOUR), it[0].tagsTimeStart)
            assertEquals(config.morning, it[0].startTime.hour)
        }

        timeParser.parse("late eve").let {
            assertEquals("eve", it[0].text)
            assertEquals(setOf(TimeUnit.HOUR), it[0].tagsTimeStart)
            assertEquals(config.evening, it[0].startTime.hour)
        }

        timeParser.parse("meet midnite").let {
            assertEquals("midnite", it[0].text)
            assertEquals(setOf(TimeUnit.HOUR), it[0].tagsTimeStart)
        }

        // Relative time unit abbreviations
        parserFinal.parse("in 5 mins").let {
            assertEquals(1, it.size)
            assertEquals("in 5 mins", it[0].text.trim())
            assertEquals(setOf(TimeUnit.MINUTE), it[0].tagsTimeStart)
        }

        parserFinal.parse("in 2 hrs").let {
            assertEquals(1, it.size)
            assertEquals("in 2 hrs", it[0].text.trim())
            assertEquals(setOf(TimeUnit.HOUR), it[0].tagsTimeStart)
        }

        parserFinal.parse("in 3 wks").let {
            assertEquals(1, it.size)
            assertEquals("in 3 wks", it[0].text.trim())
            assertEquals(setOf(TimeUnit.WEEK), it[0].tagsTimeStart)
        }

        parserFinal.parse("in 6 mos").let {
            assertEquals(1, it.size)
            assertEquals("in 6 mos", it[0].text.trim())
            assertEquals(setOf(TimeUnit.MONTH), it[0].tagsTimeStart)
        }

        parserFinal.parse("in 2 yrs").let {
            assertEquals(1, it.size)
            assertEquals("in 2 yrs", it[0].text.trim())
            assertEquals(setOf(TimeUnit.YEAR), it[0].tagsTimeStart)
        }
    }

    @Test
    fun testNumericTimeEdgeCases() {
        // Last minute of the day
        timeParser.parse("deadline is 11:59pm").let {
            assertEquals("11:59pm", it[0].text)
            assertEquals(setOf(TimeUnit.HOUR, TimeUnit.MINUTE), it[0].tagsTimeStart)
            assertEquals(23, it[0].startTime.hour)
            assertEquals(59, it[0].startTime.minute)
        }

        // First hour of the morning
        timeParser.parse("early at 1:00am").let {
            assertEquals("1:00am", it[0].text)
            assertEquals(1, it[0].startTime.hour)
            assertEquals(0, it[0].startTime.minute)
        }

        // Uppercase PM
        timeParser.parse("meeting at 3:05 PM").let {
            assertEquals(15, it[0].startTime.hour)
            assertEquals(5, it[0].startTime.minute)
        }

        // Dot notation p.m.
        timeParser.parse("game starts 3p.m.").let {
            assertEquals(15, it[0].startTime.hour)
        }

        // Dot notation a.m.
        timeParser.parse("wake up at 7a.m.").let {
            assertEquals(7, it[0].startTime.hour)
        }

        // Space between number and am
        timeParser.parse("starts at 9 am").let {
            assertEquals(9, it[0].startTime.hour)
            assertEquals(0, it[0].startTime.minute)
        }

        // Space between number and pm
        timeParser.parse("dinner at 7 pm").let {
            assertEquals(19, it[0].startTime.hour)
            assertEquals(0, it[0].startTime.minute)
        }

        // Reject hours > 12 with pm
        parserFinal.parse("Meet me at 15pm").let {
            assertTrue(
                it.none { p -> p.text.contains("15pm") },
                "'15pm' is invalid and should not be parsed as 27:00"
            )
        }

        parserFinal.parse("Meet me at 13:30pm").let {
            assertTrue(
                it.none { p -> p.text.contains("13:30pm") },
                "'13:30pm' is invalid and should not be parsed"
            )
        }

        // Zero hour in 24-hour colon format
        TimeParser(ENConfig(use24 = true)).parse("The train leaves at 00:30").let {
            assertEquals(1, it.size, "'00:30' should be parsed as a valid 24-hour time")
            assertEquals("at 00:30", it[0].text.trim())
            assertEquals(0, it[0].startTime.first().hour)
            assertEquals(30, it[0].startTime.first().minute)
        }

        // Dotted a.m. merging with adjacent date token
        parserFinal.parse("We have a meeting at 9:00 a.m. today").let {
            assertEquals(
                1,
                it.size,
                "'at 9:00 a.m. today' should merge into 1 Parsed expression, but got: ${it.map { p -> p.text }}"
            )
        }
    }

    @Test
    fun testDateFormatEdgeCases() {
        // Start of year MM/DD
        parserFinal.parse("happy new year on 01/01").let {
            assertEquals(1, it.size)
            assertEquals(1, it[0].startTime.first().monthNumber)
            assertEquals(1, it[0].startTime.first().dayOfMonth)
        }

        // End of year MM/DD
        parserFinal.parse("celebration is 12/31").let {
            assertEquals(1, it.size)
            assertEquals(12, it[0].startTime.first().monthNumber)
            assertEquals(31, it[0].startTime.first().dayOfMonth)
        }

        // Full month name + ordinal suffix
        parserFinal.parse("the party is July 4th").let {
            assertEquals(1, it.size)
            assertEquals(7, it[0].startTime.first().monthNumber)
            assertEquals(4, it[0].startTime.first().dayOfMonth)
        }

        // Full month name + 2nd suffix
        parserFinal.parse("it is february 2nd").let {
            assertEquals(1, it.size)
            assertEquals(2, it[0].startTime.first().monthNumber)
            assertEquals(2, it[0].startTime.first().dayOfMonth)
        }

        // Day-month format: "1st of january"
        timeParser.parse("party on the 1st of january").let {
            assertEquals(1, it[0].startTime.monthNumber)
            assertEquals(1, it[0].startTime.dayOfMonth)
        }

        // Day-month format: "31st of december"
        timeParser.parse("celebrate on the 31st of december").let {
            assertEquals(12, it[0].startTime.monthNumber)
            assertEquals(31, it[0].startTime.dayOfMonth)
        }

        // "sept." with dot abbreviation
        timeParser.parse("school starts sept. 5").let {
            assertEquals(9, it[0].startTime.monthNumber)
            assertEquals(5, it[0].startTime.dayOfMonth)
        }

        // Reject invalid numeric months (> 12) in default MM/DD mode
        parserFinal.parse("Meeting on 25/08/2024").let {
            assertTrue(it.isEmpty(), "25/08/2024 has month=25 in MM/DD mode and should not parse as a valid date")
        }

        parserFinal.parse("The store is open 24/7").let {
            assertTrue(it.isEmpty(), "'24/7' should not parse as a date")
        }

        parserFinal.parse("She has 20/20 vision").let {
            assertTrue(it.isEmpty(), "'20/20' should not parse as a date")
        }

        // European date configuration (DD/MM/YYYY)
        val euParser = TimeParser(ENConfig(useEuropeanDate = true))
        euParser.parse("Meeting on 25/08/2024").let {
            assertEquals(1, it.size, "25/08/2024 should parse in European mode")
            assertEquals(2024, it[0].startTime.first().year)
            assertEquals(8, it[0].startTime.first().month.number)
            assertEquals(25, it[0].startTime.first().day)
        }

        euParser.parse("Holiday on 05/08").let {
            assertEquals(1, it.size, "05/08 should parse as 5 August in European mode")
            assertEquals(8, it[0].startTime.first().month.number)
            assertEquals(5, it[0].startTime.first().day)
        }

        euParser.parse("Invalid 05/15/2024").let {
            assertTrue(it.isEmpty(), "05/15/2024 has month=15 in DD/MM/YYYY mode and should be rejected")
        }

        // Separator rules (Option 3B-1): 2-part hyphenated "4-6" is a time range when preceded by "from", not April 6th
        TimeParser(ENConfig(use24 = true)).parse("I am busy from 4-6").let {
            assertEquals(1, it.size)
            assertEquals("from 4-6", it[0].text.trim())
            assertEquals(4, it[0].startTime.first().hour)
            assertEquals(6, it[0].endTime?.hour)
            assertTrue(TimeUnit.MONTH !in it[0].tagsTimeStart)
        }

        // Space-separated bare numbers ("10 12") should not parse as October 12th
        parserFinal.parse("Check boxes 10 12 in the warehouse").let {
            assertTrue(it.isEmpty(), "Space-separated bare numbers '10 12' should not parse as October 12th")
        }
    }

    @Test
    fun testRelativeTimeEdgeCases() {
        // "in a week" — "a" maps to 1.0
        parserFinal.parse("i have plans in a week").let {
            assertEquals(1, it.size)
            assertEquals("in a week", it[0].text.trim())
            assertEquals(setOf(TimeUnit.WEEK), it[0].tagsTimeStart)
        }

        // "in 1 hour"
        parserFinal.parse("meeting in 1 hour").let {
            assertEquals(1, it.size)
            assertEquals("in 1 hour", it[0].text.trim())
            assertEquals(setOf(TimeUnit.HOUR), it[0].tagsTimeStart)
        }

        // "in a month"
        parserFinal.parse("moving in a month").let {
            assertEquals(1, it.size)
            assertEquals("in a month", it[0].text.trim())
            assertEquals(setOf(TimeUnit.MONTH), it[0].tagsTimeStart)
        }

        // "in a year"
        parserFinal.parse("graduating in a year").let {
            assertEquals(1, it.size)
            assertEquals("in a year", it[0].text.trim())
            assertEquals(setOf(TimeUnit.YEAR), it[0].tagsTimeStart)
        }

        // "in 30 seconds"
        parserFinal.parse("timer set in 30 seconds").let {
            assertEquals(1, it.size)
            assertEquals("in 30 seconds", it[0].text.trim())
            assertEquals(setOf(TimeUnit.SECOND), it[0].tagsTimeStart)
        }

        // "in 60 minutes"
        parserFinal.parse("back in 60 minutes").let {
            assertEquals(1, it.size)
            assertEquals("in 60 minutes", it[0].text.trim())
            assertEquals(setOf(TimeUnit.MINUTE), it[0].tagsTimeStart)
        }
    }

    @Test
    fun testDayOfWeekShorthands() {
        for ((abbrev, day) in listOf(
            "mon" to DayOfWeek.MONDAY,
            "tue" to DayOfWeek.TUESDAY,
            "wed" to DayOfWeek.WEDNESDAY,
            "thu" to DayOfWeek.THURSDAY,
            "fri" to DayOfWeek.FRIDAY,
            "sat" to DayOfWeek.SATURDAY,
            "sun" to DayOfWeek.SUNDAY,
        )) {
            timeParser.parse("something on $abbrev").let {
                assertEquals(abbrev, it[0].text, "Failed to parse day shorthand '$abbrev'")
                assertEquals(setOf(day), it[0].tagsDayOfWeek, "Wrong day for '$abbrev'")
                assertEquals(setOf(TimeUnit.WEEK), it[0].tagsTimeStart, "Wrong tags for '$abbrev'")
            }
        }

        // Alternate variants
        timeParser.parse("see you tues").let {
            assertEquals("tues", it[0].text)
            assertEquals(setOf(DayOfWeek.TUESDAY), it[0].tagsDayOfWeek)
        }

        timeParser.parse("dinner on thurs").let {
            assertEquals("thurs", it[0].text)
            assertEquals(setOf(DayOfWeek.THURSDAY), it[0].tagsDayOfWeek)
        }

        timeParser.parse("dinner on thur").let {
            assertEquals("thur", it[0].text)
            assertEquals(setOf(DayOfWeek.THURSDAY), it[0].tagsDayOfWeek)
        }

        timeParser.parse("meeting wens").let {
            assertEquals("wens", it[0].text)
            assertEquals(setOf(DayOfWeek.WEDNESDAY), it[0].tagsDayOfWeek)
        }

        timeParser.parse("free on wen").let {
            assertEquals("wen", it[0].text)
            assertEquals(setOf(DayOfWeek.WEDNESDAY), it[0].tagsDayOfWeek)
        }
    }

    @Test
    fun testRepeatingAndRangeEdgeCases() {
        // "every week"
        timeParser.parseAndMerge("i go swimming every week").let {
            assertEquals("every week", it[0].text.trim())
            assertEquals(1.0, it[0].repeatOften)
            assertEquals(TimeUnit.WEEK, it[0].repeatTag)
        }

        // "every month"
        timeParser.parseAndMerge("rent is due every month").let {
            assertEquals("every month", it[0].text.trim())
            assertEquals(1.0, it[0].repeatOften)
            assertEquals(TimeUnit.MONTH, it[0].repeatTag)
        }

        // "every year"
        timeParser.parseAndMerge("we celebrate every year").let {
            assertEquals("every year", it[0].text.trim())
            assertEquals(1.0, it[0].repeatOften)
            assertEquals(TimeUnit.YEAR, it[0].repeatTag)
        }

        // "every other month"
        timeParser.parseAndMerge("doctor visit every other month").let {
            assertEquals("every other month", it[0].text.trim())
            assertEquals(2.0, it[0].repeatOften)
            assertEquals(TimeUnit.MONTH, it[0].repeatTag)
        }

        // "every 3 months"
        timeParser.parseAndMerge("quarterly review every 3 months").let {
            assertEquals("every 3 months", it[0].text.trim())
            assertEquals(3.0, it[0].repeatOften)
            assertEquals(TimeUnit.MONTH, it[0].repeatTag)
        }

        // "weekly" standalone
        parserFinal.parse("Have a standup weekly").let {
            assertEquals("weekly", it[0].text)
            assertEquals(1, it[0].repeatOften)
            assertEquals(TimeUnit.WEEK, it[0].repeatTag)
        }

        // "monthly" standalone
        parserFinal.parse("There is a review monthly").let {
            assertEquals("monthly", it[0].text)
            assertEquals(1, it[0].repeatOften)
            assertEquals(TimeUnit.MONTH, it[0].repeatTag)
        }

        // "yearly" standalone
        parserFinal.parse("the festival is yearly").let {
            assertEquals("yearly", it[0].text)
            assertEquals(1, it[0].repeatOften)
            assertEquals(TimeUnit.YEAR, it[0].repeatTag)
        }

        // "until midnight"
        timeParser.parseAndMerge("party goes until midnight").let {
            assertEquals(1, it.size)
            assertNotNull(it[0].endTime)
        }
    }

    @Test
    fun testOrdinalEdgeCases() {
        // "2nd" suffix
        timeParser.parse("the 2nd will be interesting").let {
            assertEquals("2nd", it[0].text)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
            assertEquals(2, it[0].startTime.dayOfMonth)
        }

        // "21st"
        timeParser.parse("the 21st is my birthday").let {
            assertEquals("21st", it[0].text)
            assertEquals(21, it[0].startTime.dayOfMonth)
        }

        // "22nd"
        timeParser.parse("meeting on the 22nd").let {
            assertEquals("22nd", it[0].text)
            assertEquals(22, it[0].startTime.dayOfMonth)
        }

        // "23rd"
        timeParser.parse("the 23rd of december").let {
            assertEquals(23, it[0].startTime.dayOfMonth)
        }

        // "31st"
        timeParser.parse("deadline is the 31st").let {
            assertEquals("31st", it[0].text)
            assertEquals(31, it[0].startTime.dayOfMonth)
        }

        // Word ordinal "first"
        TimeParserTest(ENConfig(use24 = true)).parse("the first is a holiday").let {
            assertEquals("first", it[0].text)
            assertEquals(1, it[0].startTime.dayOfMonth)
        }

        // "twenty-first"
        TimeParserTest(ENConfig(use24 = true)).parse("the twenty-first is special").let {
            assertEquals("twenty-first", it[0].text)
            assertEquals(21, it[0].startTime.dayOfMonth)
        }
    }

    @Test
    fun testKnownBugEdgeCases() {
        // Bug 1: 12pm should be noon (12:00), not 24:00
        timeParser.parse("event at 12pm").let {
            assertEquals(12, it[0].startTime.hour, "12pm should resolve to hour 12")
            assertEquals(0, it[0].startTime.minute)
        }

        // Bug 1: 12am should be midnight (0:00), not 12:00
        timeParser.parse("event at 12am").let {
            assertEquals(0, it[0].startTime.hour, "12am should resolve to hour 0")
            assertEquals(0, it[0].startTime.minute)
        }

        // Bug 1: 12:30pm should be 12:30
        timeParser.parse("lunch is at 12:30pm").let {
            assertEquals(12, it[0].startTime.hour, "12:30pm should resolve to hour 12")
            assertEquals(30, it[0].startTime.minute)
        }

        // Bug 1: 12:30am should be 00:30
        timeParser.parse("sleep at 12:30am").let {
            assertEquals(0, it[0].startTime.hour, "12:30am should resolve to hour 0")
            assertEquals(30, it[0].startTime.minute)
        }

        // Bug 2: Midnight should resolve to 0:00
        timeParser.parse("the show ends at midnight").let {
            assertEquals(0, it[0].startTime.hour, "midnight hour should be 0")
        }

        // Bug 3: "second" ambiguity (word "second" as timeunit vs ordinal)
        parserFinal.parse("wait in 30 seconds").let {
            assertEquals(1, it.size, "Should parse as a single relative time")
            assertEquals("in 30 seconds", it[0].text.trim())
        }

        // Bug 4: Invalid date overflow (April 31st -> May 1st)
        parserFinal.parse("on april 31st").let {
            assertEquals(5, it[0].startTime.first().monthNumber, "April 31 should overflow to May")
            assertEquals(1, it[0].startTime.first().dayOfMonth)
        }

        // Bug 5: False positive matching on common words ("day" in "independence day", "years" in "new years eve")
        parserFinal.parse("independence day is July 4th").let {
            assertEquals(1, it.size, "Should only extract 'July 4th' and not treat 'day' as a separate time")
        }

        parserFinal.parse("over the years on 12/31").let {
            assertEquals(1, it.size, "Should only extract '12/31' and not treat 'years' as a separate time")
        }

        // Bug 6: DateTime.compareTo score disambiguation & "in 1 second" vs ordinal "second"
        val lowScore = config.getDateTime(text = "second", range = 0..5, points = 0, tagsTimeStart = emptySet())
        val highScore = config.getDateTime(
            text = "second",
            range = 0..5,
            points = 5,
            tagsTimeStart = setOf(TimeUnit.DAY, TimeUnit.MONTH, TimeUnit.YEAR)
        )
        assertTrue(
            lowScore.compareTo(highScore) != 0,
            "DateTime.compareTo should distinguish tokens with different points/tags at the same range"
        )

        val fixedAnchor = LocalDateTime(2025, 6, 11, 10, 0, 0)
            .toInstant(TimeZone.UTC)
            .toEpochMilliseconds()
        val fixedParser = TimeParser(ENConfig(relativeTo = fixedAnchor, timeZone = TimeZone.UTC))

        fixedParser.parse("The alarm will trigger in 1 second").let {
            assertEquals(1, it.size)
            assertEquals(
                LocalDateTime(2025, 6, 11, 10, 0, 1),
                it[0].startTime.first(),
                "'in 1 second' should add 1 second (10:00:01), not parse as January 2nd"
            )
        }

        // Bug 7: ENBegin preserves existing left.tagsTimeStart
        val enBegin = ap.panini.kwhen.en.mergers.ENBegin(ENConfig(use24 = true, relativeTo = fixedAnchor, timeZone = TimeZone.UTC))
        val tokenWithStartTag = config.getDateTime(
            text = "5",
            tagsTimeStart = setOf(TimeUnit.DAY),
            tagsTimeEnd = emptySet(),
            generalNumber = 5.0,
            generalTimeTag = TimeUnit.HOUR
        )
        val mergedBegin = enBegin.onMatch(tokenWithStartTag, null, enBegin.prefixPattern.find(" at "), null)
        assertContains(
            mergedBegin!!.tagsTimeStart,
            TimeUnit.DAY,
            "ENBegin should preserve existing left.tagsTimeStart instead of copying left.tagsTimeEnd"
        )

        // Bug 8: "tomorrow at midnight" and "midnight tomorrow" consistency
        val tmrwMidnight = fixedParser.parse("tomorrow at midnight")
        val midnightTmrw = fixedParser.parse("midnight tomorrow")
        assertEquals(tmrwMidnight[0].startTime.first(), midnightTmrw[0].startTime.first())
        assertEquals(tmrwMidnight[0].tagsTimeStart, midnightTmrw[0].tagsTimeStart)

        // Bug 9: MergerConsecutive allows "tomorrow" to merge with rolled-over time ("at 9:30" at 10pm)
        val lateNightAnchor = LocalDateTime(2025, 6, 11, 22, 0, 0)
            .toInstant(TimeZone.UTC)
            .toEpochMilliseconds()
        TimeParser(ENConfig(relativeTo = lateNightAnchor, timeZone = TimeZone.UTC))
            .parse("See you tomorrow at 9:30").let {
                assertEquals(1, it.size)
                assertEquals("tomorrow at 9:30", it[0].text.trim())
            }

        // Bug 10: Unicode case-folding ('İ') does not shift character ranges or throw StringIndexOutOfBoundsException
        val unicodeInput = "İstanbul İzmir İznik meeting tomorrow at 3pm"
        fixedParser.parse(unicodeInput).let {
            assertEquals(1, it.size)
            assertEquals("tomorrow at 3pm", it[0].text.trim())
            assertEquals(unicodeInput.substring(it[0].range), it[0].text)
        }

        // Bug 11: Dense overlapping numbers do not cause O(2^N) merge blowup
        val denseInput = (1..25).joinToString(" ") { "$it" } + " tomorrow at 5pm"
        val elapsed = measureTime {
            val res = fixedParser.parse(denseInput)
            assertTrue(res.isNotEmpty())
        }
        assertTrue(elapsed < 2.seconds, "Parsing dense number tokens took $elapsed (expected < 2s)")
    }

    @Test
    fun testRealisticConversationalSentences() {
        // 1. Scheduling call tomorrow afternoon
        parserFinal.parse("Can we schedule a call tomorrow at 2:30pm?").let {
            assertEquals(1, it.size)
            assertEquals("tomorrow at 2:30pm", it[0].text.trim())
            assertEquals(14, it[0].startTime.first().hour)
            assertEquals(30, it[0].startTime.first().minute)
            assertEquals(
                dateTime.startTime.date.plus(1, DateTimeUnit.DAY),
                it[0].startTime.first().date
            )
            assertContains(it[0].tagsTimeStart, TimeUnit.HOUR)
            assertContains(it[0].tagsTimeStart, TimeUnit.MINUTE)
        }

        // 2. Lunch on Friday at noon
        parserFinal.parse("Let's grab lunch on Friday at noon").let {
            assertEquals(1, it.size)
            assertEquals("on Friday at noon", it[0].text.trim())
            assertEquals(12, it[0].startTime.first().hour)
            assertEquals(kotlinx.datetime.DayOfWeek.FRIDAY, it[0].startTime.first().dayOfWeek)
        }

        // 3. Repeating team sync every Monday at 10am
        parserFinal.parse("Team sync every Monday at 10am in the main room").let {
            assertEquals(1, it.size)
            assertEquals("every Monday at 10am", it[0].text.trim())
            assertEquals(10, it[0].startTime.first().hour)
            assertEquals(0, it[0].startTime.first().minute)
            assertEquals(1, it[0].repeatOften)
            assertEquals(TimeUnit.WEEK, it[0].repeatTag)
            assertEquals(kotlinx.datetime.DayOfWeek.MONDAY, it[0].startTime.first().dayOfWeek)
        }

        // 4. Recurring meeting every other Tuesday at 3:15pm
        parserFinal.parse("Sprint planning is every other Tuesday at 3:15pm").let {
            assertEquals(1, it.size)
            assertEquals("every other Tuesday at 3:15pm", it[0].text.trim())
            assertEquals(15, it[0].startTime.first().hour)
            assertEquals(15, it[0].startTime.first().minute)
            assertEquals(2, it[0].repeatOften)
            assertEquals(TimeUnit.WEEK, it[0].repeatTag)
            assertEquals(kotlinx.datetime.DayOfWeek.TUESDAY, it[0].startTime.first().dayOfWeek)
        }

        // 5. Taxes reminder
        parserFinal.parse("Reminder to submit the taxes by April 15th").let {
            assertEquals(1, it.size)
            assertEquals("April 15th", it[0].text.trim())
            assertEquals(4, it[0].startTime.first().monthNumber)
            assertEquals(15, it[0].startTime.first().dayOfMonth)
        }

        // 6. Deadline tomorrow night at 11:59pm
        parserFinal.parse("Project deadline is tomorrow night at 11:59pm").let {
            assertEquals(1, it.size)
            assertEquals(23, it[0].startTime.first().hour)
            assertEquals(59, it[0].startTime.first().minute)
        }

        // 7. Text chat shorthand: free tn at 8pm
        parserFinal.parse("Hey are you free tn at 8pm?").let {
            assertEquals(1, it.size)
            assertEquals("tn at 8pm", it[0].text.trim())
            assertEquals(20, it[0].startTime.first().hour)
            assertEquals(0, it[0].startTime.first().minute)
        }

        // 8. Text chat shorthand: 2moro morning at 9am
        parserFinal.parse("Let's meet up 2moro morning at 9am").let {
            assertEquals(1, it.size)
            assertEquals(9, it[0].startTime.first().hour)
            assertEquals(0, it[0].startTime.first().minute)
        }

        // 9. Quick relative interval: in 45 mins
        parserFinal.parse("Heading to the gym in 45 mins").let {
            assertEquals(1, it.size)
            assertEquals("in 45 mins", it[0].text.trim())
            assertEquals(setOf(TimeUnit.MINUTE), it[0].tagsTimeStart)
        }

        // 10. Flight departure next Friday at 6:45pm
        parserFinal.parse("Flight departs next Friday at 6:45pm").let {
            assertEquals(1, it.size)
            assertEquals(18, it[0].startTime.first().hour)
            assertEquals(45, it[0].startTime.first().minute)
            assertEquals(kotlinx.datetime.DayOfWeek.FRIDAY, it[0].startTime.first().dayOfWeek)
        }

        // 11. Doctor appointment date with time
        parserFinal.parse("Doctor appointment on 09/25 at 10:30am").let {
            assertEquals(1, it.size)
            assertEquals(9, it[0].startTime.first().monthNumber)
            assertEquals(25, it[0].startTime.first().dayOfMonth)
            assertEquals(10, it[0].startTime.first().hour)
            assertEquals(30, it[0].startTime.first().minute)
        }

        // 12. Party range on Saturday
        parserFinal.parse("Birthday party this Saturday from 7pm to 11pm").let {
            assertEquals(1, it.size)
            assertEquals(19, it[0].startTime.first().hour)
            assertEquals(23, it[0].endTime!!.hour)
            assertEquals(kotlinx.datetime.DayOfWeek.SATURDAY, it[0].startTime.first().dayOfWeek)
        }

        // 13. Daily standup
        parserFinal.parse("Standup daily at 9:00am").let {
            assertEquals(1, it.size)
            assertEquals(9, it[0].startTime.first().hour)
            assertEquals(0, it[0].startTime.first().minute)
            assertEquals(1, it[0].repeatOften)
            assertEquals(TimeUnit.DAY, it[0].repeatTag)
        }

        // 14. Pay bill every month on the 1st
        parserFinal.parse("Pay electricity bill every month on the 1st").let {
            assertEquals(1, it.size)
            assertEquals(1, it[0].startTime.first().dayOfMonth)
            assertEquals(1, it[0].repeatOften)
            assertEquals(TimeUnit.MONTH, it[0].repeatTag)
        }

        // 15. Hospital follow-up in 2 weeks
        parserFinal.parse("Hospital follow-up in 2 weeks").let {
            assertEquals(1, it.size)
            assertEquals("in 2 weeks", it[0].text.trim())
            assertEquals(setOf(TimeUnit.WEEK), it[0].tagsTimeStart)
        }

        // 16. Multi-day date range
        parserFinal.parse("The conference runs from 10/12 to 10/15").let {
            assertEquals(1, it.size)
            assertEquals(10, it[0].startTime.first().monthNumber)
            assertEquals(12, it[0].startTime.first().dayOfMonth)
            assertEquals(10, it[0].endTime!!.monthNumber)
            assertEquals(15, it[0].endTime!!.dayOfMonth)
        }
    }

    @Test
    fun testArbitraryNumbersBeforeTime() {
        // "in room 401 at 3am" -> 401 is an arbitrary room number and should NOT corrupt or merge with "at 3am"
        parserFinal.parse("in room 401 at 3am").let {
            assertEquals(1, it.size, "Should only extract 'at 3am' and ignore room 401")
            assertEquals("at 3am", it[0].text.trim())
            assertEquals(3, it[0].startTime.first().hour)
            assertEquals(0, it[0].startTime.first().minute)
            assertContains(it[0].tagsTimeStart, TimeUnit.HOUR)
        }

        // "meet on floor 5 at 3pm" -> 5 is a floor number
        parserFinal.parse("meet on floor 5 at 3pm").let {
            assertEquals(1, it.size, "Should only extract 'at 3pm' and ignore floor 5")
            assertEquals("at 3pm", it[0].text.trim())
            assertEquals(15, it[0].startTime.first().hour)
            assertEquals(0, it[0].startTime.first().minute)
        }

        // "take bus 42 on Monday" -> 42 is a bus number
        parserFinal.parse("take bus 42 on Monday").let {
            assertEquals(1, it.size, "Should only extract 'on Monday' and ignore bus 42")
            assertEquals("on Monday", it[0].text.trim())
            assertEquals(kotlinx.datetime.DayOfWeek.MONDAY, it[0].startTime.first().dayOfWeek)
        }

        // "flight 747 on Dec 25 at 6am" -> 747 is a flight number
        parserFinal.parse("flight 747 on Dec 25 at 6am").let {
            assertEquals(1, it.size, "Should only extract 'on Dec 25 at 6am' and ignore flight 747")
            assertEquals(12, it[0].startTime.first().monthNumber)
            assertEquals(25, it[0].startTime.first().dayOfMonth)
            assertEquals(6, it[0].startTime.first().hour)
            assertEquals(0, it[0].startTime.first().minute)
        }

        // "call 911 tomorrow at noon" -> 911 is an emergency number
        parserFinal.parse("call 911 tomorrow at noon").let {
            assertEquals(1, it.size, "Should only extract 'tomorrow at noon' and ignore 911")
            assertEquals(12, it[0].startTime.first().hour)
        }

        val fixedAnchor = LocalDateTime(2025, 6, 11, 10, 0, 0)
            .toInstant(TimeZone.UTC)
            .toEpochMilliseconds()
        val fixedParser = TimeParser(ENConfig(use24 = true, relativeTo = fixedAnchor, timeZone = TimeZone.UTC))

        // "In 5 minutes a bell rings" -> trailing 'a' must not overwrite '5' in MergerGeneralTags
        fixedParser.parse("In 5 minutes a bell rings").let {
            assertEquals(1, it.size)
            assertEquals("In 5 minutes", it[0].text.trim())
            assertEquals(LocalDateTime(2025, 6, 11, 10, 5, 0), it[0].startTime.first())
        }

        // "In 2 hours 3 people arrived" -> trailing '3' must not overwrite '2' in MergerGeneralTags
        fixedParser.parse("In 2 hours 3 people arrived").let {
            assertEquals(1, it.size)
            assertEquals("In 2 hours", it[0].text.trim())
            assertEquals(LocalDateTime(2025, 6, 11, 12, 0, 0), it[0].startTime.first())
        }

        // "Table for 2 at 7pm" -> "for 2" without a time unit is not a time
        fixedParser.parse("Table for 2 at 7pm").let {
            assertEquals(1, it.size)
            assertEquals("at 7pm", it[0].text.trim())
            assertEquals(19, it[0].startTime.first().hour)
        }

        // "We are in a Friday meeting" -> "in a" before a date is not "in 1 hour"
        fixedParser.parse("We are in a Friday meeting").let {
            assertEquals(1, it.size)
            assertEquals("Friday", it[0].text.trim())
            assertEquals(setOf(TimeUnit.WEEK), it[0].tagsTimeStart)
        }

        // "I have a meeting at 3pm and a call at 5pm" -> "and a" must not be swallowed into "at 3pm"
        fixedParser.parse("I have a meeting at 3pm and a call at 5pm").let {
            assertEquals(2, it.size)
            assertEquals("at 3pm", it[0].text.trim())
            assertEquals("at 5pm", it[1].text.trim())
        }

        // "We studied for 3 hours" -> relative duration (10:00 + 3h = 13:00), not absolute clock hour 3:00
        fixedParser.parse("We studied for 3 hours").let {
            assertEquals(1, it.size)
            assertEquals("for 3 hours", it[0].text.trim())
            assertEquals(LocalDateTime(2025, 6, 11, 13, 0, 0), it[0].endTime)
        }
    }

    @Test
    fun testComprehensiveRelativeTimes() {
        // 1. Written number words with relative units
        parserFinal.parse("we will meet in two hours").let {
            assertEquals(1, it.size)
            assertEquals("in two hours", it[0].text.trim())
            assertEquals(setOf(TimeUnit.HOUR), it[0].tagsTimeStart)
        }

        parserFinal.parse("the package arrives in three days").let {
            assertEquals(1, it.size)
            assertEquals("in three days", it[0].text.trim())
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
        }

        parserFinal.parse("leave in five minutes").let {
            assertEquals(1, it.size)
            assertEquals("in five minutes", it[0].text.trim())
            assertEquals(setOf(TimeUnit.MINUTE), it[0].tagsTimeStart)
        }

        parserFinal.parse("trip begins in ten weeks").let {
            assertEquals(1, it.size)
            assertEquals("in ten weeks", it[0].text.trim())
            assertEquals(setOf(TimeUnit.WEEK), it[0].tagsTimeStart)
        }

        parserFinal.parse("return in six months").let {
            assertEquals(1, it.size)
            assertEquals("in six months", it[0].text.trim())
            assertEquals(setOf(TimeUnit.MONTH), it[0].tagsTimeStart)
        }

        parserFinal.parse("graduate in four years").let {
            assertEquals(1, it.size)
            assertEquals("in four years", it[0].text.trim())
            assertEquals(setOf(TimeUnit.YEAR), it[0].tagsTimeStart)
        }

        parserFinal.parse("project due in one day").let {
            assertEquals(1, it.size)
            assertEquals("in one day", it[0].text.trim())
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
        }

        // 2. Relative time offsets from concrete times
        parserFinal.parse("the meeting starts 15 minutes after 4pm").let {
            assertEquals(1, it.size)
            assertEquals(16, it[0].startTime.first().hour)
            assertEquals(15, it[0].startTime.first().minute)
            assertContains(it[0].tagsTimeStart, TimeUnit.HOUR)
            assertContains(it[0].tagsTimeStart, TimeUnit.MINUTE)
        }

        parserFinal.parse("alarms sound 45 minutes after 2:00").let {
            assertEquals(1, it.size)
            assertEquals(2, it[0].startTime.first().hour)
            assertEquals(45, it[0].startTime.first().minute)
        }

        parserFinal.parse("bus arrives 10 mins from 8am").let {
            assertEquals(1, it.size)
            assertEquals(8, it[0].startTime.first().hour)
            assertEquals(10, it[0].startTime.first().minute)
        }

        parserFinal.parse("lunch is 1 hour after 11am").let {
            assertEquals(1, it.size)
            assertEquals(12, it[0].startTime.first().hour)
            assertEquals(0, it[0].startTime.first().minute)
        }

        // 3. Relative date offsets from concrete dates
        parserFinal.parse("the delivery is 3 days from jul 4th").let {
            assertEquals(1, it.size)
            assertEquals(7, it[0].startTime.first().monthNumber)
            assertEquals(7, it[0].startTime.first().dayOfMonth)
        }

        parserFinal.parse("review is 10 days from 05/10").let {
            assertEquals(1, it.size)
            assertEquals(5, it[0].startTime.first().monthNumber)
            assertEquals(20, it[0].startTime.first().dayOfMonth)
        }

        parserFinal.parse("the checkup is 2 months from Jan 15").let {
            assertEquals(1, it.size)
            assertEquals(3, it[0].startTime.first().monthNumber)
            assertEquals(15, it[0].startTime.first().dayOfMonth)
        }

        // 4. Abbreviated unit relative times
        parserFinal.parse("countdown is in 15 secs").let {
            assertEquals(1, it.size)
            assertEquals("in 15 secs", it[0].text.trim())
            assertEquals(setOf(TimeUnit.SECOND), it[0].tagsTimeStart)
        }

        parserFinal.parse("workout in 3 hrs").let {
            assertEquals(1, it.size)
            assertEquals("in 3 hrs", it[0].text.trim())
            assertEquals(setOf(TimeUnit.HOUR), it[0].tagsTimeStart)
        }

        parserFinal.parse("vacation in 2 wks").let {
            assertEquals(1, it.size)
            assertEquals("in 2 wks", it[0].text.trim())
            assertEquals(setOf(TimeUnit.WEEK), it[0].tagsTimeStart)
        }

        parserFinal.parse("lease expires in 6 mos").let {
            assertEquals(1, it.size)
            assertEquals("in 6 mos", it[0].text.trim())
            assertEquals(setOf(TimeUnit.MONTH), it[0].tagsTimeStart)
        }

        parserFinal.parse("warranty valid in 5 yrs").let {
            assertEquals(1, it.size)
            assertEquals("in 5 yrs", it[0].text.trim())
            assertEquals(setOf(TimeUnit.YEAR), it[0].tagsTimeStart)
        }

        // 5. Prefixes with relative duration
        parserFinal.parse("show starting in 10 minutes").let {
            assertEquals(1, it.size)
            assertEquals("starting in 10 minutes", it[0].text.trim())
            assertEquals(setOf(TimeUnit.MINUTE), it[0].tagsTimeStart)
        }

        parserFinal.parse("call back after 3 hours").let {
            assertEquals(1, it.size)
            assertEquals("after 3 hours", it[0].text.trim())
            assertContains(it[0].tagsTimeStart, TimeUnit.HOUR)
        }

        val fixedAnchor = LocalDateTime(2025, 6, 11, 10, 0, 0)
            .toInstant(TimeZone.UTC)
            .toEpochMilliseconds()
        val fixedParser = TimeParser(ENConfig(relativeTo = fixedAnchor, timeZone = TimeZone.UTC))

        fixedParser.parse("The contract expires after 2 years").let {
            assertEquals(1, it.size)
            assertEquals(
                LocalDateTime(2027, 6, 11, 10, 0, 0),
                it[0].startTime.first(),
                "'after 2 years' should add 2 years (2027), not set absolute year to 0002 AD"
            )
        }

        fixedParser.parse("The package arrives after 2 days").let {
            assertEquals(1, it.size)
            assertEquals(
                LocalDateTime(2025, 6, 13, 10, 0, 0),
                it[0].startTime.first(),
                "'after 2 days' should add 2 days (June 13), not set day-of-month to June 2nd"
            )
        }

        // 6. Written number words above ten
        fixedParser.parse("alarm in fifteen minutes").let {
            assertEquals(1, it.size)
            assertEquals("in fifteen minutes", it[0].text.trim())
            assertEquals(LocalDateTime(2025, 6, 11, 10, 15, 0), it[0].startTime.first())
        }

        fixedParser.parse("meeting in twenty minutes").let {
            assertEquals(1, it.size)
            assertEquals("in twenty minutes", it[0].text.trim())
            assertEquals(LocalDateTime(2025, 6, 11, 10, 20, 0), it[0].startTime.first())
        }

        fixedParser.parse("leaves in forty-five minutes").let {
            assertEquals(1, it.size)
            assertEquals("in forty-five minutes", it[0].text.trim())
            assertEquals(LocalDateTime(2025, 6, 11, 10, 45, 0), it[0].startTime.first())
        }

        // 7. Relative offset from weekday: "3 days from Monday" -> Thursday
        fixedParser.parse("appointment is 3 days from Monday").let {
            assertEquals(1, it.size)
            assertEquals("3 days from Monday", it[0].text.trim())
            assertEquals(2025, it[0].startTime.first().year)
            assertEquals(6, it[0].startTime.first().monthNumber)
            assertEquals(19, it[0].startTime.first().dayOfMonth) // June 16 (Mon) + 3 days = June 19 (Thu)
            assertEquals(kotlinx.datetime.DayOfWeek.THURSDAY, it[0].startTime.first().dayOfWeek)
        }
    }

    @Test
    fun testMonthRolloverTomorrowYesterday() {
        // Jan 31 boundary: tomorrow is Feb 1
        val jan31Anchor = LocalDateTime(2025, 1, 31, 8, 0, 0).toInstant(TimeZone.UTC).toEpochMilliseconds()
        val jan31Parser = TimeParser(ENConfig(relativeTo = jan31Anchor, timeZone = TimeZone.UTC))

        jan31Parser.parse("meeting at 3pm tomorrow").let {
            assertEquals(1, it.size)
            assertEquals(LocalDateTime(2025, 2, 1, 15, 0, 0), it[0].startTime.first())
        }
        jan31Parser.parse("meeting tomorrow at 3pm").let {
            assertEquals(1, it.size)
            assertEquals(LocalDateTime(2025, 2, 1, 15, 0, 0), it[0].startTime.first())
        }

        // Mar 1 boundary: yesterday is Feb 28
        val mar1Anchor = LocalDateTime(2025, 3, 1, 8, 0, 0).toInstant(TimeZone.UTC).toEpochMilliseconds()
        val mar1Parser = TimeParser(ENConfig(relativeTo = mar1Anchor, timeZone = TimeZone.UTC))

        mar1Parser.parse("happened at 3pm yesterday").let {
            assertEquals(1, it.size)
            assertEquals(LocalDateTime(2025, 2, 28, 15, 0, 0), it[0].startTime.first())
        }
        mar1Parser.parse("happened yesterday at 3pm").let {
            assertEquals(1, it.size)
            assertEquals(LocalDateTime(2025, 2, 28, 15, 0, 0), it[0].startTime.first())
        }

        // Dec 31 boundary: tomorrow is Jan 1 of next year
        val dec31Anchor = LocalDateTime(2025, 12, 31, 8, 0, 0).toInstant(TimeZone.UTC).toEpochMilliseconds()
        val dec31Parser = TimeParser(ENConfig(relativeTo = dec31Anchor, timeZone = TimeZone.UTC))

        dec31Parser.parse("party at 3pm tomorrow").let {
            assertEquals(1, it.size)
            assertEquals(LocalDateTime(2026, 1, 1, 15, 0, 0), it[0].startTime.first())
        }
        dec31Parser.parse("party tomorrow at 3pm").let {
            assertEquals(1, it.size)
            assertEquals(LocalDateTime(2026, 1, 1, 15, 0, 0), it[0].startTime.first())
        }
    }

    @Test
    fun testNaturalOrderDurationRanges() {
        val anchor = LocalDateTime(2025, 6, 11, 8, 0, 0).toInstant(TimeZone.UTC).toEpochMilliseconds()
        val parser = TimeParser(ENConfig(relativeTo = anchor, timeZone = TimeZone.UTC))

        // Natural order: <start> for <duration>
        parser.parse("Vacation from June 3rd for 3 days").let {
            assertEquals(1, it.size)
            assertEquals("from June 3rd for 3 days", it[0].text.trim())
            assertEquals(LocalDateTime(2025, 6, 3, 8, 0, 0), it[0].startTime.first())
            assertEquals(LocalDateTime(2025, 6, 6, 8, 0, 0), it[0].endTime)
        }

        parser.parse("Event tomorrow at 3pm for 2 hours").let {
            assertEquals(1, it.size)
            assertEquals("tomorrow at 3pm for 2 hours", it[0].text.trim())
            assertEquals(LocalDateTime(2025, 6, 12, 15, 0, 0), it[0].startTime.first())
            assertEquals(LocalDateTime(2025, 6, 12, 17, 0, 0), it[0].endTime)
        }

        // Reversed order: for <duration> <start>
        parser.parse("Event for 2 hours tomorrow at 3pm").let {
            assertEquals(1, it.size)
            assertEquals(LocalDateTime(2025, 6, 12, 15, 0, 0), it[0].startTime.first())
            assertEquals(LocalDateTime(2025, 6, 12, 17, 0, 0), it[0].endTime)
        }
    }

    @Test
    fun testWeekdayRangesAndPrefixes() {
        val anchor = LocalDateTime(2025, 6, 11, 8, 0, 0).toInstant(TimeZone.UTC).toEpochMilliseconds()
        val parser = TimeParser(ENConfig(relativeTo = anchor, timeZone = TimeZone.UTC))

        // "from Monday to Friday"
        parser.parse("The workshop runs from Monday to Friday").let {
            assertEquals(1, it.size)
            assertEquals("from Monday to Friday", it[0].text.trim())
            assertEquals(kotlinx.datetime.DayOfWeek.MONDAY, it[0].startTime.first().dayOfWeek)
            assertEquals(kotlinx.datetime.DayOfWeek.FRIDAY, it[0].endTime!!.dayOfWeek)
            assertTrue(it[0].endTime!! > it[0].startTime.first())
        }

        // "starting Monday until Friday"
        parser.parse("Course starting Monday until Friday").let {
            assertEquals(1, it.size)
            assertEquals("starting Monday until Friday", it[0].text.trim())
            assertEquals(kotlinx.datetime.DayOfWeek.MONDAY, it[0].startTime.first().dayOfWeek)
            assertEquals(kotlinx.datetime.DayOfWeek.FRIDAY, it[0].endTime!!.dayOfWeek)
        }

        // "this Saturday" / "this week"
        parser.parse("Join us this Saturday").let {
            assertEquals(1, it.size)
            assertEquals("this Saturday", it[0].text.trim())
            assertEquals(kotlinx.datetime.DayOfWeek.SATURDAY, it[0].startTime.first().dayOfWeek)
        }

        parser.parse("Sync this week").let {
            assertEquals(1, it.size)
            assertEquals("this week", it[0].text.trim())
            assertContains(it[0].tagsTimeStart, TimeUnit.WEEK)
        }

        // "sat." dot notation followed by time
        parser.parse("Meet on sat. at 3pm").let {
            assertEquals(1, it.size)
            assertEquals("on sat. at 3pm", it[0].text.trim())
            assertEquals(15, it[0].startTime.first().hour)
            assertEquals(kotlinx.datetime.DayOfWeek.SATURDAY, it[0].startTime.first().dayOfWeek)
        }
    }

    @Test
    fun testSpecificTimeAndVagueTimeOfDayDisambiguation() {
        val anchor = LocalDateTime(2025, 6, 11, 8, 0, 0).toInstant(TimeZone.UTC).toEpochMilliseconds()
        val parser = TimeParser(ENConfig(relativeTo = anchor, timeZone = TimeZone.UTC))

        // "at 8am in the morning" -> single expression at 08:00
        parser.parse("flight is at 8am in the morning").let {
            assertEquals(1, it.size)
            assertEquals("at 8am in the morning", it[0].text.trim())
            assertEquals(8, it[0].startTime.first().hour)
            assertEquals(0, it[0].startTime.first().minute)
        }

        // "in the morning at 8am" -> single expression at 08:00
        parser.parse("flight is in the morning at 8am").let {
            assertEquals(1, it.size)
            assertEquals("in the morning at 8am", it[0].text.trim())
            assertEquals(8, it[0].startTime.first().hour)
            assertEquals(0, it[0].startTime.first().minute)
        }

        // "at 7 in the evening" -> 19:00
        parser.parse("dinner at 7 in the evening").let {
            assertEquals(1, it.size)
            assertEquals("at 7 in the evening", it[0].text.trim())
            assertEquals(19, it[0].startTime.first().hour)
            assertEquals(0, it[0].startTime.first().minute)
        }

        // "tomorrow at 11:59pm at night" -> 23:59, does not overwrite with 20:00
        parser.parse("deadline is tomorrow at 11:59pm at night").let {
            assertEquals(1, it.size)
            assertEquals("tomorrow at 11:59pm at night", it[0].text.trim())
            assertEquals(23, it[0].startTime.first().hour)
            assertEquals(59, it[0].startTime.first().minute)
            assertEquals(LocalDateTime(2025, 6, 12, 23, 59, 0), it[0].startTime.first())
        }
    }
}
