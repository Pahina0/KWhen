package ap.panini.kwhen.common

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.Parsed
import ap.panini.kwhen.TimeUnit
import ap.panini.kwhen.configs.Config
import ap.panini.kwhen.util.copy
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number
import kotlinx.datetime.plus

/**
 * Controller parses, merges, and finalizes times based on parsers and mergers given.
 *
 * @property config
 * @constructor Create empty Controller
 */
abstract class Controller(open val config: Config) {
    internal abstract val parsers: List<Parser>
    internal abstract val mergers: List<Merger>


    private fun String.lowercaseLengthPreserving(): String =
        CharArray(length) { this[it].lowercaseChar() }.concatToString()

    internal fun parse(input: String): List<DateTime> {
        val inputLc = input.lowercaseLengthPreserving()
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
                        input.substring(prevIndex + 1 until left.range.first).lowercaseLengthPreserving()
                    )

                val between = merger.betweenPattern.find(
                    input.substring(
                        left.range.last + 1 until (right?.range?.first ?: input.length)
                    ).lowercaseLengthPreserving()
                )


                // increment to the next merge
                ++i

                // sees if is possible to merge given data
                var merged = try {
                    merger.onMatch(left, right, prefix, between)?.copy(range = left.range)
                } catch (_: Exception) {
                    null
                }

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
                // random numbers or unmerged general time words with no tags
                if (date.tagsTimeStart.isEmpty() && date.tagsTimeEnd.isEmpty() && date.tagsDayOfWeek.isEmpty() && date.repeatTag == null && (date.generalTimeTag != null || date.generalNumber != null)) continue

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
                                    TimeUnit.DAY -> it.copy(dayOfMonth = date.startTime.day)
                                    TimeUnit.WEEK -> { it }
                                    TimeUnit.MONTH -> it.copy(monthNumber = date.startTime.month.number)
                                    TimeUnit.YEAR -> it.copy(year = date.startTime.year)
                                }
                            }
                        }
                    }

                    ret += mergeTo.copy(
                        range = mergeTo.range.first..date.range.last,
                        text = mergeTo.text + date.text.substring(mergeTo.range.last - date.range.first + 1),
                        startTime = st + mergedStartTime,
                        endTime = mergeTo.endTime ?: date.endTime,
                        tagsTimeStart = date.tagsTimeStart + mergeTo.tagsTimeStart,
                        tagsTimeEnd = mergeTo.tagsTimeEnd + date.tagsTimeEnd
                    )

                } else {
                    val whole = date.repeatTag?.unPartial(date.repeatOften ?: 0.0)
                    val st = if (date.tagsDayOfWeek.isNotEmpty()) date.tagsDayOfWeek.map {
                        dayOfWeek(
                            date.startTime, DayOfWeek.entries[it.ordinal]
                        )
                    } else listOf(date.startTime)
                    var et = date.endTime
                    if (et != null && st.isNotEmpty() && st.first() >= et) {
                        if (TimeUnit.WEEK in date.tagsTimeEnd) {
                            val nextStart = st.first().copy(date = st.first().date.plus(1, kotlinx.datetime.DateTimeUnit.DAY))
                            val resolvedEndDateTime = ap.panini.kwhen.util.resolveDayOfWeek(nextStart, et.dayOfWeek)
                            et = et.copy(date = resolvedEndDateTime.date)
                        }
                    }
                    ret += Parsed(
                        date.text,
                        date.range,
                        st,
                        et,
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

    private fun dayOfWeek(start: LocalDateTime, day: DayOfWeek): LocalDateTime =
        ap.panini.kwhen.util.resolveDayOfWeek(start, day)

}