import configs.Config
import configs.ENConfig

class TimeParser(val config: Config = ENConfig()) {
    fun parse(input: String): List<Parsed> {
        val controller = config.instance()

        val parsed = controller.parse(input)
        val merged = controller.merge(input, parsed)
        return controller.finalize(merged)
    }
}