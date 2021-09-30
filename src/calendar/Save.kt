package calendar

import logic.ConfigFiles
import logic.LogType
import logic.getJson
import logic.getJsonReader
import logic.log
import java.io.File
import java.io.FileReader
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.IsoFields


fun saveDayNote(note: Note) {
	val noteTime = LocalDate.ofInstant(Instant.ofEpochSecond(note.time * 60), ZoneId.systemDefault())
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		if(!exists()) {
			log("file with notes for ${noteTime.month.name} not found", LogType.LOW)
			createNewFile()
			writeText("{\n\t\"Week Notes\": [],\n\t\"Day Notes\": []\n}")
		}
	}
	
	val tmpDayNotes: MutableMap<Int, MutableList<Note>> = mutableMapOf()
	
	getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java)
		.forEach { (name, list) ->
			when(name) {
				"Day Notes" -> {
					log("reading Day Notes", LogType.LOW)
					list.forEach {
						FromJSON.createNote(it)?.apply {
							val time = LocalDate.ofInstant(Instant.ofEpochSecond(time * 60), ZoneId.systemDefault())
							
							if(!tmpDayNotes.containsKey(time.dayOfMonth))
								tmpDayNotes[time.dayOfMonth] = mutableListOf()
							
							tmpDayNotes[time.dayOfMonth]!!.add(this)
							log("loaded Day Note: $this", LogType.LOW)
						}
					}
					log("loaded temp day Notes $tmpDayNotes", LogType.NORMAL)
				}
			}
		}
	
	tmpDayNotes[noteTime.dayOfMonth]?.removeIf { it.time == note.time && it.type == note.type }
	
	if(!tmpDayNotes.containsKey(noteTime.dayOfMonth))
		tmpDayNotes[noteTime.dayOfMonth] = mutableListOf()
	
	tmpDayNotes[noteTime.dayOfMonth]!!.add(note)
	log("new day Notes $tmpDayNotes", LogType.NORMAL)
	
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		val original =
			getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java)
				.toMutableMap()
		
		val list = mutableListOf<Map<String, Any>>()
		tmpDayNotes.forEach { list.addAll(it.value.map { note -> ToJson.createNote(note) }) }
		
		original["Day Notes"] = list as ArrayList<Map<String, Any>>
		
		writeText(getJson().toJson(original))
	}
}

fun removeDayNote(note: Note) {
	val noteTime = LocalDate.ofInstant(Instant.ofEpochSecond(note.time * 60), ZoneId.systemDefault())
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		if(!exists()) {
			log("file with notes for ${noteTime.month.name} not found", LogType.LOW)
			createNewFile()
			writeText("{\n\t\"Week Notes\": [],\n\t\"Day Notes\": []\n}")
		}
	}
	
	val tmpDayNotes: MutableMap<Int, MutableList<Note>> = mutableMapOf()
	
	getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java)
		.forEach { (name, list) ->
			when(name) {
				"Day Notes" -> {
					log("reading Day Notes", LogType.LOW)
					list.forEach {
						FromJSON.createNote(it)?.apply {
							val time = LocalDate.ofInstant(Instant.ofEpochSecond(time * 60), ZoneId.systemDefault())
							
							if(!tmpDayNotes.containsKey(time.dayOfMonth))
								tmpDayNotes[time.dayOfMonth] = mutableListOf()
							
							tmpDayNotes[time.dayOfMonth]!!.add(this)
							log("loaded Day Note: $this", LogType.LOW)
						}
					}
					log("loaded temp day Notes $tmpDayNotes", LogType.NORMAL)
				}
			}
		}
	
	tmpDayNotes[noteTime.dayOfMonth]?.removeIf { it.time == note.time && it.type == note.type }
	
	log("new day Notes $tmpDayNotes", LogType.NORMAL)
	
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		val original =
			getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java)
				.toMutableMap()
		
		val list = mutableListOf<Map<String, Any>>()
		tmpDayNotes.forEach { list.addAll(it.value.map { note -> ToJson.createNote(note) }) }
		
		original["Day Notes"] = list as ArrayList<Map<String, Any>>
		
		writeText(getJson().toJson(original))
	}
}

fun saveWeekNote(note: Note) {
	val noteTime = LocalDate.ofInstant(Instant.ofEpochSecond(note.time * 60), ZoneId.systemDefault())
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		if(!exists()) {
			log("file with notes for ${noteTime.month.name} not found", LogType.LOW)
			createNewFile()
			writeText("{\n\t\"Week Notes\": [],\n\t\"Day Notes\": []\n}")
		}
	}
	
	val tmpWeekNotes: MutableMap<Int, MutableList<Note>> = mutableMapOf()
	
	getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java)
		.forEach { (name, list) ->
			when(name) {
				"Week Notes" -> {
					log("reading Week Notes", LogType.LOW)
					list.forEach {
						FromJSON.createNote(it)?.apply {
							val time = LocalDate.ofInstant(Instant.ofEpochSecond(time * 60), ZoneId.systemDefault())
							
							if(!tmpWeekNotes.containsKey(time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)))
								tmpWeekNotes[time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)] = mutableListOf()
							
							tmpWeekNotes[time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)]!!.add(this)
							log("loaded week Note: $this", LogType.LOW)
						}
					}
					log("loaded temp Week Notes $tmpWeekNotes", LogType.NORMAL)
				}
			}
		}
	
	tmpWeekNotes[noteTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)]?.removeIf { it.time == note.time && it.type == note.type }
	
	if(!tmpWeekNotes.containsKey(noteTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)))
		tmpWeekNotes[noteTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)] = mutableListOf()
	
	tmpWeekNotes[noteTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)]!!.add(note)
	log("new Week Notes $tmpWeekNotes", LogType.NORMAL)
	
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		val original =
			getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java)
				.toMutableMap()
		
		val list = mutableListOf<Map<String, Any>>()
		tmpWeekNotes.forEach { list.addAll(it.value.map { note -> ToJson.createNote(note) }) }
		
		original["Week Notes"] = list as ArrayList<Map<String, Any>>
		
		writeText(getJson().toJson(original))
	}
}

fun removeWeekNote(note: Note) {
	val noteTime = LocalDate.ofInstant(Instant.ofEpochSecond(note.time * 60), ZoneId.systemDefault())
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		if(!exists()) {
			log("file with notes for ${noteTime.month.name} not found", LogType.LOW)
			createNewFile()
			writeText("{\n\t\"Week Notes\": [],\n\t\"Day Notes\": []\n}")
		}
	}
	
	val tmpWeekNotes: MutableMap<Int, MutableList<Note>> = mutableMapOf()
	
	getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java)
		.forEach { (name, list) ->
			when(name) {
				"Week Notes" -> {
					log("reading Week Notes", LogType.LOW)
					list.forEach {
						FromJSON.createNote(it)?.apply {
							val time = LocalDate.ofInstant(Instant.ofEpochSecond(time * 60), ZoneId.systemDefault())
							
							if(!tmpWeekNotes.containsKey(time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)))
								tmpWeekNotes[time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)] = mutableListOf()
							
							tmpWeekNotes[time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)]!!.add(this)
							log("loaded week Note: $this", LogType.LOW)
						}
					}
					log("loaded temp Week Notes $tmpWeekNotes", LogType.NORMAL)
				}
			}
		}
	
	tmpWeekNotes[noteTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)]?.removeIf { it.time == note.time && it.type == note.type }
	
	log("new Week Notes $tmpWeekNotes", LogType.NORMAL)
	
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		val original =
			getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java)
				.toMutableMap()
		
		val list = mutableListOf<Map<String, Any>>()
		tmpWeekNotes.forEach { list.addAll(it.value.map { note -> ToJson.createNote(note) }) }
		
		original["Week Notes"] = list as ArrayList<Map<String, Any>>
		
		writeText(getJson().toJson(original))
	}
}