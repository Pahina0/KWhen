package common.parsers

import configs.Config
import common.Parser


internal abstract class ParserByWord(override val config: Config) : Parser(config) {
    @Suppress("RegExpUnnecessaryNonCapturingGroup")

    final override val pattern: Regex
        get() = "\\b(?:$matchPattern)\\b".toRegex()
}