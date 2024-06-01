package en

import common.Controller
import common.Merger
import common.mergers.MergerConsecutive
import common.mergers.MergerGeneralTags
import common.Parser
import common.parsers.ParserGenericNumbers
import en.mergers.ENBegin
import en.mergers.ENEnd
import en.parsers.*

class ENController(override val config: ENConfig = ENConfig()) : Controller(config) {
    override val parsers: List<Parser>
        get() = listOf(
            ENBasicDate(config),
            ENBasicTime(config),
            ENDayMonthYear(config),
            ENMonthDayYear(config),
            ENNumericOrdinal(config),
            ENDayOfWeek(config),
            ENGeneralTime(config),
            ParserGenericNumbers(config)
        )

    override val mergers: List<Merger>
        get() = listOf(
            MergerGeneralTags(config),
            ENBegin(config),
            ENEnd(config),
            MergerConsecutive(config)
        )
}