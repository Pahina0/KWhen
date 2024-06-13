package common.parsers

import DateTime
import configs.Config
internal open class ParserGenericNumbers(override val config: Config) : ParserByWord(config) {
    override val matchPattern: Regex
        get() = "\\d+".toRegex()

    override fun onMatch(match: MatchResult): DateTime {
        return DateTime(generalNumber = match.value.toInt())
    }
}