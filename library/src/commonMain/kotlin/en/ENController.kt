package en

import common.Controller
import common.Merger
import common.MergerConsecutive
import common.Parser
import en.mergers.ENFiller
import en.mergers.ENRange
import en.parsers.*

class ENController(override val config: ENConfig = ENConfig()) : Controller(config) {
    override val parsers: List<Parser>
        get() = listOf(
            ENBasicDate(config),
            ENBasicTime(config),
            ENDayMonthYear(config),
            ENMonthDayYear(config),
            ENNumericOrdinal(config),
            ENDayOfWeek(config)
        )

    override val mergers: List<Merger>
        get() = listOf(
            ENFiller(),
            ENRange(),
            MergerConsecutive()
        )
}