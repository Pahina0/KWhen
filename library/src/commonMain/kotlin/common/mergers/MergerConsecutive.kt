package common.mergers

import DateTime
import common.Config

class MergerConsecutive(override val config: Config) : MergerWhitespaceTrimmed(config) {
    override val betweenMatchPattern: Regex
        get() = "\\s*".toRegex()

    override val mergeRightWithLeft: Boolean
        get() = true


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