package en.mergers

import DateTime
import TagTime
import common.mergers.MergerWhitespaceTrimmed
import en.ENConfig
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import util.getDateTimeWithGeneral

/**
 * merges phrases such as
 * from 4am to 7pm
 * */
class ENEnd(override val config: ENConfig) : MergerWhitespaceTrimmed(config) {
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
                    endTime = getDateTimeWithGeneral(
                        hour,
                        TagTime.HOUR,
                        null
                    ),
                    tagsTimeEnd = left.tagsTimeEnd + TagTime.HOUR + TagTime.MINUTE,
                    generalTimeTag = null,
                    generalNumber = null,
                    points = left.points + 1
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
                points = left.points + 1
            )
        }




        return left.copy(
            startTime = DateTime().startTime,
            endTime = left.startTime,
            tagsTimeStart = setOf(),
            tagsTimeEnd = left.tagsTimeStart,
            points = left.points + 1
        )
    }
}