package common

import DateTime

class MergerConsecutive : MergerWhitespaceTrimmed() {
    override val betweenMatchPattern: Regex
        get() = "\\s*".toRegex()

    override val mergeRightWithLeft: Boolean
        get() = true

    override val mergeBetweenWithLeft: Boolean
        get() = true

    override fun onMatch(left: DateTime?, right: DateTime?, prefix: MatchResult?, between: MatchResult?): DateTime {
        return DateTime()
    }
}