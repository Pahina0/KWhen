package ap.panini.kwhen.en.mergers

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.TimeUnit
import ap.panini.kwhen.common.mergers.MergerWhitespaceTrimmed
import ap.panini.kwhen.configs.ENConfig
import ap.panini.kwhen.util.copy
import ap.panini.kwhen.util.getDateTimeWithGeneral
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class ENBegin(override val config: ENConfig) : MergerWhitespaceTrimmed(config) {
    override val prefixMatchPattern: Regex
        get() = "(?:starting\\s+)?(from|on|at|during|in|after)(?:\\s+the)?|the".toRegex()

    override val mergePrefixWithLeft: Boolean
        get() = true

    override fun onMatch(
        left: DateTime?,
        right: DateTime?,
        prefix: MatchResult?,
        between: MatchResult?,
    ): DateTime? {
        if (left == null || prefix == null) return null

        if (left.generalNumber != null) {
            if (setOf(
                    "from",
                    "on",
                    "at",
                    "during"
                ).contains(prefix.value.trim())
            ) {
                if (left.text == "a") return null // "on a boat" isn't a time

                if (left.generalTimeTag == TimeUnit.HOUR || left.generalTimeTag == null) {
                    val currentHour =
                        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
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
                            hour.toInt(),
                            TimeUnit.HOUR,
                            null
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
                        left.generalNumber.toInt(),
                        left.generalTimeTag,
                        null
                    ),
                    tagsTimeStart = left.tagsTimeStart + left.generalTimeTag,
                    generalTimeTag = null,
                    generalNumber = null,
                )
            } else {
                return left.copy(
                    startTime = getDateTimeWithGeneral(
                        left.generalNumber.toInt(),
                        left.generalTimeTag ?: TimeUnit.HOUR,
                        left.endTime ?: left.startTime
                    ),
                    tagsTimeStart = left.tagsTimeStart + (left.generalTimeTag ?: TimeUnit.HOUR),
                    generalTimeTag = null,
                    generalNumber = null,
                )
            }
        }


        return left.copy()
    }
}