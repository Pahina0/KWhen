package ap.panini.kwhen.configs

import ap.panini.kwhen.common.Controller
import ap.panini.kwhen.en.ENController
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone

/**
 * En config is a configuration file for english parsing
 *
 * @property evening what time evening is
 * @property morning what time morning is
 * @property afternoon what time afternoon is
 * @property night what time night is
 * @property use24 if 24 hour time should be used or not
 * @constructor Create empty En config
 */
data class ENConfig(
    val evening: Int = 18,
    val morning: Int = 9,
    val afternoon: Int = 15,
    val night: Int = 20,
    val use24: Boolean = false,
    override val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    override val relativeTo: Long = Clock.System.now().toEpochMilliseconds(),
) : Config() {
    override fun instance(): Controller {
        return ENController(this)
    }
}

