package en.mergers

import DateTime
import TagTime
import common.mergers.MergerWhitespaceTrimmed
import en.ENConfig
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import util.getDateTimeWithGeneral

class ENBegin(override val config: ENConfig) : MergerWhitespaceTrimmed(config) {
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
            if (/*left.endTime == null &&*/ setOf(
                    "from",
                    "on",
                    "at",
                    "during"
                ).contains(prefix.value.trim())
            ) {
                if (left.text == "a") return null // on a boat isn't a time

                if (left.generalTimeTag == TagTime.HOUR || left.generalTimeTag == null) {
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
                            TagTime.HOUR,
                            null
                        ),
                        tagsTimeStart = left.tagsTimeEnd + TagTime.HOUR + TagTime.MINUTE,
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
                        left.generalTimeTag ?: TagTime.HOUR,
                        left.endTime ?: left.startTime
                    ),
                    tagsTimeStart = left.tagsTimeStart + (left.generalTimeTag ?: TagTime.HOUR),
                    generalTimeTag = null,
                    generalNumber = null,
                )
            }
        }


        return left.copy()
    }
}