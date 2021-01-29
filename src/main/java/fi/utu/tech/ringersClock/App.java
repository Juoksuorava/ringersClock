package fi.utu.tech.ringersClock;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Clock;
import java.util.Timer;
import java.util.TimerTask;

import fi.utu.tech.ringersClock.UI.MainViewController;

/**
 * JavaFX App
 */
public class App extends Application {

	/*
	 * USER INTEFACE RELATED STUFF DO NOT TOUCH CODE IN THIS BLOCK
	 */
	private static Scene scene;
	private static MainViewController controller;
	private Gui_IO gio;
	private Clock clock;
	private Timer tt;
	/* BLOCK ENDS */

	/* Define your class variables here */

	/*
	 * This method starts the action. This one should interest you.
	 */
	private void startClient() {
		Parameters parameters = this.getParameters();
		String serverIP = parameters.getNamed().getOrDefault("host", "127.0.0.1");
		int port = Integer.parseInt(parameters.getNamed().getOrDefault("port", "3000"));
		gio = new Gui_IO(controller);
		controller.setGui_IO(gio);
		try {
			new ClockClient(serverIP, port, gio);
		} catch (IOException e)
		{ e.printStackTrace(); }
	}

	/*
	 * USER INTEFACE RELATED STUFF DO NOT TOUCH CODE UNDER THIS LINE
	 */
	@Override
	public void start(Stage stage) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("mainView.fxml"));
		Parent root = loader.load();
		controller = loader.getController();
		clock = Clock.systemDefaultZone();

		scene = new Scene(root);
		stage.setMinHeight(600);
		stage.setMinWidth(800);
		stage.setTitle("Ringers Clock");
		stage.setScene(scene);
		clockUpdate();
		stage.show();

		startClient();
	}

	@Override
	public void stop() {
		this.tt.cancel();
	}

	private void clockUpdate() {
		this.tt = new Timer("Update time");
		this.tt.schedule(new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						controller.setTime(clock.instant());
					}
				});
			}
		}, 0, 25);
	}

	static void setRoot(String fxml) throws IOException {
		scene.setRoot(loadFXML(fxml));
	}

	private static Parent loadFXML(String fxml) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
		return fxmlLoader.load();
	}


	public static void main(String[] args) {
		System.out.println("Program starting...");
		launch();
	}
}