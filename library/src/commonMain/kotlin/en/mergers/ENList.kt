package en.mergers

import common.mergers.MergerConsecutive
import en.ENConfig

class ENList(config: ENConfig) : MergerConsecutive(config) {
    override val betweenMatchPattern: Regex
        get() = ",\\s*|and|&|\\s*".toRegex()

}