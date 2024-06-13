package en.parsers

import DateTime
import TimeUnit
import configs.Config
import common.parsers.ParserByWord
import en.weekdays
import util.matchAny

internal class ENDayOfWeek(override val config: Config) : ParserByWord(config) {
    override val matchPattern: Regex
        get() = weekdays.keys.matchAny()

    override fun onMatch(match: MatchResult): DateTime = DateTime(
        tagsDayOfWeek = setOf(weekdays[match.value]!!),
        tagsTimeStart = setOf(TimeUnit.WEEK)
    )

}