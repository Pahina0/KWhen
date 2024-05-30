package common

import DateTime

abstract class Merger {
    /**
     * regex that should be applied to every pattern for matching prefix (text before first time)
     * */
    open val prefixPattern: Regex by lazy { prefixMatchPattern }


    /**
     * regex that should be applied to every pattern between two times
     * */
    open val betweenPattern: Regex by lazy { betweenMatchPattern }

    /**
     * the pattern that is overwritten for matching prefix
     * */
    protected open val prefixMatchPattern: Regex = "[\\s\\S]*".toRegex()

    /**
     * the pattern that is overwritten for matching between two times
     * */
    protected open val betweenMatchPattern: Regex = "[\\s\\S]*".toRegex()

    open val mergePrefixWithLeft = false
    open val mergeRightWithLeft = false

    /**
     * what is called when there is a match with all the patterns
     *
     * usual parsing will be prefix left between right
     *
     * @return DateTime: null if cannot merge, else a merged version of the date times besides text and range
     * */
    abstract fun onMatch(
        left: DateTime?,
        right: DateTime?,
        prefix: MatchResult?,
        between: MatchResult?,
    ): DateTime?
}