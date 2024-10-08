package ap.panini.kwhen.common

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.Parsed
import ap.panini.kwhen.configs.Config
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours

/**
 * Controller parses, merges, and finalizes times based on parsers and mergers given.
 *
 * @property config
 * @constructor Create empty Controller
 */
abstract class Controller(open val config: Config) {
    internal abstract val parsers: List<Parser>
    internal abstract val mergers: List<Merger>


    internal fun parse(input: String): List<DateTime> {
        val inputLc = input.lowercase()
        val parsed = mutableListOf<DateTime>()

        parsers.forEach {
            var index = 0

            while (index < inputLc.length) {
                val match = it.pattern.find(inputLc, index) ?: break

                index = match.range.last + 1
                val matchPared = try {
                    it.onMatch(match)
                } catch (_: Exception) {
                    continue
                } ?: continue
                parsed += matchPared.copy(text = match.value, range = match.range)
            }
        }

        return mergeRange(parsed.sorted())
    }

    private fun mergeRange(parsed: List<DateTime>): List<DateTime> {
        val ret = mutableListOf<DateTime>()
        parsed.forEach { date ->
            if (ret.isNotEmpty() && date.range.last <= ret.last().range.last) return@forEach

            ret += date
        }

        return ret
    }

    internal fun merge(input: String, parsed: List<DateTime>): List<DateTime> {
        println(input)
        println()


        var processed = parsed.toMutableList()

        // tries to use every merger to merge 2 date times together
        for (merger in mergers) {
            println("\n${merger::class.simpleName}")
            val toProcess = processed
            processed = mutableListOf()


            // merges using current and next node
            var i = 0
            while (i < toProcess.size) {
                println(i)
                println(toProcess.joinToString("\n") + "\n\n")
                val left = toProcess[i]
                val right = toProcess.getOrNull(i + 1)
                val prevIndex = toProcess.getOrNull(i - 1)?.range?.last ?: -1

                val prefix = merger.prefixPattern.find(
                    input.substring(prevIndex + 1 until left.range.first).lowercase()
                )

                val between = merger.betweenPattern.find(
                    input.substring(
                        left.range.last + 1 until (right?.range?.first ?: input.length)
                    ).lowercase()
                )


                // increment to the next merge
                ++i

                println("LEFT: $left")
                println("RIGHT: $right")
                println("PREFIX: ${prefix?.groups}")
                println("BETWEEN: ${between?.groups}")

                var merged = merger.onMatch(left, right, prefix, between)?.copy(range = left.range)

                if (merged == null) {
                    processed += left
                    continue
                }

                if (merger.mergePrefixWithLeft && prefix != null) {
                    val range = merged.range.first - prefix.value.length..merged.range.last
                    merged = merged.copy(
                        range = range, text = input.substring(range)
                    )
                }

                if (merger.mergeRightWithLeft == Merger.BetweenMergeOption.PREFIX_MERGE && between != null && right != null) {
                    val range = merged.range.first until right.range.first
                    merged = merged.copy(
                        range = range, text = input.substring(range)
                    )

                }

                if (merger.mergeRightWithLeft == Merger.BetweenMergeOption.FULL_MERGE && between != null && right != null) {
                    val range = merged.range.first..right.range.last
                    merged = merged.copy(
                        range = range, text = input.substring(range)
                    )
                    toProcess.removeAt(i--)
                    toProcess[i] = merged
                } else {
                    processed += merged
                }

            }
            println("FINAL: $processed")
        }

        return processed.also {
            println("FINAL: $it")
        }
    }

    /**
     * The only things that should join are those with the same tags
     * */
    internal fun finalize(times: List<DateTime>): List<Parsed> {
        val ret = mutableListOf<Parsed>()

        println("\n\n$times")
        for (date in times.filter { it.points != 0 }) {
            if (ret.isNotEmpty() && date.range.first <= ret.last().range.last + 1) {
                val mergeTo = ret.last()


                val st = if (date.tagsDayOfWeek.isNotEmpty()) {
                    date.tagsDayOfWeek.map {
                        dayOfWeek(
                            date.startTime, DayOfWeek.entries[it.ordinal]
                        )
                    }
                } else listOf(date.startTime)


                ret.removeLast()
                ret += mergeTo.copy(
                    range = mergeTo.range.first..date.range.last,
                    text = mergeTo.text + date.text.substring(mergeTo.range.last - date.range.first + 1),
                    startTime = st + mergeTo.startTime
                )
            } else {
                val whole = date.repeatTag?.unPartial(date.repeatOften ?: 0.0)
                ret += Parsed(date.text,
                    date.range,
                    if (date.tagsDayOfWeek.isNotEmpty()) date.tagsDayOfWeek.map {
                        dayOfWeek(
                            date.startTime, DayOfWeek.entries[it.ordinal]
                        )
                    } else listOf(date.startTime),
                    date.endTime,
                    date.tagsTimeStart,
                    date.tagsTimeEnd,
                    whole?.first,
                    whole?.second)
            }
        }

        return ret.also { println(it) }
    }

    private fun dayOfWeek(start: LocalDateTime, day: DayOfWeek): LocalDateTime {
        var from = start.toInstant(TimeZone.currentSystemDefault())
        for (i in 0..7) {
            val time = from.toLocalDateTime(TimeZone.currentSystemDefault())
            if (time.dayOfWeek == day) return time

            from += 24.0.hours
        }

        return from.toLocalDateTime(TimeZone.currentSystemDefault())
    }

    /**
     * keep if no generic number and time tag
     * if there are, if start/end time changed, then keep
     */
    private fun List<DateTime>.cleanGenerics() = filter {
        (it.generalNumber == null && it.generalTimeTag == null) || (it.startTime != DateTime.nowZeroed || it.endTime != null)
    }

    private fun List<DateTime>.mergeIntervals(): List<DateTime> {
        val ret = mutableListOf<DateTime>()
        val sorted = this.sortedBy { it.range.first }

        for (date in sorted) {
            if (ret.isNotEmpty() && date.range.first <= ret.last().range.last && date.range.last != ret.last().range.last) {
                val mergeTo = ret.last()

                ret[ret.size - 1] = mergeTo.merge(date).copy(
                    range = mergeTo.range.first..date.range.last,
                    text = mergeTo.text + date.text.substring(mergeTo.range.last - date.range.first + 1)
                )
            } else {
                ret += date
            }
        }

        return ret
    }
}