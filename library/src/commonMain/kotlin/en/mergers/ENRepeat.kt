package en.mergers

import DateTime
import common.mergers.MergerWhitespaceTrimmed
import configs.ENConfig
import util.getRepeatTime

internal class ENRepeat(override val config: ENConfig) : MergerWhitespaceTrimmed(config) {
    override val prefixMatchPattern: Regex
        get() = "every(\\s+other)?".toRegex()
    override val mergePrefixWithLeft: Boolean
        get() = true

    override fun onMatch(
        left: DateTime?,
        right: DateTime?,
        prefix: MatchResult?,
        between: MatchResult?
    ): DateTime? {
        if (left == null || prefix == null) return null

        val times = if (prefix.groupValues[1].trim() == "") 1 else 2

        // already has an exact time: every 4th of july
        if (left.generalTimeTag == null) {
            return left.copy(
                repeatTag = left.tagsTimeStart.getRepeatTime() ?: return null,
                repeatOften = times
            )
        }

        return left.copy(
            repeatTag = left.generalTimeTag,
            repeatOften = (left.generalNumber ?: 1) * times,
            generalTimeTag = null,
            generalNumber = null
        )

    }
}