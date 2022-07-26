package logic

import java.io.File
import java.util.*

object OSFolders {
	private val os = System.getProperty("os.name").lowercase(Locale.getDefault())

	fun getConfigFolder(): String {
		return if(os.startsWith("mac os x")) {
			System.getProperty("user.home") + "/Library/Preferences" + "/calendar/"
		} else if(os.startsWith("windows")) {
			System.getenv("APPDATA") + "\\calendar\\" + "configs\\"
		} else {
			System.getProperty("user.home") + "/.config" + "/calendar/"
		}.also { // create Folder if it doesn't exist
			if(!File(it).exists()) {
				val dir = File(it)
				dir.mkdirs()
			}
		}
	}

	fun getDataFolder(): String {
		return if(os.startsWith("mac os x")) {
			System.getProperty("user.home") + "/Library/Application Support" + "/calendar/"
		} else if(os.startsWith("windows")) {
			System.getenv("APPDATA") + "\\calendar\\" + "data\\"
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
