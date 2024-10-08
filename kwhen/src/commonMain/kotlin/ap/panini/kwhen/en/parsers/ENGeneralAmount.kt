package ap.panini.kwhen.en.parsers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.common.parsers.ParserGenericNumbers
import ap.panini.kwhen.configs.ENConfig
import ap.panini.kwhen.en.amounts
import ap.panini.kwhen.util.matchAny

/**
 * En general amount finds generic numbers such as
 * 1, 2, three, four
 *^(?:\d+|a|one|two|three|four|five|six|seven|eight|nine|ten|half|quarter(?:(?:\s+an|a)?(?:\s+-)?)?)$
 * @property config
 * @constructor Create empty En general amount
 */
internal class ENGeneralAmount(override val config: ENConfig) : ParserGenericNumbers(config) {
    override val matchPattern: Regex
        get() = "${super.matchPattern}|${amounts.keys.matchAny()}(?:(?:\\s+an|a)?(?:\\s+-)?)?".toRegex()

    override fun onMatch(match: MatchResult): DateTime {
        return DateTime(
            generalNumber = amounts[match.value] ?: match.value.toDouble(),
            points = if (match.value == "a" || match.value == "an") 0 else 1
        )
    }
}