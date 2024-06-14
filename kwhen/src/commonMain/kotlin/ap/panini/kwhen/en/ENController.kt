package ap.panini.kwhen.en

import ap.panini.kwhen.common.Merger
import ap.panini.kwhen.configs.ENConfig
import ap.panini.kwhen.common.Parser
import ap.panini.kwhen.common.mergers.MergerConsecutive
import ap.panini.kwhen.common.mergers.MergerGeneralTags
import ap.panini.kwhen.en.mergers.ENBegin
import ap.panini.kwhen.en.mergers.ENEnd
import ap.panini.kwhen.en.mergers.ENList
import ap.panini.kwhen.en.mergers.ENRepeat
import ap.panini.kwhen.en.parsers.ENBasicDate
import ap.panini.kwhen.en.parsers.ENBasicTime
import ap.panini.kwhen.en.parsers.ENDayMonthYear
import ap.panini.kwhen.en.parsers.ENDayOfWeek
import ap.panini.kwhen.en.parsers.ENGeneralAmount
import ap.panini.kwhen.en.parsers.ENGeneralTime
import ap.panini.kwhen.en.parsers.ENMonthDayYear
import ap.panini.kwhen.en.parsers.ENNumericOrdinal

internal class ENController  (override val config: ENConfig = ENConfig()) :
    ap.panini.kwhen.common.Controller(config) {

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
            MergerConsecutive(config),
            ENList(config)
        )
}