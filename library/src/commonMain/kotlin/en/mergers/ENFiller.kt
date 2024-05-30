package en.mergers

import DateTime
import common.MergerWhitespaceTrimmed

class ENFiller : MergerWhitespaceTrimmed() {
    override val prefixMatchPattern: Regex
        get() = "(?:from|on|at|during)(?:\\s+the)?|the".toRegex()

    override val mergePrefixWithLeft: Boolean
        get() = true

    override fun onMatch(
        left: DateTime?,
        right: DateTime?,
        prefix: MatchResult?,
        between: MatchResult?,
    ): DateTime? {
        prefix ?: return null

        return left
    }
}