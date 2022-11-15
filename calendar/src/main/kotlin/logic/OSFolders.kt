package logic

import logic.OSFolders.getConfigFolder
import logic.OSFolders.getDataFolder
import java.io.File
import java.util.*

/**
 * Get paths to config and data folders fo current platform
 *
 * @see getConfigFolder
 * @see getDataFolder
 */
object OSFolders {
	/** current OS name */
	private val os = System.getProperty("os.name").lowercase(Locale.getDefault())

	/** folder where files are put inside */
	private const val name = "calendar"

	/**
	 * Get config folder, depending on user platform
	 *
	 * also creates folder if its missing
	 *
	 * @return path to folder to store configs
	 */
	fun getConfigFolder(): String {
		return if(os.startsWith("mac os x")) {
			System.getProperty("user.home") + "/Library/Preferences" + "/$name/"
		} else if(os.startsWith("windows")) {
			System.getenv("APPDATA") + "\\$name\\" + "configs\\"
		} else {
			System.getProperty("user.home") + "/.config" + "/$name/"
		}.also { // create Folder if it doesn't exist
			if(!File(it).exists()) {
				File(it).mkdirs()
			}
		}
	}

	/**
	 * Get data folder, depending on user platform
	 *
	 * also creates folder if its missing
	 *
	 * @return path to folder to store data
	 */
	fun getDataFolder(): String {
		return if(os.startsWith("mac os x")) {
			System.getProperty("user.home") + "/Library/Application Support" + "/$name/"
		} else if(os.startsWith("windows")) {
			System.getenv("APPDATA") + "\\$name\\" + "data\\"
		} else {
			System.getProperty("user.home") + "/.local/share" + "/$name/"
		}.also {// create Folder if it doesn't exist
			if(!File(it).exists()) {
				val dir = File(it)
				dir.mkdirs()
			}
		}
	}

	/**
	 * Get logs folder, depending on user platform
	 *
	 * also creates folder if its missing
	 *
	 * @return path to folder to store logs
	 */
	fun getLogFolder(): String {
		return if(os.startsWith("mac os x")) {
			System.getProperty("user.home") + "/Library/Application Support" + "/$name/"
		} else if(os.startsWith("windows")) {
			System.getenv("APPDATA") + "\\$name\\"
		} else {
			System.getProperty("user.home") + "/.local/share" + "/$name/"
		}.also {// create Folder if it doesn't exist
			if(!File(it).exists()) {
				File(it).mkdirs()
			}
		}
	}
}
