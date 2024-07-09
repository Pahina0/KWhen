package ap.panini.kwhen.common.parsers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.configs.Config

internal open class ParserGenericNumbers(override val config: Config) : ParserByWord(config) {
    override val matchPattern: Regex
        get() = "\\d+".toRegex()

    override fun onMatch(match: MatchResult): DateTime {
        return DateTime(generalNumber = match.value.toDouble())
    }
}