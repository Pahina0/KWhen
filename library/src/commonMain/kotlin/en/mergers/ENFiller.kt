package en.mergers

import DateTime
import common.MergerWhitespaceTrimmed

class ENFiller : MergerWhitespaceTrimmed() {
    override val prefixMatchPattern: Regex
        get() = "(?:starting\\s+)?(?:from|on|at|during|in)(?:\\s+the)?|the".toRegex()

    override val mergePrefixWithLeft: Boolean
        get() = true

    override fun onMatch(
        left: DateTime?,
        right: DateTime?,
        prefix: MatchResult?,
        between: MatchResult?,
    ): DateTime? {
        if (left == null || prefix == null) return null

        return left.copy(points = left.points + 1)
    }
}