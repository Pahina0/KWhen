import kotlinx.datetime.LocalDateTime

data class Processed (
    val text: String,
    val range: IntRange ,

    val startTime: List<LocalDateTime> ,

    val endTime: LocalDateTime? = null,

    val tagsTimeStart: Set<TagTime> = mutableSetOf(),
    val tagsTimeEnd: Set<TagTime> = mutableSetOf(),

    val repeatTag: TagTime? = null,
    val repeatOften: Int? = null
)