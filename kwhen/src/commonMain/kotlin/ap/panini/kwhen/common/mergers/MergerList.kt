package ap.panini.kwhen.common.mergers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.configs.ENConfig

/**
 * Merger list merges lists such as [apple, pineapple, and juice] into a single time
 *
 * @property config
 * @constructor Create empty Merger list
 */
internal open class MergerList(override val config: ENConfig) : MergerWhitespaceTrimmed(config) {
    override val betweenMatchPattern: Regex
        get() = ",\\s*|&\\s*".toRegex()

    override val mergeRightWithLeft = BetweenMergeOption.PREFIX_MERGE

    override fun onMatch(
        left: DateTime?,
        right: DateTime?,
        prefix: MatchResult?,
        between: MatchResult?
    ): DateTime? {
        if (left == null || right == null || between == null) return null

        if (
            left.tagsTimeStart.isNotEmpty() &&
            right.tagsTimeStart.isNotEmpty() &&
            left.tagsTimeStart.maxOfOrNull { it.ordinal } != right.tagsTimeStart.maxOfOrNull { it.ordinal }
        ) return null

        return left.copy()
    }
}