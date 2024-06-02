package en.parsers

import DateTime
import common.parsers.ParserGenericNumbers
import en.ENConfig
import en.amounts
import util.matchAny

class ENGeneralAmount(override val config: ENConfig) : ParserGenericNumbers(config) {
    override val matchPattern: Regex
        get() = "${super.matchPattern}|${amounts.keys.matchAny()}".toRegex()

    override fun onMatch(match: MatchResult): DateTime {
        return DateTime(generalNumber = amounts[match.value] ?: match.value.toInt())
    }
}