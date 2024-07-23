package ap.panini.kwhen

import kotlinx.datetime.LocalDateTime

data class Parsed(
   val text: String,
   val range: IntRange,

   val startTime: List<LocalDateTime>,

   val endTime: LocalDateTime? = null,

   val tagsTimeStart: Set<TimeUnit> = mutableSetOf(),
   val tagsTimeEnd: Set<TimeUnit> = mutableSetOf(),

   val repeatTag: TimeUnit? = null,
   val repeatOften: Int? = null
)