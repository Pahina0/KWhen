package ap.panini.kwhen.common

import ap.panini.kwhen.DateTime
import ap.panini.kwhen.configs.Config

/**
 * Parser finds specific words and turns them into a timeunit
 *
 * @property config
 * @constructor Create empty Parser
 */
internal abstract class Parser(open val config: Config) {

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