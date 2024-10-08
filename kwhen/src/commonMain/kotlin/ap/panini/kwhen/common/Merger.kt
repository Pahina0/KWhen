package ap.panini.kwhen.common

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.configs.Config

/**
 * Merger merges two found times together
 *
 * @property config any various configuration that may come from localization
 * @constructor Create empty Merger
 */
internal abstract class Merger(open val config: Config) {
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
    open val mergeRightWithLeft: BetweenMergeOption = BetweenMergeOption.NO_MERGE

    /**
     * The amount of points rewarded per successful merge
     * */
    open val reward = 1


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


    enum class BetweenMergeOption {
        /**
         * No Merge, do not merge with anything
         *
         * @constructor Create empty No Merge
         */
        NO_MERGE,

        /**
         * Full Merge, merges the left and right side together
         * this is checked recursively but **doesn't** get added to the new list of found times
         *
         * @constructor Create empty Full Merge
         */
        FULL_MERGE,

        /**
         * Prefix Merge, merges the left and between text together
         * this is checked recursively while being added to the new list of found times
         *
         * @constructor Create empty Prefix Merge
         */
        PREFIX_MERGE
    }

}