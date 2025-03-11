package ap.panini.kwhen.common.parsers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.configs.Config

/**
 * Parser generic numbers is a parser to find numbers and save them as "generic" times
 *
 * @property config
 * @constructor Create empty Parser generic numbers
 */
internal open class ParserGenericNumbers(override val config: Config) : ParserByWord(config) {
    override val matchPattern: Regex
        get() = "\\d+".toRegex()

    override fun onMatch(match: MatchResult): DateTime {
        return config.getDateTime(generalNumber = match.value.toDouble(), points = 0)
    }
}