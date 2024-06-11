package en.mergers

import common.mergers.MergerList
import configs.ENConfig

internal class ENList(config: ENConfig) : MergerList(config) {
    override val betweenMatchPattern: Regex
        get() = ",\\s*(?:and)?|and|&|\\s*".toRegex()
}