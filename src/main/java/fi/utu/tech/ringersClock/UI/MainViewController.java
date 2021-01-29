package fi.utu.tech.ringersClock.UI;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import fi.utu.tech.ringersClock.Gui_IO;
import fi.utu.tech.ringersClock.entities.WakeUpGroup;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.scene.control.ChoiceBox;

/*
 * Control class for JavaFX UI. Don't edit this file.
 * 
 */

public class MainViewController {
	@FXML
	private Label currentTime;
	@FXML
	private Label alarmTime;
	@FXML
	private CheckBox noRain;
	@FXML
	private CheckBox temp;
	@FXML
	private Button createButton;
	@FXML
	private Button joinButton;
	@FXML
	private Button resignButton;
	@FXML
	private TextArea statusText;
	@FXML
	private TextField newGroupName;
	@FXML
	private ChoiceBox<Integer> newHour;
	@FXML
	private ChoiceBox<Integer> newMinute;
	@FXML
	private ChoiceBox<WakeUpGroup> existingGroup;
	@FXML
	private ImageView alarmPic;

	private Gui_IO gio;

	private DateTimeFormatter DATE_TIME_FORMATTER;

	public MainViewController() {
		super();
		DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
	}

	public void initialize() {
		statusText.setEditable(false);
		statusText.setWrapText(true);
		alarmPic.setVisible(false);

		for (int i = 0; i < 24; i++) {
			newHour.getItems().add(i);
		}
		newHour.setValue(0);
		for (int i = 0; i < 59; i = i + 5) {
			newMinute.getItems().add(i);
		}
		newMinute.setValue(0);
	}

	public void setGui_IO(Gui_IO gio) {
		this.gio = gio;
	}

	@FXML
	public void setTime(Instant time) {
		currentTime.setText(DATE_TIME_FORMATTER.format(time));
	}

	@FXML
	public void setAlarmTime(Instant time) {
		alarmTime.setText(DATE_TIME_FORMATTER.format(time));
	}

	@FXML
	public void clearAlarmTime() {
		alarmTime.setText("--:--");
	}

	@FXML
	public void alarm() {
		alarmPic.setVisible(true);
		FadeTransition ft = new FadeTransition(Duration.millis(500), alarmPic);
		ft.setFromValue(1.0);
		ft.setToValue(0);
		ft.setCycleCount(10);
		ft.setAutoReverse(true);
		ft.play();
		ft.setOnFinished(event -> alarmPic.setVisible(false));
	}

	@FXML
	public void appendToStatus(String text) {
		statusText.setText(statusText.getText() + text + "\n");
	}

	@FXML
	public void fillGroups(ArrayList<WakeUpGroup> groups) {
		ObservableList<WakeUpGroup> list = FXCollections.observableArrayList();
		list.clear();
		list.addAll(groups);
		existingGroup.setItems(list);
	}

	@FXML
	private void createButtonPressed(ActionEvent event) {
		if (!newGroupName.getText().equals("")) {
			gio.createNewGroup(newGroupName.getText(), newHour.getValue(), newMinute.getValue(), noRain.isSelected(), temp.isSelected());
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("No group name");
			alert.setHeaderText("The name of the group is missing");
			alert.showAndWait();
		}
	}

	@FXML
	private void joinButtonPressed(ActionEvent event) {
		if (!(existingGroup.getValue() == null)) {
			gio.joinGroup(existingGroup.getValue());
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("No group selected");
			alert.setHeaderText("You have to select a group form the menu");
			alert.showAndWait();
		}
	}

	@FXML
	private void resignButtonPressed(ActionEvent event) {
		if (!(existingGroup.getValue() == null)) {
			gio.resignGroup(existingGroup.getValue());
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("No group selected");
			alert.setHeaderText("Please select a group");
			alert.showAndWait();
		}
	}
}