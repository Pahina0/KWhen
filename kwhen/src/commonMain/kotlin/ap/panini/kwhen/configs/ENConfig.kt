package ap.panini.kwhen.configs

import ap.panini.kwhen.en.ENController

data class ENConfig(
    val evening: Int = 18,
    val morning: Int = 9,
    val afternoon: Int = 15,
    val night: Int = 20,
    val use24: Boolean = false
) : Config {
    override fun instance(): ap.panini.kwhen.common.Controller {
        return ENController(this)
    }
}

