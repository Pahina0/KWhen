package ap.panini.kwhen

import ap.panini.kwhen.Partials.HALF
import ap.panini.kwhen.Partials.QUARTER


enum class TimeUnit {
    SECOND {
        override fun partial(part: Double): Pair<TimeUnit, Int> {
            return Pair(SECOND, part.toInt())
        }

        /**
         * Un partial for seconds wont go any lower. This will not un partial anything
         *
         * @param part
         * @return part as an int
         */
        override fun unPartial(part: Double): Pair<TimeUnit, Int> {
            return Pair(SECOND, part.toInt())
        }
    },
    MINUTE {
        override fun partial(part: Double): Pair<TimeUnit, Int> {
            return Pair(SECOND, (60 * part).toInt())
        }

        override fun unPartial(part: Double): Pair<TimeUnit, Int> {
            if (part.rem(1).equals(0.0)) return Pair(MINUTE, part.toInt())

            return when (part) {
                QUARTER -> Pair(SECOND, 15)
                HALF -> Pair(SECOND, 30)
                else -> throw Partials.badPartialTimeException
            }
        }
    },
    HOUR {
        override fun partial(part: Double): Pair<TimeUnit, Int> {
            return MINUTE.partial(60 * part)
        }

        override fun unPartial(part: Double): Pair<TimeUnit, Int> {
            if (part.rem(1).equals(0.0)) return Pair(HOUR, part.toInt())

            return when (part) {
                QUARTER -> Pair(MINUTE, 15)
                HALF -> Pair(MINUTE, 30)
                else -> throw Partials.badPartialTimeException
            }
        }
    },
    DAY {
        override fun partial(part: Double): Pair<TimeUnit, Int> {
            return HOUR.partial(60 * part)
        }

        override fun unPartial(part: Double): Pair<TimeUnit, Int> {
            if (part.rem(1).equals(0.0)) return Pair(DAY, part.toInt())

            return when (part) {
                QUARTER -> Pair(HOUR, 6)
                HALF -> Pair(HOUR, 12)
                else -> throw Partials.badPartialTimeException
            }
        }
    },
    WEEK {
        override fun partial(part: Double): Pair<TimeUnit, Int> {
            return DAY.partial(7 * part)
        }

        override fun unPartial(part: Double): Pair<TimeUnit, Int> {
            if (part.rem(1).equals(0.0)) return Pair(WEEK, part.toInt())

            return when (part) {
                QUARTER -> Pair(DAY, 2)
                HALF -> Pair(DAY, 4)
                else -> throw Partials.badPartialTimeException
            }
        }
    },
    MONTH {
        override fun partial(part: Double): Pair<TimeUnit, Int> {
            return DAY.partial(30 * part)
        }

        override fun unPartial(part: Double): Pair<TimeUnit, Int> {
            if (part.rem(1).equals(0.0)) return Pair(MONTH, part.toInt())

            return when (part) {
                QUARTER -> Pair(DAY, 8)
                HALF -> Pair(DAY, 15)
                else -> throw Partials.badPartialTimeException
            }
        }
    },
    YEAR {
        override fun partial(part: Double): Pair<TimeUnit, Int> {
            return DAY.partial(365 * part)

        }

        override fun unPartial(part: Double): Pair<TimeUnit, Int> {
            if (part.rem(1).equals(0.0)) return Pair(YEAR, part.toInt())

            return when (part) {
                QUARTER -> Pair(MONTH, 3)
                HALF -> Pair(MONTH, 6)
                else -> throw Partials.badPartialTimeException
            }
        }
    };

    abstract fun partial(part: Double): Pair<TimeUnit, Int>

    /**
     * Un partial converts a partial time to a whole number if it is under 0
     * If the number is partial it must be QUARTER or HALF (0.25 or 0.5 respectively)
     *
     * @param part the double part to convert from
     * @throws RuntimeException if the double value isn't supported
     * @return The new time unit it belongs to and the whole number representation
     */
    abstract fun unPartial(part: Double): Pair<TimeUnit, Int>
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

object Partials {
    internal const val QUARTER = 0.25
    internal const val HALF = 0.5


    internal val badPartialTimeException =
        RuntimeException("The partial time must be either QUARTER (0.25) or HALF (0.5)")
}
