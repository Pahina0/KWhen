package common


abstract class ParserByWord(override val config: Config) : Parser(config) {
    final override val pattern: Regex
        get() = "\\b(?:$matchPattern)\\b".toRegex()
}