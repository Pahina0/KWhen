package common

import DateTime
import kotlin.math.min

abstract class Controller(open val config: Config) {
    protected abstract val parsers: List<Parser>
    protected abstract val mergers: List<Merger>

    fun parse(input: String): List<DateTime> {
        val parsed = mutableListOf<DateTime>()

        parsers.forEach {
            var index = 0

            while (index < input.length) {
                val match = it.pattern.find(input, index) ?: break

                index = match.range.last + 1
                val matchPared = it.onMatch(match) ?: continue
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

    fun merge(input: String, parsed: List<DateTime>): List<DateTime> {
        var current: List<DateTime>
        var ret = parsed.toMutableList()

        println(parsed)

        mergers.forEach {
            current = ret
            ret = mutableListOf()
            println(it::class.simpleName)

            for (i in current.indices) {
                println("\n$i")

                // substrings from previous items last index or 0, to the beginning of the current item
                val prefixStart = min(
                    current.getOrNull(i - 1)?.range?.last?.plus(1) ?: 0, input.length - 1
                )
                val prefixEnd = current[i].range.first
                val prefix = if (prefixStart <= prefixEnd) {
                    input.substring(prefixStart..<prefixEnd)
                } else {
                    // adds to back, will only happen if previous is messed up which will be removed due to continue below
                    if (ret.isEmpty() || !(ret.last().range.first <= current[i].range.first && ret.last().range.last >= current[i].range.last)) {
                        ret += current[i]
                    }
                    continue
                }
                val prefixMatch = it.prefixPattern.find(prefix)

                println("prefix \"$prefix\", found ${prefixMatch?.value ?: "NOT FOUND"}")

                val betweenStart = min(current[i].range.last + 1, input.length)
                val betweenEnd = current.getOrNull(i + 1)?.range?.first ?: input.length
                val between = if (betweenStart <= betweenEnd) {
                    input.substring(betweenStart..<betweenEnd)
                } else {

                    // skips the current
                    continue
                }
                val betweenMatch = it.betweenPattern.find(between)

                println("between \"$between\", found ${betweenMatch?.value ?: "NOT FOUND"}")


                val date = it.onMatch(
                    current[i],
                    current.getOrNull(i + 1),
                    prefixMatch,
                    betweenMatch,
                )

                if (date == null) {
                    // only adds current if it's range is not in the last in ret range
                    if (ret.isEmpty() || !(ret.last().range.first <= current[i].range.first && ret.last().range.last >= current[i].range.last)) {
                        ret += current[i]
                    }

                    continue
                }

                println("MERGING")

                var merged = DateTime(
                    range = current[i].range
                )

                if (it.mergePrefixWithLeft && prefixMatch != null) {
                    val range = merged.range.first - prefixMatch.value.length..merged.range.last
                    merged = merged.copy(
                        range = range, text = input.substring(range)
                    )
                }


                if (it.mergeRightWithLeft && i + 1 < current.size) {
                    val range = merged.range.first..current[i + 1].range.last

                    merged = merged.copy(
                        range = range, text = input.substring(range)
                    )
                }

                println("DATE: $date")


                ret += date.copy(
                    text = merged.text, range = merged.range, points = date.points + it.reward
                )

            }
            println(ret)
        }



        return ret.mergeIntervals().cleanGenerics()
    }

    private fun List<DateTime>.cleanGenerics() =
        filter { it.generalNumber == null && it.generalTimeTag == null }

    private fun List<DateTime>.mergeIntervals(): List<DateTime> {
        val ret = mutableListOf<DateTime>()

        for (date in this) {
            if (ret.isNotEmpty() && date.range.first <= ret.last().range.last) {
                val mergeTo = ret.last()

                ret.removeLast()
                ret += mergeTo.merge(date).copy(
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