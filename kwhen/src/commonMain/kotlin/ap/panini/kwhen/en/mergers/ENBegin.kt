package ap.panini.kwhen.en.mergers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.TimeUnit
import ap.panini.kwhen.common.mergers.MergerWhitespaceTrimmed
import ap.panini.kwhen.configs.ENConfig
import ap.panini.kwhen.util.copy
import ap.panini.kwhen.util.getDateTimeWithGeneral

/**
 * En begin finds words that come before times that can be merged with times
 *
 * @property config
 * @constructor Create empty En begin
 */
internal class ENBegin(override val config: ENConfig) : MergerWhitespaceTrimmed(config) {
    override val prefixMatchPattern: Regex
        get() = "(?:starting\\s+)?(from|on|at|during|in|after)(?:\\s+the)?|the|next".toRegex()

    override val mergePrefixWithLeft: Boolean
        get() = true

    override fun onMatch(
        left: DateTime?,
        right: DateTime?,
        prefix: MatchResult?,
        between: MatchResult?,
    ): DateTime? {
        if (left == null || prefix == null) return null

        if (prefix.value.trim() == "next" && left.generalTimeTag != null) {

            return left.copy(
                startTime = getDateTimeWithGeneral(
                    left.generalNumber ?: 1.0,
                    left.generalTimeTag,
                    left.startTime,
                    config
                ),
                tagsTimeStart = left.tagsTimeStart + left.generalTimeTag,
                generalTimeTag = null,
                generalNumber = null,
            )
        }

        if (left.generalNumber == null) {
            return left.copy()
        }

        if (setOf(
                "from",
                "on",
                "at",
                "during",
                "after"
            ).contains(prefix.value.trim())
        ) {
            if (left.text == "a") return null // "on a boat" isn't a time

            if (left.generalTimeTag == TimeUnit.HOUR || left.generalTimeTag == null) {
                val currentHour =
                    config.now().hour
                val hour = if (config.use24) {
                    left.generalNumber
                } else {
                    if (currentHour < left.generalNumber) {
                        left.generalNumber
                    } else if (currentHour < left.generalNumber + 12) {
                        (left.generalNumber + 12) % 24
                    } else {
                        left.generalNumber
                    }
                }

                return left.copy(
                    startTime = getDateTimeWithGeneral(
                        hour,
                        TimeUnit.HOUR,
                        null,
                        config
                    ).copy(
                        minute = 0
                    ),
                    tagsTimeStart = left.tagsTimeEnd + TimeUnit.HOUR + TimeUnit.MINUTE,
                    generalTimeTag = null,
                    generalNumber = null,
                )
            }

            return left.copy(
                startTime = getDateTimeWithGeneral(
                    left.generalNumber,
                    left.generalTimeTag,
                    null,
                    config
                ),
                tagsTimeStart = left.tagsTimeStart + left.generalTimeTag,
                generalTimeTag = null,
                generalNumber = null,
            )
        }

        return left.copy(
            startTime = getDateTimeWithGeneral(
                left.generalNumber,
                left.generalTimeTag ?: TimeUnit.HOUR,
                left.endTime ?: left.startTime,
                config
            ),
            tagsTimeStart = left.tagsTimeStart + (left.generalTimeTag ?: TimeUnit.HOUR),
            generalTimeTag = null,
            generalNumber = null,
        )


    }
}