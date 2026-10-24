package ap.panini.kwhen.common.mergers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.TimeUnit
import ap.panini.kwhen.configs.Config

/**
 * Merger consecutive merges any 2 times in a row
 *
 * @property config the type of config you want
 * @constructor Create empty Merger consecutive
 */
internal class MergerConsecutive(override val config: Config) : MergerWhitespaceTrimmed(config) {
    override val betweenMatchPattern: Regex
        get() = "\\s*".toRegex()

    override val mergeRightWithLeft = BetweenMergeOption.FULL_MERGE


    override fun onMatch(left: DateTime?, right: DateTime?, prefix: MatchResult?, between: MatchResult?): DateTime? {
        if (left == null || right == null || between == null) return null

        fun DateTime.isValidOperand(): Boolean {
            if (generalTimeTag != null && generalNumber == null) return false
            if (generalNumber != null && generalTimeTag == null && tagsTimeStart.isEmpty() && tagsTimeEnd.isEmpty() && tagsDayOfWeek.isEmpty() && repeatTag == null) return false
            return tagsTimeStart.isNotEmpty() || tagsTimeEnd.isNotEmpty() || tagsDayOfWeek.isNotEmpty() || repeatTag != null || (generalTimeTag != null && generalNumber != null)
        }

        if (!left.isValidOperand() || !right.isValidOperand()) return null

        val leftIsTimeOnly = TimeUnit.HOUR in left.tagsTimeStart && left.tagsDayOfWeek.isEmpty()
        val rightIsTimeOnly = TimeUnit.HOUR in right.tagsTimeStart && right.tagsDayOfWeek.isEmpty()
        val leftIsDateOnly = TimeUnit.HOUR !in left.tagsTimeStart && left.tagsTimeStart.any { it.ordinal >= TimeUnit.DAY.ordinal }
        val rightIsDateOnly = TimeUnit.HOUR !in right.tagsTimeStart && right.tagsTimeStart.any { it.ordinal >= TimeUnit.DAY.ordinal }

        val leftHasMinute = TimeUnit.MINUTE in left.tagsTimeStart
        val rightHasMinute = TimeUnit.MINUTE in right.tagsTimeStart
        val oneIsSpecificTimeAndOtherIsVague = (leftHasMinute && !rightHasMinute && TimeUnit.HOUR in right.tagsTimeStart) ||
                (rightHasMinute && !leftHasMinute && TimeUnit.HOUR in left.tagsTimeStart)

        if (!(leftIsDateOnly && rightIsTimeOnly) && !(leftIsTimeOnly && rightIsDateOnly) && !oneIsSpecificTimeAndOtherIsVague) {
            if (
                left.tagsTimeStart.isNotEmpty() &&
                right.tagsTimeStart.isNotEmpty() &&
                left.tagsTimeStart.maxOfOrNull { it.ordinal } == right.tagsTimeStart.maxOfOrNull { it.ordinal }
            ) return null
        }

        return left.merge(right, config = config)
    }
}