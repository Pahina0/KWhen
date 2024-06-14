package ap.panini.kwhen.en.mergers

import ap.panini.kwhen.common.mergers.MergerList
import ap.panini.kwhen.configs.ENConfig

internal class ENList(config: ENConfig) : MergerList(config) {
    override val betweenMatchPattern: Regex
        get() = ",\\s*(?:and)?|and|&|\\s*".toRegex()
}