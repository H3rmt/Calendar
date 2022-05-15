package frame.tabs

import calendar.CellDisplay
import calendar.Day
import calendar.Note
import calendar.Type
import calendar.Week
import frame.styles.GlobalStyles
import frame.styles.NoteStyles
import frame.styles.TabStyles
import frame.typeCombobox
import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.layout.*
import listen
import logic.Configs
import logic.Language
import logic.LogType
import logic.getConfig
import logic.log
import logic.translate
import tornadofx.*
import java.time.temporal.ChronoField



fun createNoteTab(pane: TabPane, cell: CellDisplay, updateCallback: () -> Unit): Tab {
	log("creating note tab", LogType.IMPORTANT)
	return pane.tab("") {
		text = when(cell) {
			is Day -> "Notes for ${cell.time.dayOfMonth}. ${cell.time.month.name.translate(Language.TranslationTypes.Global)}"
			is Week -> "Notes for ${cell.time.get(ChronoField.ALIGNED_WEEK_OF_MONTH)}. Week in ${cell.time.month.name.translate(Language.TranslationTypes.Global)}"
			else -> ""
		}
		isClosable = true
		addClass(TabStyles.tab_)
		
		vbox {
			addClass(TabStyles.content_)
			lateinit var add: Button
			lateinit var addType: ComboBox<Type>
			
			val noteTabs = mutableListOf<TitledPane>()
			
			hbox(spacing = 20.0, alignment = Pos.CENTER_LEFT) {
				addClass(TabStyles.topbar_)
				
				addType = typeCombobox()
				add = button {
					text = "Add"
					isDisable = true
					
					// disables button if no type selected or type already added
					addType.valueProperty().listen { new -> isDisable = (new == null) || noteTabs.any { it.text == new.name.value } }
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
						var note: Note? = null
						lateinit var tb: TitledPane
						tb = noteTab(this, addType.value.name.value, "", { text ->
							if(note == null) {
								note = Note.new(cell.time, "", addType.value, cell is Week)
							}
							note?.text?.set(text)
							updateCallback()
						}, {
							this.children.remove(noteTabs.first { it == tb })
							noteTabs.remove(tb)
							note?.remove()
							
							// triggers reload of add button to check if new note can be created
							add.isDisable = noteTabs.any { it.text == addType.value.name.value }
							updateCallback()
						})
						noteTabs.add(0, tb)
						add.isDisable = true
					}
					
					for(note in cell.notes) {
						lateinit var tb: TitledPane
						tb = noteTab(this, note.type.name, note.text.value, { text ->
							note.text.set(text)
							updateCallback()
						}, {
							this.children.remove(noteTabs.first { it == tb })
							noteTabs.remove(tb)
							note.remove()
							
							// triggers reload of add button to check if new note can be created
							add.isDisable = noteTabs.any { it.text == addType.value.name.value }
							updateCallback()
						})
						noteTabs.add(tb)
					}
				}
			}
		}
	}
}

fun noteTab(tabs: VBox, title: String, text: String, saveFun: (String) -> Unit, deleteFun: () -> Unit): TitledPane {
	// cannot use the EventTarget Functions because they automatically add the
	// pane to the end of the vbox
	val pane = TitledPane()
	pane.apply {
		setText(title)
		isExpanded = getConfig(Configs.ExpandNotesOnOpen)
		addClass(NoteStyles.notesPane_)
		
		lateinit var save: Button
		
		graphic = toolbar {
			addClass(NoteStyles.paneToolbar_)
			hbox(spacing = 20.0) {
				style {
					fontSize = 15.px
				}
				save = button("create".translate(Language.TranslationTypes.Note))
				
				button("delete".translate(Language.TranslationTypes.Note)) {
					action { deleteFun() }
				}
			}
		}
		contentDisplay = ContentDisplay.RIGHT
		expandedProperty().listen { new -> contentDisplay = if(new) ContentDisplay.RIGHT else ContentDisplay.TEXT_ONLY }
		
		htmleditor(text) {
			addClass(GlobalStyles.disableFocusDraw_)
			addClass(NoteStyles.editor_)
			this.getChildList()
			save.action {
				saveFun(this@htmleditor.htmlText)
				save.text = "save".translate(Language.TranslationTypes.Note)
			}
		}
	}
	
	// set tab as first
	tabs.getChildList()?.add(0, pane)
	
	return pane
}
