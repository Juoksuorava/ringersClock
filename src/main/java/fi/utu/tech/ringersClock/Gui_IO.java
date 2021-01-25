package fi.utu.tech.ringersClock;

import java.time.Instant;
import java.util.ArrayList;
import fi.utu.tech.ringersClock.entities.*;
import fi.utu.tech.ringersClock.UI.MainViewController;
import fi.utu.tech.ringersClock.entities.WakeUpGroup;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class Gui_IO {

	private MainViewController cont;

	public Gui_IO(MainViewController cont) {
		this.cont = cont;
	}

	/*
	 * Method for displaying the time of the alarm Use this method to set the alarm
	 * time in the UI The time is received from the server
	 * 
	 * DO not edit
	 */
	public void setAlarmTime(Instant time) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				cont.setAlarmTime(time);
			}
		});
	}

	
	/*
	 * Method for clearing the time of the alarm. Use this method when alarm is cancelled.
	 * 
	 * DO not edit
	 */
	public void clearAlarmTime() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				cont.clearAlarmTime();
			}
		});
	}
	
	/*
	 * Method for appending text to the status display You can write status messages
	 * to UI using this method.
	 * 
	 * DO not edit
	 */
	public void appendToStatus(String text) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				cont.appendToStatus(text);
			}
		});
	}

	/*
	 * Method for filling the existing wake-up groups to Choosebox Must run every
	 * time when the existing wake-up groups are receiver from the server
	 *
	 *DO not edit
	 */
	public void fillGroups(ArrayList<WakeUpGroup> list) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				cont.fillGroups(list);
			}
		});
	}

	/*
	 * This method is for waking up the ringer when it is time.
	 * 
	 * DO not edit
	 */

	public void alarm() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				cont.alarm();
				cont.clearAlarmTime();
			}
		});
	}

	/*
	 * This method must only on the client is the group leader. The idea is to use
	 * this method to confirm the wake up before waking up the rest of the team
	 * 
	 * DO not edit
	 */

	public void confirmAlarm(WakeUpGroup group) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Confirm alarm");
				alert.setHeaderText("Do you want wake up the team?");
				alert.setContentText("The weather seems to be ok.");
				alert.showAndWait().ifPresent((btnType) -> {
					if (btnType == ButtonType.OK) {
						AlarmAll(group);
					} else {
						CancelAlarm(group);
					}
				});
			}
		});
	}

	/*
	 * This method is run if the leader accepts the wake-up Now you have to wake up
	 * the rest of the team
	 */
	public void AlarmAll(WakeUpGroup group) {
		System.out.println("AlarmAll " + group.getName());
		ClockClient.send(new ClientCall<>(ClientCallType.ALARM_ALL));
	}

	/*
	 * This method is run if the leader cancel the wake-up The alarm is cancelled
	 * and should be removed from server
	 */
	public void CancelAlarm(WakeUpGroup group) {
		System.out.println("CancelAll " + group.getName());
		ClockClient.send(new ClientCall<>(ClientCallType.CANCEL_ALARM));
	}

	/*
	 * This method is run when user pressed the create button Now the group with
	 * wake-up time must be sent to server
	 */
	public void createNewGroup(String name, Integer hour, Integer minute, boolean noRain, boolean tempPlus) {
		System.out.println("Create New Group pressed, name: " + name + " Wake-up time: " + hour + ":" + minute + " Rain allowed: " + noRain + " Temperature over 0 deg: " + tempPlus);
		Integer ID = ClockClient.getLocalPort();
		Alarm alarm = new Alarm(hour, minute, noRain, tempPlus);
		ClockClient.send(
				new ClientCall<WakeUpGroup>(
						ClientCallType.CREATE_WAKE_UP_GROUP, new WakeUpGroup(ID, name, alarm))
		);
	}

	/*
	 * This method is run when user pressed the join button The info must be sent to
	 * server
	 */

	public void joinGroup(WakeUpGroup group) {
		System.out.println("Join Group pressed" + group.getName());
		ClockClient.send(
				new ClientCall<WakeUpGroup>(
						ClientCallType.JOIN_WAKE_UP_GROUP, group)
		);
	}
	
	/*
	 * This method is run when user pressed the resign button The info must be sent to
	 * server
	 */
	public void resignGroup(WakeUpGroup value) {
		System.out.println("Resign Group pressed");
		ClockClient.send(
				new ClientCall<WakeUpGroup>(
				ClientCallType.RESIGN_WAKE_UP_GROUP, value)
		);
	}

	public void confirmAlarm() {
	}
}
