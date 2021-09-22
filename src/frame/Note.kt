package frame

import calendar.Celldisplay
import calendar.Day
import calendar.Types
import calendar.Week
import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import logic.LogType
import logic.getLangString
import logic.log
import tornadofx.*
import java.time.temporal.ChronoField



fun createnotetab(pane: TabPane, cell: Celldisplay): Tab {
	log("creating week tab", LogType.IMPORTANT)
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
				addClass(Styles.Tabs.maintab)
				style {
					padding = box(0.px, 0.px, 2.px, 0.px)
				}
				
				lateinit var add: Button
				lateinit var addtype: ComboBox<String>
				
				val notetabs = mutableListOf<TitledPane>()
				
				hbox(spacing = 20.0, alignment = Pos.CENTER_LEFT) {
					addClass(Styles.Tabs.topbar)
					style {
						padding = box(0.px, 15.px, 0.px, 15.px)
					}
					
					addtype = combobox {
						items = Types.getTypes().map { it.name }.toObservable()
					}
					add = button {
						text = "Add"
						isDisable = true
						// disables button if no type selected or type already added
						addtype.valueProperty().addListener { _, _, new -> isDisable = new == null || notetabs.any { it.text == new } }
						addClass(Styles.Tabs.titlebuttons)
					}
				}
				
				seperate()
				
				scrollpane(fitToWidth = true) {
					style {
						prefHeight = Int.MAX_VALUE.px
					}
					vbox {
						add.action {
							notetabs.add(notetab(this@vbox, addtype.value, "") { println("htmlsave for new note: $it") })
							add.isDisable = true
						}
						
						for(note in cell.notes) {
							notetabs.add(notetab(this@vbox, note.type.name, note.text) {
								println("htmlsave for $note: $it")
							})
						}
					}
				}
			}
			
			// used to shadow the overflow from tab
			pane {
				isMouseTransparent = true
				addClass(Styles.Tabs.shadowborder)
			}
		}
	}
}

fun notetab(tabs: VBox, title: String, text: String, savefun: (String) -> Unit): TitledPane {
	return tabs.titledpane(title = title) {
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
				save = button(getLangString("save")) {
					action {
					
					}
				}
				button(getLangString("delete")) {
				
				}
			}
		}
		contentDisplay = ContentDisplay.RIGHT
		expandedProperty().addListener { _, _, new -> contentDisplay = if(new) ContentDisplay.RIGHT else ContentDisplay.TEXT_ONLY }
		
		htmleditor(text) {
			//addClass(Styles.disablefocus)
			addClass(Styles.NoteTab.texteditor)
			save.action {
				savefun(this@htmleditor.htmlText)
			}
		}
	}
}
