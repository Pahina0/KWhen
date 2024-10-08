package ap.panini.kwhen.common.mergers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.configs.Config

/**
 * Merger general tags merges any general times together
 *
 * @property config
 * @constructor Create empty Merger general tags
 */
internal class MergerGeneralTags(override val config: Config) : MergerWhitespaceTrimmed(config) {

    override val betweenMatchPattern: Regex
        get() = "\\s*".toRegex()

    override val mergeRightWithLeft = BetweenMergeOption.FULL_MERGE

    override val reward: Int
        get() = 0

    override fun onMatch(
        left: DateTime?,
        right: DateTime?,
        prefix: MatchResult?,
        between: MatchResult?
    ): DateTime? {
        if (left == null || right == null || between == null) return null

        val generalTag = left.generalTimeTag ?: right.generalTimeTag ?: return null
        val generalNumber = right.generalNumber ?: left.generalNumber ?: return null

        return DateTime(
            generalNumber = generalNumber,
            generalTimeTag = generalTag
        )
    }
}
