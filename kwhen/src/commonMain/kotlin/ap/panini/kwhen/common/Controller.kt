package ap.panini.kwhen.common

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.Parsed
import ap.panini.kwhen.configs.Config
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.min
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
        val mInput = input.lowercase()
        var ret = parsed.sortedBy { it.range.first }.toMutableList()

        mergers.forEach { merger ->
            //// don't need to merge if nothing to merge
            //if (ret.size <= 1) return@forEach

            //println(merger::class.simpleName)
            //println(ret)
            //println()
            var index = 0
            while (index < ret.size) {
                val cur = ret[index]
                val prev = ret.getOrNull(index - 1)
                val next = ret.getOrNull(index + 1)

                // either gets where the last on the previous one or maxes at length
                val prefixStart = min(
                    prev?.range?.last?.plus(1) ?: 0, mInput.length - 1
                )

                // prefix ends at where the current one starts
                val prefixEnd = cur.range.first

                //val prefix = if (prefixStart <= prefixEnd) {
                //    mInput.substring(prefixStart..<prefixEnd)
                //} else {
                //    return@forEachIndexed
                //}
                val prefix = if (prefixStart <= prefixEnd) {
                    mInput.substring(prefixStart..<prefixEnd)
                } else {
                    ++index
                    continue
                }

                val betweenStart = min(cur.range.last + 1, mInput.length)
                val betweenEnd = next?.range?.first ?: mInput.length
                //val between = if (betweenStart <= betweenEnd) {
                //    mInput.substring(betweenStart..<betweenEnd)
                //} else {
                //    // skips the current
                //    return@forEachIndexed
                //}
                val between = if (betweenStart <= betweenEnd) {
                    mInput.substring(betweenStart..<betweenEnd)
                } else {
                    ++index
                    continue
                }

                val prefixMatch = merger.prefixPattern.find(prefix)
                val betweenMatch = merger.betweenPattern.find(between)

                println("NEXT $next")
                val mergedIncomplete = merger.onMatch(
                    cur,
                    next,
                    prefixMatch,
                    betweenMatch,
                )

                if (mergedIncomplete == null) {
                    ++index
                    continue
                }


                var merged = DateTime(
                    range = cur.range
                )

                var betweenStartIndex = merged.range.last + 1
                var rightWithLeftStartIndex = merged.range.first

                if (merger.mergePrefixWithLeft && prefixMatch != null) {
                    val range = merged.range.first - prefixMatch.value.length..merged.range.last
                    merged = merged.copy(
                        range = range, text = mInput.substring(range)
                    )

                    rightWithLeftStartIndex = merged.range.first
                }

                if (merger.mergeRightWithLeft && index + 1 < ret.size) {
                    val range = rightWithLeftStartIndex..next!!.range.last

                    merged = merged.copy(
                        range = range, text = mInput.substring(range)
                    )

                    betweenStartIndex = merged.range.first
                }

                if (merger.mergeBetweenWithRight && index + 1 < ret.size) {
                    val range = betweenStartIndex..next!!.range.last
                    merged = merged.copy(
                        range = range, text = mInput.substring(range)
                    )
                    ret += cur
                }

                println("MERGED: $merged")

                ret[index] = mergedIncomplete.copy(
                    text = merged.text,
                    range = merged.range,
                    points = mergedIncomplete.points + merger.reward
                )

                //println("OUT: ${ret[index]}")
                println("??? $ret")
                println("index is $index")
                //++index


            }

            println(ret)
            ret = ret.mergeIntervals().toMutableList()
            //println("RET IS NOW $ret\n")
        }
        return ret
    }
    //internal fun merge(input: String, parsed: List<DateTime>): List<DateTime> {
    //    val inputLc = input.lowercase()
    //    var current: List<DateTime>
    //    var ret = parsed.toMutableList()
    //
    //
    //    mergers.forEach {
    //        current = ret
    //        ret = mutableListOf()
    //
    //        //println(it::class.simpleName)
    //        //println(current)
    //        //println()
    //
    //        for (i in current.indices) {
    //
    //            // substrings from previous items last index or 0, to the beginning of the current item
    //            val prefixStart = min(
    //                current.getOrNull(i - 1)?.range?.last?.plus(1) ?: 0, inputLc.length - 1
    //            )
    //            val prefixEnd = current[i].range.first
    //            val prefix = if (prefixStart <= prefixEnd) {
    //                inputLc.substring(prefixStart..<prefixEnd)
    //            } else {
    //                // adds to back, will only happen if previous is messed up which will be removed due to continue below
    //                if (ret.isEmpty() || !(ret.last().range.first <= current[i].range.first && ret.last().range.last >= current[i].range.last)) {
    //                    ret += current[i]
    //                }
    //                continue
    //            }
    //            val prefixMatch = it.prefixPattern.find(prefix)
    //
    //            val betweenStart = min(current[i].range.last + 1, inputLc.length)
    //            val betweenEnd = current.getOrNull(i + 1)?.range?.first ?: inputLc.length
    //            val between = if (betweenStart <= betweenEnd) {
    //                inputLc.substring(betweenStart..<betweenEnd)
    //            } else {
    //
    //                // skips the current
    //                continue
    //            }
    //            val betweenMatch = it.betweenPattern.find(between)
    //
    //            val date = try {
    //                it.onMatch(
    //                    current[i],
    //                    current.getOrNull(i + 1),
    //                    prefixMatch,
    //                    betweenMatch,
    //                )
    //            } catch (e: Exception) {
    //                // only adds current if it's range is not in the last in ret range
    //                if (ret.isEmpty() || !(ret.last().range.first <= current[i].range.first && ret.last().range.last >= current[i].range.last)) {
    //                    ret += current[i]
    //                }
    //                continue
    //            }
    //
    //            if (date == null) {
    //                // only adds current if it's range is not in the last in ret range
    //                if (ret.isEmpty() || !(ret.last().range.first <= current[i].range.first && ret.last().range.last >= current[i].range.last)) {
    //                    ret += current[i]
    //                }
    //
    //                continue
    //            }
    //
    //            var merged = DateTime(
    //                range = current[i].range
    //            )
    //
    //            if (it.mergePrefixWithLeft && prefixMatch != null) {
    //                val range = merged.range.first - prefixMatch.value.length..merged.range.last
    //                merged = merged.copy(
    //                    range = range, text = inputLc.substring(range)
    //                )
    //            }
    //
    //
    //            if (it.mergeRightWithLeft && i + 1 < current.size) {
    //                val range = merged.range.first..current[i + 1].range.last
    //
    //                merged = merged.copy(
    //                    range = range, text = inputLc.substring(range)
    //                )
    //            }
    //
    //            if (it.mergeBetweenWithRight && i + 1 < current.size) {
    //                val range = merged.range.last + 1..current[i + 1].range.last
    //                merged = merged.copy(
    //                    range = range, text = inputLc.substring(range)
    //                )
    //                ret += current[i]
    //            }
    //
    //
    //            ret += date.copy(
    //                text = merged.text, range = merged.range, points = date.points + it.reward
    //            )
    //        }
    //
    //        ret = ret.mergeIntervals().toMutableList()
    //    }
    //
    //    return ret.cleanGenerics()
    //}

    /**
     * The only things that should join are those with the same tags
     * */
    internal fun finalize(times: List<DateTime>): List<Parsed> {
        val ret = mutableListOf<Parsed>()

        for (date in times) {
            if (ret.isNotEmpty() && date.range.first <= ret.last().range.last + 1) {
                val mergeTo = ret.last()


                val st = if (date.tagsDayOfWeek.isNotEmpty()) {
                    date.tagsDayOfWeek.map {
                        dayOfWeek(
                            date.startTime,
                            DayOfWeek.entries[it.ordinal]
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
                ret += Parsed(
                    date.text,
                    date.range,
                    if (date.tagsDayOfWeek.isNotEmpty()) date.tagsDayOfWeek.map {
                        dayOfWeek(
                            date.startTime,
                            DayOfWeek.entries[it.ordinal]
                        )
                    } else listOf(date.startTime),
                    date.endTime,
                    date.tagsTimeStart,
                    date.tagsTimeEnd,
                    whole?.first,
                    whole?.second
                )
            }
        }

        return ret
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
    private fun List<DateTime>.cleanGenerics() =
        filter {
            (it.generalNumber == null && it.generalTimeTag == null) ||
                    (it.startTime != DateTime.nowZeroed || it.endTime != null)
        }

    private fun List<DateTime>.mergeIntervals(): List<DateTime> {
        val ret = mutableListOf<DateTime>()
        val sorted = this.sortedBy { it.range.first }

        for (date in sorted) {
            if (ret.isNotEmpty()
                && date.range.first <= ret.last().range.last
                && date.range.last != ret.last().range.last
            ) {
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