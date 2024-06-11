package en.mergers

import common.mergers.MergerList
import en.ENConfig

class ENList(config: ENConfig) : MergerList(config) {
    override val betweenMatchPattern: Regex
        get() = ",\\s*(?:and)?|and|&|\\s*".toRegex()
}