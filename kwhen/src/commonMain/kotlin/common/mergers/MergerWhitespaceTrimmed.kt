package common.mergers

import configs.Config
import common.Merger

internal abstract class MergerWhitespaceTrimmed(override val config: Config) : Merger(config) {
    @Suppress("RegExpUnnecessaryNonCapturingGroup")

    final override val prefixPattern: Regex
        get() = "(?:\\b|\\s*)(?:$prefixMatchPattern)\\s*$".toRegex()

    @Suppress("RegExpUnnecessaryNonCapturingGroup")
    final override val betweenPattern: Regex
        get() = "^\\s*(?:$betweenMatchPattern)\\s*$".toRegex()
}