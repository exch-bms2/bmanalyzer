package bms.tool.bmanalyzer;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class BMSInformationLoader extends Application {

	private BMSInformationView bmsinfo;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		String path = null;
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
				HBox stackPane = (HBox) loader.load();
				bmsinfo = (BMSInformationView) loader.getController();
//				scene.getStylesheets().addAll("/bms/res/win7glass.css",
//						"/bms/res/style.css");
//				primaryStage.getIcons().addAll(this.primaryStage.getIcons());
				Scene scene = new Scene(stackPane, stackPane.getPrefWidth(),
						stackPane.getPrefHeight());

				primaryStage.setScene(scene);
				primaryStage.show();

				bmsinfo.update(path.replace("\\", "/"));
				primaryStage.setTitle("譜面情報:"
						+ bmsinfo.getModel().getFullTitle() + "[" + path
						+ "]");

			} catch (IOException e) {
				Logger.getGlobal().severe(e.getMessage());
				e.printStackTrace();
			}			
		}
	}

}
