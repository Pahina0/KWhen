import ap.panini.kwhen.Parsed
import ap.panini.kwhen.TimeParser
import ap.panini.kwhen.configs.ENConfig
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class FuzzMassTest {

    private val zone = TimeZone.UTC
    private val anchorMillis = LocalDateTime(2025, 6, 11, 12, 0, 0)
        .toInstant(zone)
        .toEpochMilliseconds()

    private fun verifyUniversalInvariants(
        input: String,
        parsed: List<Parsed>,
        failures: MutableList<String>,
        context: String
    ) {
        var prevFirst = -1
        for ((idx, item) in parsed.withIndex()) {
            if (item.range.first < 0 || item.range.last >= input.length || item.range.first > item.range.last) {
                failures += "[$context] INVALID RANGE at index $idx on '$input': range=${item.range}, length=${input.length}"
                continue
            }
            val expectedSubstring = input.substring(item.range)
            if (item.text != expectedSubstring) {
                failures += "[$context] SUBSTRING MISMATCH at index $idx on '$input': text='${item.text}' vs substring='$expectedSubstring'"
            }
            if (item.startTime.isEmpty()) {
                failures += "[$context] EMPTY START_TIME at index $idx on '$input'"
            }
            if (item.range.first < prevFirst) {
                failures += "[$context] UNSORTED OUTPUT at index $idx on '$input': ${item.range.first} < $prevFirst"
            }
            prevFirst = item.range.first
        }
    }

    @Test
    fun testFuzzShuffledVocabularyAndNumbers() {
        val rng = Random(seed = 1337)
        val parser = TimeParser(ENConfig(relativeTo = anchorMillis, timeZone = zone))
        val failures = mutableListOf<String>()
        val totalIterations = 1000

        val vocab = listOf(
            "today", "tmrw", "yesterday", "now", "tonight", "morning", "afternoon", "evening",
            "night", "midnight", "noon", "monday", "tues", "fri", "sun", "jan", "feb.", "july",
            "december", "1st", "2nd", "3rd", "4th", "21st", "22nd", "first", "second", "third",
            "twentieth", "a", "an", "one", "two", "three", "half", "quarter", "second", "seconds",
            "minute", "mins", "hour", "hrs", "day", "days", "week", "wks", "month", "mos", "year",
            "yrs", "hourly", "daily", "weekly", "biweekly", "monthly", "yearly", "every",
            "every other", "recurring", "from", "on", "at", "during", "in", "after", "the", "next",
            "for", "to", "till", "until", "ends", "-", "/", ":", ",", "and", "&", "am", "pm",
            "0", "1", "2", "4", "6", "12", "13", "24", "25", "30", "31", "59", "60", "99", "2025"
        )

        repeat(totalIterations) { iter ->
            val tokenCount = rng.nextInt(3, 10)
            val separatorPool = listOf(" ", " ", " ", "  ", "-", "/", ":", ", ")
            val builder = StringBuilder()
            repeat(tokenCount) { idx ->
                if (idx > 0) builder.append(separatorPool[rng.nextInt(separatorPool.size)])
                builder.append(vocab[rng.nextInt(vocab.size)])
            }
            val input = builder.toString()

            val parsed = try {
                parser.parse(input)
            } catch (t: Throwable) {
                failures += "[VocabFuzz #$iter] UNHANDLED EXCEPTION on '$input': ${t::class.simpleName}: ${t.message}"
                return@repeat
            }

            verifyUniversalInvariants(input, parsed, failures, "VocabFuzz #$iter")
        }

        assertTrue(
            failures.isEmpty(),
            "Failed ${failures.size} / $totalIterations Vocabulary Fuzz cases:\n" +
                failures.take(30).joinToString("\n")
        )
    }

    @Test
    fun testFuzzAdversarialNumericAndBoundaryInputs() {
        val parser = TimeParser(ENConfig(relativeTo = anchorMillis, timeZone = zone))
        val failures = mutableListOf<String>()

        val adversarialInputs = listOf(
            "",
            "   ",
            "---",
            "///",
            ":::",
            "00:00",
            "00:59",
            "23:59",
            "24:00",
            "24:01",
            "25:99",
            "0am",
            "0pm",
            "12am",
            "12pm",
            "13am",
            "13pm",
            "99am",
            "99pm",
            "0/0/0",
            "00/00/00",
            "13/13/13",
            "31/31/31",
            "32/01/2025",
            "02/29/2025",
            "02/30/2024",
            "02/31/2024",
            "04/31/2025",
            "999999999999999999999",
            "in 999999999999999999999 hours",
            "for 999999999999999999999 days",
            "every 999999999999999999999 weeks",
            "every 3 half hours",
            "every other quarter second",
            "in a quarter of a second",
            "in half a second",
            "every 1989",
            "every 2025",
            "on a",
            "at an",
            "from to until",
            "every every other every",
            "and , & and",
            "1st 2nd 3rd 4th 5th 6th 7th 8th 9th 10th",
            "monday, tuesday, wednesday, thursday, friday, saturday, and sunday"
        )

        for ((idx, input) in adversarialInputs.withIndex()) {
            val parsed = try {
                parser.parse(input)
            } catch (t: Throwable) {
                failures += "[Adversarial #$idx] UNHANDLED EXCEPTION on '$input': ${t::class.simpleName}: ${t.message}"
                continue
            }

            verifyUniversalInvariants(input, parsed, failures, "Adversarial #$idx")
        }

        assertTrue(
            failures.isEmpty(),
            "Failed ${failures.size} / ${adversarialInputs.size} Adversarial Boundary cases:\n" +
                failures.take(30).joinToString("\n")
        )
    }

    @Test
    fun testFuzzUnicodeAndWhitespaceMutations() {
        val rng = Random(seed = 2026)
        val parser = TimeParser(ENConfig(relativeTo = anchorMillis, timeZone = zone))
        val failures = mutableListOf<String>()
        val totalIterations = 300

        val unicodePrefixes = listOf(
            "İ",
            "İstanbul ",
            "İzmir İznik ",
            "Straße ",
            "📅⏰ ",
            "日本語テキスト ",
            "العربية ",
            "\t\n\r ",
            "\"'",
            "(([[{{"
        )

        val corePhrases = listOf(
            "tomorrow at 4pm",
            "every Monday and Friday",
            "from 9am to 5pm",
            "on the 2nd of July",
            "in 3 hours",
            "on 04/13/2025"
        )

        repeat(totalIterations) { iter ->
            val prefix = unicodePrefixes[rng.nextInt(unicodePrefixes.size)].repeat(rng.nextInt(1, 4))
            val phrase = corePhrases[rng.nextInt(corePhrases.size)]
            val suffix = unicodePrefixes[rng.nextInt(unicodePrefixes.size)]
            val input = "$prefix $phrase $suffix"

            val parsed = try {
                parser.parse(input)
            } catch (t: Throwable) {
                failures += "[UnicodeFuzz #$iter] UNHANDLED EXCEPTION on '$input': ${t::class.simpleName}: ${t.message}"
                return@repeat
            }

            verifyUniversalInvariants(input, parsed, failures, "UnicodeFuzz #$iter")

            if (parsed.isEmpty()) {
                failures += "[UnicodeFuzz #$iter] FAILED TO PARSE core phrase '$phrase' inside '$input'"
            } else if (!parsed.first().text.trim().equals(phrase, ignoreCase = true)) {
                failures += "[UnicodeFuzz #$iter] CORRUPTED EXTRACTED TEXT on '$input': expected '$phrase', got '${parsed.first().text.trim()}'"
            }
        }

        assertTrue(
            failures.isEmpty(),
            "Failed ${failures.size} / $totalIterations Unicode & Whitespace Fuzz cases:\n" +
                failures.take(30).joinToString("\n")
        )
    }
}
