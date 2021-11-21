import javafx.beans.value.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MinuteSecondPicker extends VBox implements Initializable {
	
	private final DateTimePickerPopup parentContainer;
	
	@FXML
	private Slider slider;
	
	@FXML
	private Label label;
	
	MinuteSecondPicker(DateTimePickerPopup parentContainer) {
		this.parentContainer = parentContainer;
		
		// Load FXML
		try {
			final FXMLLoader fxmlLoader = new FXMLLoader(new File("src/datetimepicker/MinuteSecondPicker.fxml").toURI().toURL());
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			
			try {
				fxmlLoader.load();
			} catch(IOException ex) {
				// Should never happen.  If it does however, we cannot recover
				// from this
				throw new RuntimeException(ex);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		slider.setMin(0);
		slider.setMax(59);
		slider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				
				final int newValueInt = newValue.intValue();
				label.setText(String.format("%02d", newValueInt));
			}
		});
		slider.onMouseReleasedProperty().set(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				parentContainer.restoreTimePanel();
			}
		});
	}
	
	public int getValue() {
		return Integer.parseInt(label.getText());
	}
}
