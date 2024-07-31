package ap.panini.kwhen.en.mergers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.common.mergers.MergerList
import ap.panini.kwhen.configs.ENConfig

/**
 * En list merges a list with english words
 *
 * @property config
 * @constructor Create empty En list
 */
internal class ENList(override val config: ENConfig) : MergerList(config) {
    override val betweenMatchPattern: Regex
        get() = ",\\s*(?:and)?|and|&|\\s*".toRegex()

    override fun onMatch(
        left: DateTime?,
        right: DateTime?,
        prefix: MatchResult?,
        between: MatchResult?
    ): DateTime? {
        println("LEFT: $left")
        println("RIGHT: $right")
        return super.onMatch(left, right, prefix, between).also { println(it) }
    }
}