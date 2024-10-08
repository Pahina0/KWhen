package ap.panini.kwhen.common.mergers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.configs.Config

/**
 * Merger consecutive merges any 2 times in a row
 *
 * @property config the type of config you want
 * @constructor Create empty Merger consecutive
 */
internal class MergerConsecutive(override val config: Config) : MergerWhitespaceTrimmed(config) {
    override val betweenMatchPattern: Regex
        get() = "\\s*".toRegex()

    override val mergeRightWithLeft = BetweenMergeOption.FULL_MERGE


    override fun onMatch(left: DateTime?, right: DateTime?, prefix: MatchResult?, between: MatchResult?): DateTime? {
        if (left == null || right == null || between == null) return null

        if (
            left.tagsTimeStart.isNotEmpty() &&
            right.tagsTimeStart.isNotEmpty() &&
            left.tagsTimeStart.maxOfOrNull { it.ordinal } == right.tagsTimeStart.maxOfOrNull { it.ordinal }
        ) return null

        return left.merge(right)
    }
}