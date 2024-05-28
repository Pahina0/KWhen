package common

abstract class MergerWhitespaceTrimmed : Merger() {
    final override val prefixPattern: Regex
        get() = "\\b(?:$prefixMatchPattern)\\s+$".toRegex()

    final override val betweenPattern: Regex
        get() = "^\\s+(?:$betweenMatchPattern)\\s+$".toRegex()
}