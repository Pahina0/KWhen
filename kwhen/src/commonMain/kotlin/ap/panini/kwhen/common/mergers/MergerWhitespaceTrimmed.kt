package ap.panini.kwhen.common.mergers

import ap.panini.kwhen.common.Merger
import ap.panini.kwhen.configs.Config

internal abstract class MergerWhitespaceTrimmed(override val config: Config) : Merger(config) {

    @Suppress("RegExpUnnecessaryNonCapturingGroup")
    final override val prefixPattern: Regex
        get() = "(?:\\b|\\s+)(?:$prefixMatchPattern)\\s*$".toRegex()

    @Suppress("RegExpUnnecessaryNonCapturingGroup")
    final override val betweenPattern: Regex
        get() = "^\\s*(?:$betweenMatchPattern)\\s*$".toRegex()
}