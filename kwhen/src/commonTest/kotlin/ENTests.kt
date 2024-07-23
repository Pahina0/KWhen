import ap.panini.kwhen.DateTime
import ap.panini.kwhen.DayOfWeek
import ap.panini.kwhen.TimeParser
import ap.panini.kwhen.TimeUnit
import ap.panini.kwhen.configs.ENConfig
import ap.panini.kwhen.util.copy
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


class ENTests {
    private lateinit var timeParser: TimeParserTest
    private lateinit var parserFinal: TimeParser

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
                DateTime().startTime.run { copy(date.plus(1, DateTimeUnit.DAY)) },
                it[0].startTime
            )
        }



        timeParser.parse("it is like yesterday's weather today").let {
            assertEquals("yesterday", it[0].text)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
            assertEquals(
                DateTime().startTime.run { copy(date.minus(1, DateTimeUnit.DAY)) },
                it[0].startTime
            )
        }
    }

    @Test
    fun testBasicTime() {
        timeParser.parse("i will swim in the afternoon").let {
            assertEquals("afternoon", it[0].text)
            assertEquals(setOf(TimeUnit.HOUR), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(hour = 15) }, it[0].startTime)
        }

        timeParser.parse("the morning was nice and cool").let {
            assertEquals("morning", it[0].text)
            assertEquals(setOf(TimeUnit.HOUR), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(hour = 9) }, it[0].startTime)
        }

        timeParser.parse("tonight was kind of boring").let {
            assertEquals("tonight", it[0].text)
            assertEquals(setOf(TimeUnit.HOUR, TimeUnit.DAY), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(hour = 20) }, it[0].startTime)
        }

        TimeParserTest(ENConfig(afternoon = 14)).parse("i will go run in the afternoon today").let {
            assertEquals("afternoon", it[0].text)
            assertEquals(setOf(TimeUnit.HOUR, TimeUnit.HOUR), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(hour = 14) }, it[0].startTime)
        }
    }

    @Test
    fun testDayMonthYear() {
        timeParser.parse("12 june 07 was an extremely hot day").let {
            assertEquals("12 june 07", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH, TimeUnit.YEAR), it[0].tagsTimeStart)
            assertEquals(
                DateTime().startTime.run { copy(year = 2007, monthNumber = 6, dayOfMonth = 12) },
                it[0].startTime
            )
        }

        timeParser.parse("it will be a blast on the 4th of july").let {
            assertEquals("4th of july", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH), it[0].tagsTimeStart)
            assertEquals(
                DateTime().startTime.run { copy(monthNumber = 7, dayOfMonth = 4) },
                it[0].startTime
            )
        }

        timeParser.parse("5 aug. 1448 stuff happened").let {
            assertEquals("5 aug. 1448", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH, TimeUnit.YEAR), it[0].tagsTimeStart)
            assertEquals(
                DateTime().startTime.run { copy(year = 1448, monthNumber = 8, dayOfMonth = 5) },
                it[0].startTime
            )
        }

        timeParser.parse("18 sep 76 there was (not) a battle").let {
            assertEquals("18 sep 76", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH, TimeUnit.YEAR), it[0].tagsTimeStart)
            assertEquals(
                DateTime().startTime.run { copy(year = 1976, monthNumber = 9, dayOfMonth = 18) },
                it[0].startTime
            )
        }

        timeParser.parse("a bird crashed on the 1st of    feb").let {
            assertEquals("1st of    feb", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH), it[0].tagsTimeStart)
            assertEquals(
                DateTime().startTime.run { copy(monthNumber = 2, dayOfMonth = 1) },
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
                DateTime().startTime.run { copy(year = 2007, monthNumber = 6, dayOfMonth = 12) },
                it[0].startTime
            )
        }

        timeParser.parse("it will be a blast on the jul 4th ").let {
            assertEquals("jul 4th", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH), it[0].tagsTimeStart)
            assertEquals(
                DateTime().startTime.run { copy(monthNumber = 7, dayOfMonth = 4) },
                it[0].startTime
            )
        }

        timeParser.parse("aug. 5 1448 stuff happened").let {
            assertEquals("aug. 5 1448", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH, TimeUnit.YEAR), it[0].tagsTimeStart)
            assertEquals(
                DateTime().startTime.run { copy(year = 1448, monthNumber = 8, dayOfMonth = 5) },
                it[0].startTime
            )
        }

        timeParser.parse(" sep-18 76 there was (not) a battle").let {
            assertEquals("sep-18 76", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH, TimeUnit.YEAR), it[0].tagsTimeStart)
            assertEquals(
                DateTime().startTime.run { copy(year = 1976, monthNumber = 9, dayOfMonth = 18) },
                it[0].startTime
            )
        }

        timeParser.parse("a bird crashed on the feb/1").let {
            assertEquals("feb/1", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH), it[0].tagsTimeStart)
            assertEquals(
                DateTime().startTime.run { copy(monthNumber = 2, dayOfMonth = 1) },
                it[0].startTime
            )
        }

        timeParser.parse("04/13/05").let {
            assertEquals("04/13/05", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH, TimeUnit.YEAR), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run {
                copy(
                    year = 2005,
                    monthNumber = 4,
                    dayOfMonth = 13
                )
            }, it[0].startTime)
        }

        timeParser.parse("4-13-05").let {
            assertEquals("4-13-05", it[0].text)
            assertEquals(setOf(TimeUnit.DAY, TimeUnit.MONTH, TimeUnit.YEAR), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run {
                copy(
                    year = 2005,
                    monthNumber = 4,
                    dayOfMonth = 13
                )
            }, it[0].startTime)
        }

        timeParser.parse("i'm going to leave in december").let {
            assertEquals("december", it[0].text)
            assertEquals(setOf(TimeUnit.MONTH), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(monthNumber = 12) }, it[0].startTime)
        }

    }

    @Test
    fun testNumericOrdinal() {
        timeParser.parse("im going to swim at 3am").let {
            assertEquals("3am", it[0].text)
            assertEquals(setOf(TimeUnit.HOUR, TimeUnit.MINUTE), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(hour = 3, minute = 0) }, it[0].startTime)
        }

        timeParser.parse("im going to swim at 03:25pm").let {
            assertEquals("03:25pm", it[0].text)
            assertEquals(setOf(TimeUnit.HOUR, TimeUnit.MINUTE), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(hour = 15, minute = 25) }, it[0].startTime)

        }


        TimeParserTest(ENConfig(use24 = true)).parse("the fourth will be crazy").let {
            assertEquals("fourth", it[0].text)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(dayOfMonth = 4) }, it[0].startTime)

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
            assertEquals(DateTime().startTime.run { copy(dayOfMonth = 4) }, it[0].startTime)
            assertEquals(DateTime().startTime.run { copy(dayOfMonth = 18) }, it[0].endTime)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeEnd)
        }

        timeParser.parseAndMerge("i have nothing to do today till tmrw").let {
            assertEquals("today till tmrw", it[0].text.trim())
            assertEquals(DateTime().startTime, it[0].startTime)
            assertEquals(
                DateTime().startTime.run { copy(date.plus(1, DateTimeUnit.DAY)) },
                it[0].endTime
            )
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeStart)
            assertEquals(setOf(TimeUnit.DAY), it[0].tagsTimeEnd)
        }

        timeParser.parseAndMerge("in the morning i will go eat until the night").let {
            assertEquals("in the morning", it[0].text.trim())
            assertEquals(DateTime().startTime.copy(hour = 9), it[0].startTime)
            assertEquals(setOf(TimeUnit.HOUR), it[0].tagsTimeStart)

            assertEquals("until the night", it[1].text.trim())
            assertEquals(DateTime().startTime.copy(hour = 20), it[1].endTime)
            assertEquals(setOf(TimeUnit.HOUR), it[1].tagsTimeEnd)
        }
    }

    @Test
    fun testGenericAndMerge() {
        TimeParserTest(config = ENConfig(use24 = true)).parseAndMerge("im busy from 4 to 6").let {
            assertEquals("from 4 to 6", it[0].text.trim())
            assertEquals(DateTime().startTime.run { copy(hour = 4, minute = 0) }, it[0].startTime)
            assertEquals(DateTime().startTime.run { copy(hour = 6, minute = 0) }, it[0].endTime)
            assertEquals(setOf(TimeUnit.MINUTE, TimeUnit.HOUR), it[0].tagsTimeStart)
            assertEquals(setOf(TimeUnit.MINUTE, TimeUnit.HOUR), it[0].tagsTimeEnd)
        }

        timeParser.parseAndMerge("the diving team will go on a field trip in 18 months").let {
            assertEquals("in 18 months", it[0].text.trim())
            assertEquals(
                DateTime().startTime.run { copy(monthNumber = monthNumber + 18) },
                it[0].startTime
            )
            assertEquals(setOf(TimeUnit.MONTH), it[0].tagsTimeStart)
        }

        timeParser.parseAndMerge("im going to japan 06/18 - dec 2025").let {
            assertEquals("06/18 - dec 2025", it[0].text.trim())
            assertEquals(
                DateTime().startTime.run { copy(monthNumber = 6, dayOfMonth = 18) },
                it[0].startTime
            )
            assertEquals(
                DateTime().startTime.run { copy(monthNumber = 12, year = 2025) },
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
                DateTime().startTime.run { copy(monthNumber = 7, dayOfMonth = 8) },
                it[0].startTime
            )
            assertEquals(setOf(TimeUnit.MONTH, TimeUnit.DAY), it[0].tagsTimeStart)

        }
    }


    @Test
    fun testProcessList() {
        timeParser.parseMergeProcess("i will go swim on every other 9th  10th, 12th").let {
            assertEquals("on every other 9th  10th, 12th", it[0].text.trim())
            assertEquals(3, it[0].startTime.size)
            assertEquals(TimeUnit.MONTH, it[0].repeatTag)
            assertEquals(2, it[0].repeatOften)
        }

        timeParser.parseMergeProcess("i swim every mon, tues and fri").let {
            assertEquals("every mon, tues and fri", it[0].text.trim())
            assertEquals(3, it[0].startTime.size)
            assertEquals(setOf(TimeUnit.WEEK), it[0].tagsTimeStart)
            assertEquals(TimeUnit.WEEK, it[0].repeatTag)
            assertEquals(1, it[0].repeatOften)
        }

        timeParser.parseMergeProcess("the world is boring every june, jul, and aug").let {
            assertEquals("every june, jul, and aug", it[0].text.trim())
            assertEquals(3, it[0].startTime.size)
            assertEquals(setOf(TimeUnit.MONTH), it[0].tagsTimeStart)
            assertEquals(TimeUnit.YEAR, it[0].repeatTag)
            assertEquals(1, it[0].repeatOften)
        }
    }

    @Test
    fun testProcessSentence() {
        parserFinal.parse("Jul 9 is going to be crazy").let {
            assertEquals(1, it.size)
            assertEquals("Jul 9", it[0].text.trim())
            assertEquals(1, it[0].startTime.size)
            assertEquals(
                DateTime().startTime.run { copy(monthNumber = 7, dayOfMonth = 9) },
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
            assertEquals(setOf(TimeUnit.HOUR, TimeUnit.MINUTE), it[0].tagsTimeStart)
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
            assertEquals(setOf(TimeUnit.HOUR, TimeUnit.MINUTE), it[0].tagsTimeStart)
            assertEquals(18, it[0].startTime.first().minute)
        }
        //    TODO add this week, next week ect
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



}