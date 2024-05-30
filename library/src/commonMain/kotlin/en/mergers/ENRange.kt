package en.mergers

import DateTime
import common.MergerWhitespaceTrimmed

/**
 * merges phrases such as
 * from 4am to 7pm
 * */
class ENRange : MergerWhitespaceTrimmed() {
    override val prefixMatchPattern: Regex
        get() = "(?:(?:starting\\s+from)?|from)?".toRegex()
    override val betweenMatchPattern: Regex
        get() = "to|till|ends|ending|until|-".toRegex()

    override val mergePrefixWithLeft: Boolean
        get() = true

    override val mergeRightWithLeft: Boolean
        get() = true

    override fun onMatch(
        left: DateTime?,
        right: DateTime?,
        prefix: MatchResult?,
        between: MatchResult?
    ): DateTime? {
        if (left == null || right == null) return null

        return left.copy(
            endTime = right.startTime,
            tagsTimeEnd = right.tagsTimeStart,
            points = left.points + right.points
        )
    }
}