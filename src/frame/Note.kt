package frame

import calendar.Celldisplay
import calendar.Day
import calendar.Note
import calendar.Timing.toUTCEpochSecond
import calendar.Types
import calendar.Week
import calendar.removeDayNote
import calendar.removeWeekNote
import calendar.saveDayNote
import calendar.saveWeekNote
import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import logic.Configs
import logic.LogType
import logic.getConfig
import logic.getLangString
import logic.log
import tornadofx.*
import java.time.temporal.ChronoField



fun createNoteTab(pane: TabPane, cell: Celldisplay, updateCallback: () -> Unit): Tab {
	log("creating note tab", LogType.IMPORTANT)
	return pane.tab("") {
		if(cell is Day)
			text = "Notes for ${cell.time.dayOfMonth}. ${getLangString(cell.time.month.name)}"
		else if(cell is Week)
			text = "Notes for ${cell.time.get(ChronoField.ALIGNED_WEEK_OF_MONTH)}. Week in ${getLangString(cell.time.month.name)}"
		
		isClosable = true
		stackpane {
			style(append = true) {
				padding = box(6.px)
			}
			
			vbox {
				addClass(Styles.Tabs.mainTab)
				style {
					padding = box(0.px, 0.px, 2.px, 0.px)
				}
				
				lateinit var add: Button
				lateinit var addType: ComboBox<String>
				
				val noteTabs = mutableListOf<TitledPane>()
				
				hbox(spacing = 20.0, alignment = Pos.CENTER_LEFT) {
					addClass(Styles.Tabs.topbar)
					style {
						padding = box(0.px, 15.px, 0.px, 15.px)
					}
					
					addType = combobox {
						items = Types.getTypes().map { it.name }.toObservable()
					}
					add = button {
						text = "Add"
						isDisable = true
						
						// disables button if no type selected or type already added
						addType.valueProperty().addListener { _, _, new -> isDisable = new == null || noteTabs.any { it.text == new } }
						addClass(Styles.Tabs.titleButtons)
					}
				}
				
				separate()
				
				scrollpane(fitToWidth = true) {
					style {
						prefHeight = Int.MAX_VALUE.px
					}
					vbox {
						add.action {
							val note = Note(cell.time.toUTCEpochSecond() / 60, "", Types.valueOf(addType.value), emptyList())
							lateinit var tb: TitledPane
							tb = noteTab(this, addType.value, true, "", {
								note.text = (it)
								if(cell is Day)
									saveDayNote(note)
								else if(cell is Week)
									saveWeekNote(note)
								updateCallback()
							}, {
								if(cell is Day)
									removeDayNote(note)
								else if(cell is Week)
									removeWeekNote(note)
								this.children.remove(noteTabs.first { it == tb })
								noteTabs.remove(tb)
								updateCallback()
							})
							noteTabs.add(0, tb)
							add.isDisable = true
						}
						
						for(note in cell.notes) {
							lateinit var tb: TitledPane
							tb = noteTab(this, note.type.name, false, note.text, {
								note.text = (it)
								if(cell is Day)
									saveDayNote(note)
								else if(cell is Week)
									saveWeekNote(note)
								updateCallback()
							}, {
								if(cell is Day)
									removeDayNote(note)
								else if(cell is Week)
									removeWeekNote(note)
								this.children.remove(noteTabs.first { it == tb })
								noteTabs.remove(tb)
								updateCallback()
							})
							
							noteTabs.add(tb)
						}
					}
				}
			}
			
			// used to shadow the overflow from tab
			pane {
				isMouseTransparent = true
				addClass(Styles.Tabs.shadowBorder)
			}
		}
	}
}

fun noteTab(tabs: VBox, title: String, new: Boolean, text: String, saveFun: (String) -> Unit, deleteFun: () -> Unit): TitledPane {
	// cannot use the EventTarget Functions because they automatically add the
	// pane to the end of the vbox
	val pane = TitledPane()
	pane.apply {
		setText(title)
		isExpanded = getConfig(Configs.ExpandNotesOnOpen)
		
		style {
			padding = box(1.px)
		}
		
		lateinit var save: Button
		
		graphic = toolbar {
			style {
				backgroundColor += Color.TRANSPARENT
				padding = box(0.px, 0.px, 0.px, 20.px)
			}
			hbox(spacing = 20.0) {
				style {
					fontSize = 15.px
				}
				save = button(if(new) getLangString("create") else getLangString("save"))
				
				button(getLangString("delete")) {
					action { deleteFun() }
				}
			}
		}
		contentDisplay = ContentDisplay.RIGHT
		expandedProperty().addListener { _, _, new -> contentDisplay = if(new) ContentDisplay.RIGHT else ContentDisplay.TEXT_ONLY }
		
		htmleditor(text) {
			//addClass(Styles.disableFocus)
			addClass(Styles.NoteTab.texteditor)
			save.action {
				saveFun(this@htmleditor.htmlText)
				save.text = getLangString("save")
			}
		}
	}
	
	// set tab as first
	tabs.getChildList()?.add(0, pane)
	
	return pane
}
