package ap.panini.kwhen

import ap.panini.kwhen.configs.Config
import ap.panini.kwhen.configs.ENConfig

/**
 * Time parser parses a sentence  and outputs the various times it can find
 *
 * @property config
 * @constructor Create empty Time parser
 */
class TimeParser(val config: Config = ENConfig()) {

    /**
     * Parse parses a string to find time units contained in it
     *
     * @param input the string you want to find times from
     * @return a list of found times
     */
    fun parse(input: String): List<Parsed> {
        val controller = config.instance()

        val parsed = controller.parse(input)
        val merged = controller.merge(input, parsed)
        return controller
            .finalize(merged)
            .map { it.copy(text = input.substring(it.range)) }
    }
}