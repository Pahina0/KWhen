package configs

import common.Controller

// for things which are uncertain such as evening being somewhere between 6 - 9pm
sealed interface Config {
    fun instance(): Controller
}
