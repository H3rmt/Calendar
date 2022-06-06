package frame.tabs

import calendar.Note
import calendar.Notes
import calendar.Type
import calendar.Types
import frame.styles.GlobalStyles
import frame.styles.NoteStyles
import frame.styles.TabStyles
import frame.typeCombobox
import javafx.beans.property.Property
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.scene.web.HTMLEditor
import logic.*
import tornadofx.*
import java.time.LocalDate


fun createNoteTab(pane: TabPane, time: LocalDate, isWeek: Boolean): Tab {
	log("creating note tab", LogType.IMPORTANT)
	return pane.tab("") {
		text = if(isWeek) {
			"Week Notes for %s to %s".translate(
				Language.TranslationTypes.Note,
				"${time.dayOfMonth}.${time.month.value}.",
				"${time.plusDays(6).dayOfMonth}.${time.plusDays(6).month.value}."
			)
		} else {
			"Notes on %s".translate(Language.TranslationTypes.Note, "${time.dayOfMonth}.${time.month.value}.")
		}
		isClosable = true
		addClass(TabStyles.tab_)
		
		vbox {
			addClass(TabStyles.content_)
			lateinit var add: Button
			lateinit var addType: ComboBox<Type>
			val type: Property<Type> = Types.getRandom("Note").toProperty()
			
			var noteTabs = mutableMapOf<Note, TitledPane>()
			
			hbox(spacing = 20.0, alignment = Pos.CENTER_LEFT) {
				addClass(TabStyles.topbar_)
				
				addType = typeCombobox(type)
				add = button {
					text = "Add"
					
					// disables button if no type selected or type already added
					addType.valueProperty().listen(runOnce = true) { new ->
						isDisable = noteTabs.any { it.key.type.value == new }
					}
					addClass(TabStyles.titleButton_)
				}
			}
			
			scrollpane(fitToWidth = true, fitToHeight = true) {
				addClass(GlobalStyles.disableFocusDraw_)
				addClass(GlobalStyles.maxHeight_)
				addClass(GlobalStyles.background_)
				isPannable = true
				
				// gets stretched across whole scrollpane
				vbox(spacing = 2.0, alignment = Pos.TOP_CENTER) {
					addClass(GlobalStyles.background_)
					
					add.action {
						Note.new(time, "", type.value, isWeek)
						add.isDisable = true
					}
					
					val notes = Notes.getNotesAt(time)
					notes.listenUpdates { change ->
						while(change.next()) {
							if(change.wasAdded()) {
								for(note in change.addedSubList) {
									noteTabs[note] = noteTab(this, note)
								}
							}
							if(change.wasRemoved()) {
								for(note in change.removed) {
									// custom filtering, as notes are not the exact same (this will always return only one element)
									this.children.removeAll(noteTabs.filter { it.key == note }.values)
									noteTabs = noteTabs.filter { it.key != note }.toMutableMap()
									
									// check addType selector again
									add.isDisable = noteTabs.any { it.key.type.value == addType.value }
								}
							}
						}
					}
					for(note in notes) {
						noteTabs[note] = noteTab(this, note)
					}
				}
			}
		}
	}
}

fun noteTab(tabs: VBox, note: Note): TitledPane {
	// cannot use the EventTarget Functions because they automatically add the
	// pane to the end of the vbox
	val pane = TitledPane()
	pane.apply {
		textProperty().bind(note.type.value.name)
		isExpanded = getConfig(Configs.ExpandNotesOnOpen)
		addClass(NoteStyles.notesPane_)
		
		@Suppress("JoinDeclarationAndAssignment")
		lateinit var editor: HTMLEditor
		
		graphic = toolbar {
			addClass(NoteStyles.paneToolbar_)
			hbox(spacing = 20.0) {
				style {
					fontSize = 15.px
				}
				button("save".translate(Language.TranslationTypes.Note)) {
					action {
						note.text.set(editor.htmlText)
					}
				}
				
				button("delete".translate(Language.TranslationTypes.Note)) {
					action {
						note.remove()
					}
				}
			}
		}
		expandedProperty().listen(runOnce = true) { new ->
			contentDisplay = if(new)
				ContentDisplay.RIGHT
			else
				ContentDisplay.TEXT_ONLY
		}
		
		editor = htmleditor(note.text.value) {
			addClass(GlobalStyles.disableFocusDraw_)
			addClass(NoteStyles.editor_)
		}
	}
	
	// set tab as first
	tabs.getChildList()?.add(0, pane)
	
	return pane
}
