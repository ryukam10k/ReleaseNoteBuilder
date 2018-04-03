package application;
	
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXMLLoader;

public class Main extends Application {
	
	public static String releaseNotePath;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			primaryStage.setTitle("基幹システムリリースノート作成ツール");

			AnchorPane root = (AnchorPane)FXMLLoader.load(getClass().getResource("Form.fxml"));
			Scene scene = new Scene(root,600,430);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		if (args.length == 0) {
			releaseNotePath = "\\\\webapsvr01\\releaseNote";
		} else {
			releaseNotePath = args[0];
		}
		launch(args);
	}
}
