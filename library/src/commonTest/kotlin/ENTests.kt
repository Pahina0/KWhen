import en.ENConfig
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import util.copy
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


class ENTests {
    private lateinit var timeParser: TimeParser

    @BeforeTest
    fun setup() {
        timeParser = TimeParser()
    }


    @Test
    fun testBasicDate() {
        timeParser.parse("today i will go swim").let {
            assertEquals("today", it[0].text)
            assertEquals(setOf(TagTime.DAY), it[0].tagsTimeStart)
        }

        timeParser.parse("it will be a great day tmrw").let {
            println(it)
            assertEquals("tmrw", it[0].text)
            assertEquals(setOf(TagTime.DAY), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(date.plus(1, DateTimeUnit.DAY)) }, it[0].startTime)
        }



        timeParser.parse("it is like yesterday's weather today").let {
            assertEquals("yesterday", it[0].text)
            assertEquals(setOf(TagTime.DAY), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(date.minus(1, DateTimeUnit.DAY)) }, it[0].startTime)
        }
    }

    @Test
    fun testBasicTime() {
        timeParser.parse("i will swim in the afternoon").let {
            assertEquals("afternoon", it[0].text)
            assertEquals(setOf(TagTime.HOUR), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(hour = 15) }, it[0].startTime)

            // assertEquals(DateTime().startTime.apply { set(Calendar.HOUR_OF_DAY, 15) }, it[0].startTime)
        }

        timeParser.parse("the morning was nice and cool").let {
            assertEquals("morning", it[0].text)
            assertEquals(setOf(TagTime.HOUR), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(hour = 9) }, it[0].startTime)
        }

        timeParser.parse("tonight was kind of boring").let {
            assertEquals("tonight", it[0].text)
            assertEquals(setOf(TagTime.HOUR, TagTime.DAY), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(hour = 20) }, it[0].startTime)
        }

        TimeParser(ENConfig(afternoon = 14)).parse("i will go run in the afternoon today").let {
            assertEquals("afternoon", it[0].text)
            assertEquals(setOf(TagTime.HOUR, TagTime.HOUR), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(hour = 14) }, it[0].startTime)
            println(it)
        }
    }

    @Test
    fun testDayMonthYear() {
        timeParser.parse("12 june 07 was an extremely hot day").let {
            assertEquals("12 june 07", it[0].text)
            assertEquals(setOf(TagTime.DAY, TagTime.MONTH, TagTime.YEAR), it[0].tagsTimeStart)
            assertEquals(
                DateTime().startTime.run { copy(year = 2007, monthNumber = 6, dayOfMonth = 12) },
                it[0].startTime
            )
        }

        timeParser.parse("it will be a blast on the 4th of july").let {
            assertEquals("4th of july", it[0].text)
            assertEquals(setOf(TagTime.DAY, TagTime.MONTH), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(monthNumber = 7, dayOfMonth = 4) }, it[0].startTime)
        }

        timeParser.parse("5 aug. 1448 stuff happened").let {
            assertEquals("5 aug. 1448", it[0].text)
            assertEquals(setOf(TagTime.DAY, TagTime.MONTH, TagTime.YEAR), it[0].tagsTimeStart)
            assertEquals(
                DateTime().startTime.run { copy(year = 1448, monthNumber = 8, dayOfMonth = 5) },
                it[0].startTime
            )
        }

        timeParser.parse("18 sep 76 there was (not) a battle").let {
            assertEquals("18 sep 76", it[0].text)
            assertEquals(setOf(TagTime.DAY, TagTime.MONTH, TagTime.YEAR), it[0].tagsTimeStart)
            assertEquals(
                DateTime().startTime.run { copy(year = 1976, monthNumber = 9, dayOfMonth = 18) },
                it[0].startTime
            )
        }

        timeParser.parse("a bird crashed on the 1st of    feb").let {
            assertEquals("1st of    feb", it[0].text)
            assertEquals(setOf(TagTime.DAY, TagTime.MONTH), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(monthNumber = 2, dayOfMonth = 1) }, it[0].startTime)
        }
    }

    @Test
    fun testMonthDayYear() {
        timeParser.parse(" june  12 07 was an extremely hot day").let {
            println(it)
            assertEquals("june  12 07", it[0].text)
            assertEquals(setOf(TagTime.DAY, TagTime.MONTH, TagTime.YEAR), it[0].tagsTimeStart)
            assertEquals(
                DateTime().startTime.run { copy(year = 2007, monthNumber = 6, dayOfMonth = 12) },
                it[0].startTime
            )
        }

        timeParser.parse("it will be a blast on the jul 4th ").let {
            assertEquals("jul 4th", it[0].text)
            assertEquals(setOf(TagTime.DAY, TagTime.MONTH), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(monthNumber = 7, dayOfMonth = 4) }, it[0].startTime)
        }

        timeParser.parse("aug. 5 1448 stuff happened").let {
            assertEquals("aug. 5 1448", it[0].text)
            assertEquals(setOf(TagTime.DAY, TagTime.MONTH, TagTime.YEAR), it[0].tagsTimeStart)
            assertEquals(
                DateTime().startTime.run { copy(year = 1448, monthNumber = 8, dayOfMonth = 5) },
                it[0].startTime
            )
        }

        timeParser.parse(" sep-18 76 there was (not) a battle").let {
            assertEquals("sep-18 76", it[0].text)
            assertEquals(setOf(TagTime.DAY, TagTime.MONTH, TagTime.YEAR), it[0].tagsTimeStart)
            assertEquals(
                DateTime().startTime.run { copy(year = 1976, monthNumber = 9, dayOfMonth = 18) },
                it[0].startTime
            )
        }

        timeParser.parse("a bird crashed on the feb/1").let {
            assertEquals("feb/1", it[0].text)
            assertEquals(setOf(TagTime.DAY, TagTime.MONTH), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(monthNumber = 2, dayOfMonth = 1) }, it[0].startTime)
        }

        timeParser.parse("04/13/05").let {
            assertEquals("04/13/05", it[0].text)
            assertEquals(setOf(TagTime.DAY, TagTime.MONTH, TagTime.YEAR), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(year = 2005, monthNumber = 4, dayOfMonth = 13) }, it[0].startTime)
        }

        timeParser.parse("4-13-05").let {
            assertEquals("4-13-05", it[0].text)
            assertEquals(setOf(TagTime.DAY, TagTime.MONTH, TagTime.YEAR), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(year = 2005, monthNumber = 4, dayOfMonth = 13) }, it[0].startTime)
        }

        timeParser.parse("i'm going to leave in december").let {
            assertEquals("december", it[0].text)
            assertEquals(setOf(TagTime.MONTH), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy( monthNumber = 12) }, it[0].startTime)
        }

    }

    @Test
    fun testNumericOrdinal() {
        timeParser.parse("im going to swim at 3am").let {
            assertEquals("3am", it[0].text)
            assertEquals(setOf(TagTime.HOUR, TagTime.MINUTE), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(hour = 3, minute = 0) }, it[0].startTime)
        }

        timeParser.parse("im going to swim at 03:25pm").let {
            assertEquals("03:25pm", it[0].text)
            assertEquals(setOf(TagTime.HOUR, TagTime.MINUTE), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(hour = 15, minute = 25) }, it[0].startTime)

        }


        TimeParser(ENConfig(use24 = true)).parse("the fourth will be crazy").let {
            assertEquals("fourth", it[0].text)
            assertEquals(setOf(TagTime.DAY), it[0].tagsTimeStart)
            assertEquals(DateTime().startTime.run { copy(dayOfMonth = 4) }, it[0].startTime)

        }
    }

    @Test
    fun testDayOfWeek() {
        TimeParser(ENConfig(use24 = true)).parse("i do it every mon, tues, and friday").let {
            assertEquals("mon", it[0].text)
            assertEquals("tues", it[1].text)
            assertEquals("friday", it[2].text)
            assertEquals(setOf(TagTime.DAY_OF_WEEK), it[0].tagsTimeStart)
            assertEquals(setOf(TagDayOfWeek.MONDAY), it[0].tagsDayOfWeek)
            assertEquals(setOf(TagDayOfWeek.TUESDAY), it[1].tagsDayOfWeek)
            assertEquals(setOf(TagDayOfWeek.FRIDAY), it[2].tagsDayOfWeek)

        }
    }


    @Test
    fun testFillerEndMerge() {
        timeParser.parseAndMerge("i will go party from the 4th to 18th").let {
            assertEquals("from the 4th to 18th", it[0].text.trim())
            assertEquals(DateTime().startTime.run { copy(dayOfMonth = 4) }, it[0].startTime)
            assertEquals(DateTime().startTime.run { copy(dayOfMonth = 18) }, it[0].endTime)
            assertEquals(setOf(TagTime.DAY), it[0].tagsTimeStart)
            assertEquals(setOf(TagTime.DAY), it[0].tagsTimeEnd)
        }

        timeParser.parseAndMerge("i have nothing to do today till tmrw").let {
            assertEquals("today till tmrw", it[0].text.trim())
            assertEquals(DateTime().startTime, it[0].startTime)
            assertEquals(DateTime().startTime.run { copy(date.plus(1, DateTimeUnit.DAY)) }, it[0].endTime)
            assertEquals(setOf(TagTime.DAY), it[0].tagsTimeStart)
            assertEquals(setOf(TagTime.DAY), it[0].tagsTimeEnd)
        }

        timeParser.parseAndMerge("in the morning i will go eat until the night").let {
            assertEquals("in the morning", it[0].text.trim())
            assertEquals(DateTime().startTime.copy(hour = 9), it[0].startTime)
            assertEquals(setOf(TagTime.HOUR), it[0].tagsTimeStart)

            assertEquals("until the night", it[1].text.trim())
            assertEquals(DateTime().startTime.copy(hour = 20), it[1].endTime)
            assertEquals(setOf(TagTime.HOUR), it[1].tagsTimeEnd)
        }
    }

    @Test
    fun testGenericAndMerge() {
        TimeParser(config = ENConfig(use24 = true)).parseAndMerge("im busy from 4 to 6").let {
            assertEquals("from 4 to 6", it[0].text.trim())
            assertEquals(DateTime().startTime.run { copy(hour = 4, minute = 0) }, it[0].startTime)
            assertEquals(DateTime().startTime.run { copy(hour = 6, minute = 0) }, it[0].endTime)
            assertEquals(setOf(TagTime.MINUTE, TagTime.HOUR), it[0].tagsTimeStart)
            assertEquals(setOf(TagTime.MINUTE, TagTime.HOUR), it[0].tagsTimeEnd)
        }

        timeParser.parseAndMerge("the diving team will go on a field trip in 18 months").let {
            assertEquals("in 18 months", it[0].text.trim())
            assertEquals(DateTime().startTime.run { copy(monthNumber = monthNumber + 18) }, it[0].startTime)
            assertEquals(setOf(TagTime.MONTH), it[0].tagsTimeStart)
        }

        timeParser.parseAndMerge("im going to japan 06/18 - dec 2025").let {
            assertEquals("06/18 - dec 2025", it[0].text.trim())
            assertEquals(DateTime().startTime.run { copy(monthNumber = 6, dayOfMonth = 18) }, it[0].startTime)
            assertEquals(DateTime().startTime.run { copy(monthNumber = 12, year = 2025) }, it[0].endTime)
            assertEquals(setOf(TagTime.MONTH, TagTime.DAY), it[0].tagsTimeStart)
            assertEquals(setOf(TagTime.MONTH, TagTime.YEAR), it[0].tagsTimeEnd)
        }

    }
}