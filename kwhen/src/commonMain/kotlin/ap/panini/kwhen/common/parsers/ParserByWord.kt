package ap.panini.kwhen.common.parsers

import ap.panini.kwhen.common.Parser
import ap.panini.kwhen.configs.Config


/**
 * Parser by word is a parser which finds words surrounded by spaces
 *
 * @property config
 * @constructor Create empty Parser by word
 */
internal abstract class ParserByWord(override val config: Config) : Parser(config) {

    @Suppress("RegExpUnnecessaryNonCapturingGroup")
    final override val pattern: Regex
        get() = "\\b(?:$matchPattern)\\b".toRegex()
}