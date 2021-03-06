package bms.tool.bmanalyzer;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.stage.FileChooser.ExtensionFilter;

public class BMSInformationLoader extends Application {

	private BMSInformationView bmsinfo;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		String path = null;
		int lntype = 0;
		for (String s : getParameters().getNamed().keySet()) {
			if(s.equals("lntype")) {
				String value = getParameters().getNamed().get(s);
				try {
					lntype = Integer.parseInt(value);
				} catch(NumberFormatException e) {
					
				}
			}
		}
		for (String s : getParameters().getUnnamed()) {
			File f = new File(s);
			if (f.exists()) {
				path = f.getAbsolutePath();
				break;
			}
		}
		if (path == null) {
			FileChooser chooser = new FileChooser();
			chooser.getExtensionFilters().setAll(
					new ExtensionFilter("Be Music Script File", "*.bms",
							"*.bme", "*.bml", "*.pms", "*.bmson"));
			chooser.setTitle("開く : BMSファイルを指定してください");
			File f = chooser.showOpenDialog(null);
			if (f != null) {
				path = f.getAbsolutePath();
			}
		}

		if(path != null) {
			try {
				FXMLLoader loader = new FXMLLoader(
						BMSInformationLoader.class
								.getResource("/bms/tool/bmanalyzer/BMSInformationView.fxml"));
				VBox stackPane = (VBox) loader.load();
				bmsinfo = (BMSInformationView) loader.getController();
				bmsinfo.setStage(primaryStage);
//				scene.getStylesheets().addAll("/bms/res/win7glass.css",
//						"/bms/res/style.css");
//				primaryStage.getIcons().addAll(this.primaryStage.getIcons());
				Scene scene = new Scene(stackPane, stackPane.getPrefWidth(),
						stackPane.getPrefHeight());

				primaryStage.setScene(scene);
				primaryStage.show();

				primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

					@Override
					public void handle(WindowEvent t) {
						Logger.getGlobal().info("BMAnalyzerを終了します");
						Platform.exit();
						System.exit(0);
					}
				});

				bmsinfo.setLntype(lntype);
				bmsinfo.update(path.replace("\\", "/"));

			} catch (IOException e) {
				Logger.getGlobal().severe(e.getMessage());
				e.printStackTrace();
			}			
		} else {
			Platform.exit();
			System.exit(0);			
		}
	}

}
