package ap.panini.kwhen

enum class TimeUnit {
    SECOND {
        override fun partial(part: Double): Pair<TimeUnit, Int> {
            return Pair(SECOND, part.toInt())
        }
    },
    MINUTE {
        override fun partial(part: Double): Pair<TimeUnit, Int> {
            return Pair(SECOND, (60 * part).toInt())
        }
    },
    HOUR {
        override fun partial(part: Double): Pair<TimeUnit, Int> {
            return MINUTE.partial(60 * part)
        }
    },
    DAY {
        override fun partial(part: Double): Pair<TimeUnit, Int> {
            return HOUR.partial(60 * part)
        }
    },
    WEEK {
        override fun partial(part: Double): Pair<TimeUnit, Int> {
            return DAY.partial(7 * part)
        }
    },
    MONTH {
        override fun partial(part: Double): Pair<TimeUnit, Int> {
            return DAY.partial(30 * part)
        }
    },
    YEAR {
        override fun partial(part: Double): Pair<TimeUnit, Int> {
            return DAY.partial(365 * part)

        }
    };

    abstract fun partial(part: Double): Pair<TimeUnit, Int>
}

enum class DayOfWeek {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
}