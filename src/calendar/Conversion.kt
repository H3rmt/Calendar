package calendar

import logic.Warning
import java.time.DayOfWeek

object ToJson {
	private fun createNoteFile(file: File): Map<String, Any> {
		return mapOf(
			"data" to file.data,
			"name" to file.name,
			"origin" to file.origin
		)
	}
	
	fun createNote(note: Note): Map<String, Any> {
		val files = mutableListOf<Map<String, Any>>()
		for(t in note.files)
			createNoteFile(t).let { files.add(it) }
		
		return mapOf(
			"time" to note.time,
			"text" to note.text,
			"type" to note.type.name,
			"files" to files
		)
	}
}

object FromJSON {
	private fun createNoteFile(file: Map<String, Any>): File? {
		try {
			@Suppress("UNCHECKED_CAST")
			return File(
				(file["data"] as List<Byte>).toByteArray(),
				file["name"] as String,
				file["origin"] as String
			)
		} catch(e: Exception) {
			Warning("an35f7", e, "Exception creating NoteFile from map:$file")
		}
		return null
	}
	
	fun createNote(note: Map<String, Any>): Note? {
		try {
			val tmp = note["files"] as List<*>
			val files = mutableListOf<File>()
			for(t in tmp)
				@Suppress("UNCHECKED_CAST")
				createNoteFile(t as Map<String, Any>)?.let { files.add(it) }
			
			return Note(
				(note["time"] as Double).toLong(),
				note["text"] as String,
				Types.valueOf(note["type"] as String),
				files
			)
		} catch(e: Exception) {
			Warning("an35f7", e, "Exception creating Note from map:$note")
		}
		return null
	}
	
	fun createAppointment(appointment: Map<String, Any>, day: Boolean): Appointment? {
		try {
			return if(day) {
				Appointment(
					DayOfWeek.valueOf((appointment["day"] as String).uppercase()), (appointment["start"] as Double).toLong(),
					(appointment["duration"] as Double).toLong(), appointment["title"] as String,
					appointment["description"] as String, Types.valueOf(appointment["type"] as String)
				)
			} else {
				Appointment(
					DayOfWeek.SATURDAY, (appointment["start"] as Double).toLong(),
					(appointment["duration"] as Double).toLong(), appointment["title"] as String,
					appointment["description"] as String, Types.valueOf(appointment["type"] as String)
				)
			}
		} catch(e: Exception) {
			Warning("an35f7", e, "Exception creating Appointment from map:$appointment")
		}
		return null
	}
	
}