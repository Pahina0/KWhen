package common.mergers

import DateTime
import configs.ENConfig

internal open class MergerList(override val config: ENConfig) : MergerWhitespaceTrimmed(config) {
    override val betweenMatchPattern: Regex
        get() = ",\\s*|&\\s*".toRegex()

    override val mergeBetweenWithRight: Boolean
        get() = true


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

        return right.copy()
    }
}