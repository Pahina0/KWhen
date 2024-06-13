package en.mergers

import DateTime
import TimeUnit
import common.mergers.MergerWhitespaceTrimmed
import configs.ENConfig
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import util.getDateTimeWithGeneral

/**
 * merges phrases such as
 * from 4am to 7pm
 * */
internal class ENEnd(override val config: ENConfig) : MergerWhitespaceTrimmed(config) {
    override val prefixMatchPattern: Regex
        get() = "to|till|ends|ending|-|until".toRegex()

    override val mergePrefixWithLeft: Boolean
        get() = true

    override fun onMatch(
        left: DateTime?,
        right: DateTime?,
        prefix: MatchResult?,
        between: MatchResult?
    ): DateTime? {
        if (left == null || prefix == null) return null


        if (left.generalNumber != null) {
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
                    endTime = getDateTimeWithGeneral(
                        hour,
                        TimeUnit.HOUR,
                        null
                    ),
                    tagsTimeEnd = left.tagsTimeEnd + TimeUnit.HOUR + TimeUnit.MINUTE,
                    generalTimeTag = null,
                    generalNumber = null,
                )
            }


            return left.copy(
                endTime = getDateTimeWithGeneral(
                    left.generalNumber,
                    left.generalTimeTag,
                    null
                ),
                tagsTimeEnd = left.tagsTimeEnd + left.generalTimeTag,
                generalTimeTag = null,
                generalNumber = null,
            )
        }




        return left.copy(
            startTime = DateTime().startTime,
            endTime = left.startTime,
            tagsTimeStart = setOf(),
            tagsTimeEnd = left.tagsTimeStart,
        )
    }
}