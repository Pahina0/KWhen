package configs

import common.Controller
import en.ENController

data class ENConfig(
    val evening: Int = 18,
    val morning: Int = 9,
    val afternoon: Int = 15,
    val night: Int = 20,
    val use24: Boolean = false
) : Config {
    override fun instance(): Controller {
        return ENController(this)
    }
}

