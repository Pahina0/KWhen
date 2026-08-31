import ap.panini.kwhen.DayOfWeek
import ap.panini.kwhen.TimeParser
import ap.panini.kwhen.TimeUnit
import ap.panini.kwhen.configs.ENConfig
import ap.panini.kwhen.util.copy
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
    }
}