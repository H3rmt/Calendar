package logic

import java.io.File
import java.util.*

object OSFolders {
	/** Os */
	private val os = System.getProperty("os.name").lowercase(Locale.getDefault())

	/**
	 * Get config folder
	 *
	 * @return
	 */
	fun getConfigFolder(): String {
		return if(os.startsWith("mac os x")) {
			System.getProperty("user.home") + "/Library/Preferences" + "/calendar/"
		} else if(os.startsWith("windows")) {
			System.getenv("APPDATA") + "\\calendar\\"
		} else {
			System.getProperty("user.home") + "/.config" + "/calendar/"
		}.also { // create Folder if it doesn't exist
			if(!File(it).exists()) {
				val dir = File(it)
				dir.mkdirs()
			}
		}
	}

	/**
	 * Get data folder
	 *
	 * @return
	 */
	fun getDataFolder(): String {
		return if(os.startsWith("mac os x")) {
			System.getProperty("user.home") + "/Library/Application Support" + "/calendar/"
		} else if(os.startsWith("windows")) {
			System.getenv("APPDATA") + "\\calendar\\"
		} else {
			System.getProperty("user.home") + "/.local/share" + "/calendar/"
		}.also {// create Folder if it doesn't exist
			if(!File(it).exists()) {
				val dir = File(it)
				dir.mkdirs()
			}
		}
	}
}
