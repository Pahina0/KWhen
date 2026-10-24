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

        if (left.generalNumber == null || left.generalTimeTag != null) return null
        if (right.generalTimeTag == null || right.generalNumber != null) return null

        return config.getDateTime(
            generalNumber = left.generalNumber,
            generalTimeTag = right.generalTimeTag
        )
    }
}
