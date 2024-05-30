package en.parsers

import DateTime
import TagTime
import common.Config
import common.ParserByWord
import en.weekdays
import util.matchAny

class ENDayOfWeek(override val config: Config) : ParserByWord(config) {
    override val matchPattern: Regex
        get() = weekdays.keys.matchAny()

    override fun onMatch(match: MatchResult): DateTime = DateTime(
        tagsDayOfWeek = setOf(weekdays[match.value]!!),
        tagsTimeStart = setOf(TagTime.DAY_OF_WEEK)
    )

}