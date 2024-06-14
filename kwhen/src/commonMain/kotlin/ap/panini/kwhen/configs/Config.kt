package ap.panini.kwhen.configs

import ap.panini.kwhen.common.Controller

// for things which are uncertain such as evening being somewhere between 6 - 9pm
sealed interface Config {
    fun instance(): ap.panini.kwhen.common.Controller
}
