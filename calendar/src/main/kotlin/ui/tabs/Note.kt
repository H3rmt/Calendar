package ui.tabs

import calendar.Note
import calendar.Notes
import calendar.Type
import calendar.Types
import ui.styles.GlobalStyles
import ui.styles.NoteStyles
import ui.styles.TabStyles
import ui.typeCombobox
import javafx.beans.property.*
import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.web.*
import logic.Configs
import logic.Language
import logic.ObservableListListeners.listenChanges
import logic.ObservableValueListeners.listen
import logic.getConfig
import logic.log
import logic.translate
import tornadofx.*
import java.time.LocalDate

/**
 * Create note tab
 *
 * Never called directly (called by TabManager.openTab(...,::createNoteTab,
 * ... ) )
 *
 * @param pane ref to main pane
 * @param time date on which notes should get displayed
 * @param isWeek bool to show week notes
 */
fun createNoteTab(pane: TabPane, time: LocalDate, isWeek: Boolean): Tab {
	log("creating note tab")
	return pane.tab("") {
		text = if(isWeek) {
			"week notes for %s to %s".translate(
				Language.TranslationTypes.Note,
				"${time.dayOfMonth}.${time.month.value}.",
				"${time.plusDays(6).dayOfMonth}.${time.plusDays(6).month.value}."
			)
		} else {
			"notes on %s".translate(Language.TranslationTypes.Note, "${time.dayOfMonth}.${time.month.value}.")
		}
		isClosable = true
		addClass(TabStyles.tab_)

		vbox {
			addClass(TabStyles.content_)
			lateinit var add: Button
			lateinit var addType: ComboBox<Type>
			val type: Property<Type> = Types.getRandom("Note").toProperty()
			var notePanes = mutableMapOf<Note, TitledPane>()

			// heaader bar
			hbox(spacing = 20.0, alignment = Pos.CENTER_LEFT) {
				addClass(TabStyles.topbar_)

				addType = typeCombobox(type)
				add = button {
					text = "Add"

					// disables button if no type selected or type already added
					addType.valueProperty().listen(runOnce = true) { new ->
						isDisable = notePanes.any { it.key.type.value == new }
					}
					addClass(TabStyles.titleButton_)
				}
			}

			// pane containing note display
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

					// listen for updates on notes list on this day
					notes.listenChanges { change ->
						while(change.next()) {
							if(change.wasAdded()) {
								for(note in change.addedSubList) {
									notePanes[note] = notePane(this, note)
								}
							}
							if(change.wasRemoved()) {
								for(note in change.removed) {
									// custom filtering, as notes are not the exact same (this will always return only one element)
									this.children.removeAll(notePanes.filter { it.key == note }.values)
									notePanes = notePanes.filter { it.key != note }.toMutableMap()

									// check addType selector again
									add.isDisable = notePanes.any { it.key.type.value == addType.value }
								}
							}
						}
					}
					for(note in notes) {
						notePanes[note] = notePane(this, note)
					}
				}
			}
		}
		log("created note tab")
	}
}

/**
 * creates a note display
 *
 * @param tabs ref to parent
 * @param note note to show
 */
private fun notePane(tabs: VBox, note: Note): TitledPane {
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
						note.remove() // TODO add confirm dialog
					}
				}
			}
		}
		expandedProperty().listen(runOnce = true) { new ->
			contentDisplay = if(new) ContentDisplay.RIGHT else ContentDisplay.TEXT_ONLY
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
