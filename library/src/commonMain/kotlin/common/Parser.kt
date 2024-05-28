package common

import DateTime

abstract class Parser(open val config: Config) {

    /**
     * regex that should be applied to every pattern
     * */
    open val pattern: Regex by lazy { matchPattern }


    /**
     * the pattern that is overwritten
     * */
    protected abstract val matchPattern: Regex

    /**
     * what should be called when a match is found given pattern
     * */
    abstract fun onMatch(match: MatchResult): DateTime?
}