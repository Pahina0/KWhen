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

        return parsed.sorted()
    }

}