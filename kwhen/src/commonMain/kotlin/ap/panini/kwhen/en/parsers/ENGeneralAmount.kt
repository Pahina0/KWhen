package ap.panini.kwhen.en.parsers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.common.parsers.ParserGenericNumbers
import ap.panini.kwhen.configs.ENConfig
import ap.panini.kwhen.en.amounts
import ap.panini.kwhen.util.matchAny

/**
 * En general amount finds generic numbers such as
 * 1, 2, three, four
 *
 * @property config
 * @constructor Create empty En general amount
 */
internal class ENGeneralAmount(override val config: ENConfig) : ParserGenericNumbers(config) {
    override val matchPattern: Regex
        get() = "${super.matchPattern}|(?:(?:a|an)\\s+(?=half|quarter))?(${amounts.keys.matchAny()})".toRegex()

    override fun onMatch(match: MatchResult): DateTime? {
        val key = match.groupValues[1].ifBlank { match.value }
        val number = amounts[key] ?: match.value.toDoubleOrNull() ?: return null
        if (!number.isFinite() || number > 1_000_000) return null
        return config.getDateTime(
            generalNumber = number,
            points = if (match.value == "a" || match.value == "an") 0 else 1
        )
    }
}