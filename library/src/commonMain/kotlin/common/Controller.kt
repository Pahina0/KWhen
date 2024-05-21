package common

import DateTime

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

            for (i in current.indices) {
                println("\n$i")

                // substrings from previous items last index or 0, to the beginning of the current item
                val left =
                    input.substring(
                        (current.getOrNull(i - 1)?.range?.last?.plus(1) ?: 0)
                                ..<current[i].range.first)
                val leftMatch = it.prefixPattern.find(left)

                println("Left $left, found ${leftMatch?.value ?: "NOT FOUND"}")

                val between = input.substring(
                    current[i].range.last + 1..<(current.getOrNull(i + 1)?.range?.first ?: input.length)
                )
                val betweenMatch = it.betweenPattern.find(between)

                println("between $between, found ${betweenMatch?.value ?: "NOT FOUND"}")


                val date = it.onMatch(
                    if (i - 1 < 0) null else current[i - 1],
                    if (i >= current.size) null else current[i],
                    leftMatch,
                    betweenMatch,
                )

                if (date == null) {
                    ret += current[i]

                    continue
                }

                println("MERGING")

                var merged = current[i]

                if (it.mergePrefixWithLeft && leftMatch != null) {
                    merged = merged.copy(
                        range = merged.range.first - leftMatch.value.length..merged.range.last,
                        text = leftMatch.value + merged.text
                    )
                }

                if (it.mergeBetweenWithLeft && betweenMatch != null) {
                    merged = merged.copy(
                        range = merged.range.first..merged.range.last + betweenMatch.value.length,
                        text = merged.text + betweenMatch.value
                    )
                }

                if (it.mergeRightWithLeft && i + 1 < current.size) {
                    if (!it.mergeBetweenWithLeft) {
                        throw RuntimeException("Merge right with left must also merge between with left")
                    }

                    merged = merged.copy(
                        range = merged.range.first..current[i + 1].range.last,
                        text = merged.text + current[i + 1].text
                    )
                }

                ret += merged.merge(date)


            }
        }



        return ret
    }
}