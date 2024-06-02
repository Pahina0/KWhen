package common.mergers

import common.Config
import common.Merger

abstract class MergerWhitespaceTrimmed(override val config: Config) : Merger(config) {
    final override val prefixPattern: Regex
        get() = "(?:\\b|\\s*)(?:$prefixMatchPattern)\\s*$".toRegex()

    final override val betweenPattern: Regex
        get() = "^\\s*(?:$betweenMatchPattern)\\s*$".toRegex()
}