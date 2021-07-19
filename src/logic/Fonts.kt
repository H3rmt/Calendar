package logic

import java.awt.Font
import java.io.File
import java.io.FileReader
import java.io.StringReader

class Fonts {

	/**
	 * linking a Fonttype to a Font
	 *
	 * @see Fonttype
	 * @see Font
	 */
	private var fonts: MutableMap<Fonttype, Font>

	/**
	 * creates json file if it didn't exist
	 *
	 * reads JSON from file and stores different log.Fonts
	 * in log.getFonts Map
	 *
	 * @see log.getFonts
	 */
	init {
		val file = File(getfontfile())
		if(! file.exists()) {
			file.createNewFile()
			val default = "{\n\n}"
			file.writeText(default)
		}
		val loadfonts: Map<String, Map<String, Double>> = getJson().fromJson(getJsonReader(FileReader(getfontfile())), Map::class.java)
		fonts = mutableMapOf()

		loadfonts.forEach {
			try {
				val type: Fonttype = getJson().fromJson(getJsonReader(StringReader(it.key)), Fonttype::class.java)
				fonts[type] = Font(getConfig(Configs.fontfamily), it.value["Style"] !!.toInt(), it.value["Size"] !!.toInt())
			} catch (e: NullPointerException) {
				log("$it is an invalid JSON entry", LogType.WARNING)
			}
		}
	}

	/**
	 * finds the corresponding Font to a
	 * Fonttype
	 *
	 * @param font Fonttype enum entry
	 *
	 * @return font linked with Fonttype
	 *
	 * @see Font
	 * @see Fonttype
	 * @see Fonts
	 */
	fun get(font: Fonttype): Font {
		try {
			fonts[font] !!.let {
				return it
			}
		} catch (e: NullPointerException) {
			log("$font was not found", LogType.WARNING)
			return Font("Arial", 2, 14)
		}
	}

	/**
	 * all different types of log.getFonts
	 *
	 * for a font to get loaded from JSON
	 * it must be specified here
	 */
	enum class Fonttype {
		Title, Header, SmolHeader, Description, Button,
	}

}