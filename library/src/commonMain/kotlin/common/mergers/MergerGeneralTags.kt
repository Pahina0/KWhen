package common.mergers

import DateTime
import common.Config

class MergerGeneralTags(override val config: Config) : MergerWhitespaceTrimmed(config) {

    override val betweenMatchPattern: Regex
        get() = "\\s*".toRegex()

    override val mergeRightWithLeft: Boolean
        get() = true

    override val reward: Int
        get() = 0

    override fun onMatch(
        left: DateTime?,
        right: DateTime?,
        prefix: MatchResult?,
        between: MatchResult?
    ): DateTime? {
        if (left == null || right == null) return null

        val generalTag = left.generalTimeTag ?: right.generalTimeTag ?: return null
        val generalNumber = right.generalNumber ?: left.generalNumber ?: return null

        return DateTime(
            generalNumber = generalNumber,
            generalTimeTag = generalTag
        )
    }
}
