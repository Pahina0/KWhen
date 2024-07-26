package ap.panini.kwhen.en.mergers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.common.mergers.MergerWhitespaceTrimmed
import ap.panini.kwhen.configs.ENConfig
import ap.panini.kwhen.util.getRepeatTime


/**
 * En repeat merges times what would indicate a repeating time
 * such as every friday... it makes it repeat once a week on friday
 *
 * @property config
 * @constructor Create empty En repeat
 */
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
                repeatOften = times.toDouble()
            )
        }

        return left.copy(
            repeatTag = left.generalTimeTag,
            repeatOften = (left.generalNumber ?: 1.0) * times,
            generalTimeTag = null,
            generalNumber = null
        )

    }
}