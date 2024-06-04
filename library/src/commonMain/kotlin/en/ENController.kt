package en

import common.Controller
import common.Merger
import common.Parser
import common.mergers.MergerConsecutive
import common.mergers.MergerGeneralTags
import en.mergers.ENBegin
import en.mergers.ENEnd
import en.mergers.ENRepeat
import en.parsers.ENBasicDate
import en.parsers.ENBasicTime
import en.parsers.ENDayMonthYear
import en.parsers.ENDayOfWeek
import en.parsers.ENGeneralTime
import en.parsers.ENMonthDayYear
import en.parsers.ENGeneralAmount
import en.parsers.ENNumericOrdinal

class ENController(override val config: ENConfig = ENConfig()) : Controller(config) {
    override val parsers: List<Parser>
        get() = listOf(
            ENGeneralAmount(config),
            ENBasicDate(config),
            ENBasicTime(config),
            ENDayMonthYear(config),
            ENMonthDayYear(config),
            ENNumericOrdinal(config),
            ENDayOfWeek(config),
            ENGeneralTime(config)
        )

    override val mergers: List<Merger>
        get() = listOf(
            MergerGeneralTags(config),
            ENRepeat(config),
            ENBegin(config),
            ENEnd(config),
            MergerConsecutive(config)
        )
}