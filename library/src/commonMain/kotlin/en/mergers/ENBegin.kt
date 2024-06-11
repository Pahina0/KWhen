package en.mergers

import DateTime
import TimeUnit
import common.mergers.MergerWhitespaceTrimmed
import configs.ENConfig
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import util.getDateTimeWithGeneral

internal class ENBegin(override val config: ENConfig) : MergerWhitespaceTrimmed(config) {
    override val prefixMatchPattern: Regex
        get() = "(?:starting\\s+)?(from|on|at|during|in)(?:\\s+the)?|the".toRegex()

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
                if (left.text == "a") return null // on a boat isn't a time

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
                            hour,
                            TimeUnit.HOUR,
                            null
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
                        null
                    ),
                    tagsTimeStart = left.tagsTimeStart + left.generalTimeTag,
                    generalTimeTag = null,
                    generalNumber = null,
                )
            } else {
                return left.copy(
                    startTime = getDateTimeWithGeneral(
                        left.generalNumber,
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