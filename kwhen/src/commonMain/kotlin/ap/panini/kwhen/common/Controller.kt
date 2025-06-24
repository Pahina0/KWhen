package ap.panini.kwhen.common

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.Parsed
import ap.panini.kwhen.TimeUnit
import ap.panini.kwhen.configs.Config
import ap.panini.kwhen.util.copy
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
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

    internal fun merge(input: String, parsed: List<DateTime>): List<List<DateTime>> {
        var processed = parsed.toMutableList()

        // tries to use every merger to merge 2 date times together
        for (merger in mergers) {
            val toProcess = processed
            processed = mutableListOf()

            // merges using current and next node
            var i = 0
            while (i < toProcess.size) {
                val left = toProcess[i]
                val right = toProcess.getOrNull(i + 1)
                val prevIndex = toProcess.getOrNull(i - 1)?.range?.last ?: -1

                // prevent out of bounds
                if (
                    prevIndex + 1 > left.range.first ||
                    left.range.last + 1 > (right?.range?.first ?: input.length)
                ) {
                    if (right == null) {
                        // this shouldn't happen?
                        ++i
                        processed += left
                        continue
                    }

                    val l = toProcess.toMutableList().apply { removeAt(i) }
                    val r = toProcess.toMutableList().apply { removeAt(i + 1) }
                    return merge(input, r) + merge(
                        input,
                        l
                    )
                }

                val prefix =
                    merger.prefixPattern.find(
                        input.substring(prevIndex + 1 until left.range.first).lowercase()
                    )

                val between = merger.betweenPattern.find(
                    input.substring(
                        left.range.last + 1 until (right?.range?.first ?: input.length)
                    ).lowercase()
                )


                // increment to the next merge
                ++i

                // sees if is possible to merge given data
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
        }

        return listOf(processed)
    }

    /**
     * The only things that should join are those with the same tags
     * */
    internal fun finalize(times: List<List<DateTime>>): List<Parsed> {
        val allRet = mutableListOf<Set<Parsed>>()

        for (curTimes in times) {
            val ret = mutableListOf<Parsed>()

            for (date in curTimes.filter { it.points != 0 }) {
                if (ret.isNotEmpty() && date.range.first <= ret.last().range.last + 1) {
                    val mergeTo = ret.removeAt(ret.size - 1)


                    val st = if (date.tagsDayOfWeek.isNotEmpty()) {
                        date.tagsDayOfWeek.map {
                            dayOfWeek(
                                date.startTime, DayOfWeek.entries[it.ordinal]
                            )
                        }
                    } else listOf(date.startTime)


                    // merges the tags, such as if one has hour/minute and the other doesn't
                    var mergedStartTime = mergeTo.startTime
                    for (tag in date.tagsTimeStart) {
                        if (tag !in mergeTo.tagsTimeStart) {
                            mergedStartTime = mergedStartTime.map {
                                when (tag) {
                                    TimeUnit.SECOND -> it.copy(second = date.startTime.second)
                                    TimeUnit.MINUTE -> it.copy(minute = date.startTime.minute)
                                    TimeUnit.HOUR -> it.copy(hour = date.startTime.hour)
                                    TimeUnit.DAY -> it.copy(dayOfMonth = date.startTime.dayOfMonth)
                                    TimeUnit.WEEK -> { it }
                                    TimeUnit.MONTH -> it.copy(monthNumber = date.startTime.monthNumber)
                                    TimeUnit.YEAR -> it.copy(year = date.startTime.year)
                                }
                            }
                        }
                    }

                    ret += mergeTo.copy(
                        range = mergeTo.range.first..date.range.last,
                        text = mergeTo.text + date.text.substring(mergeTo.range.last - date.range.first + 1),
                        startTime = st + mergedStartTime,
                        tagsTimeStart = date.tagsTimeStart + mergeTo.tagsTimeStart
                    )

                } else {
                    // random numbers with no meaning
                    if (date.generalTimeTag == null && date.generalNumber != null) continue

                    val whole = date.repeatTag?.unPartial(date.repeatOften ?: 0.0)
                    ret += Parsed(
                        date.text,
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
            allRet += ret.toSet()
        }
        return allRet.reduce { l, r -> l union r }.sortedBy { it.range.first }
    }

    private fun dayOfWeek(start: LocalDateTime, day: DayOfWeek): LocalDateTime {
        var from = start.toInstant(config.timeZone)
        repeat(7) {
            val time = from.toLocalDateTime(config.timeZone)
            if (time.dayOfWeek == day) return time

            from += 24.0.hours
        }

        return from.toLocalDateTime(config.timeZone)
    }

}