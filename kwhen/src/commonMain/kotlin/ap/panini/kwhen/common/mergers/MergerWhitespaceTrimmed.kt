package ap.panini.kwhen.common.mergers

import ap.panini.kwhen.common.Merger
import ap.panini.kwhen.configs.Config

/**
 * Merger whitespace trimmed is a merger that ignores whitespace
 *
 * @property config
 * @constructor Create empty Merger whitespace trimmed
 */
internal abstract class MergerWhitespaceTrimmed(override val config: Config) : Merger(config) {

    /**
     * Prefix pattern ignores nearby spaces or word borders
     */
    @Suppress("RegExpUnnecessaryNonCapturingGroup")
    final override val prefixPattern: Regex
        get() = "(?:\\b|\\s+)(?:$prefixMatchPattern)\\s*$".toRegex()

    /**
     * Between pattern ignores nearby spaces or word borders
     */
    @Suppress("RegExpUnnecessaryNonCapturingGroup")
    final override val betweenPattern: Regex
        get() = "^\\s*(?:$betweenMatchPattern)\\s*$".toRegex()
}