package ap.panini.kwhen.en.parsers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.common.parsers.ParserGenericNumbers
import ap.panini.kwhen.configs.ENConfig
import ap.panini.kwhen.en.amounts
import ap.panini.kwhen.util.matchAny

internal class ENGeneralAmount(override val config: ENConfig) : ParserGenericNumbers(config) {
    override val matchPattern: Regex
        get() = "${super.matchPattern}|${amounts.keys.matchAny()}(?:(?:\\s+an|a)?(?:\\s+-)?)?".toRegex()

    override fun onMatch(match: MatchResult): DateTime {
        return DateTime(generalNumber = amounts[match.value] ?: match.value.toDouble())
    }
}