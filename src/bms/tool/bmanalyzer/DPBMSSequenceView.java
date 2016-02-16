package bms.tool.bmanalyzer;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import bms.model.*;

/**
 * DP譜面ビューア
 * 
 * @author exch
 */
public class DPBMSSequenceView extends BMSSequenceView implements Initializable {

	@FXML
	private ScrollPane sequence;
	@FXML
	private ComboBox<String> playside;
	@FXML
	private ComboBox<String> option;
	@FXML
	private TextField keyorder;
	@FXML
	private ComboBox<String> option2;
	@FXML
	private TextField keyorder2;
	@FXML
	private ComboBox<Double> hispeed;

	private static final String[] PLAYSIDE = { "OFF", "ON" };

	private int[] random = { 0, 1, 2, 3, 4, 5, 6 };
	private int[] random2 = { 0, 1, 2, 3, 4, 5, 6 };

	public void initialize(URL arg0, ResourceBundle arg1) {
		playside.getItems().setAll(PLAYSIDE);
		playside.setValue(PLAYSIDE[0]);
		option.getItems().setAll(OPTION);
		option.setValue(OPTION[0]);
		option2.getItems().setAll(OPTION);
		option2.setValue(OPTION[0]);
		hispeed.getItems().setAll(HISPEED);
		hispeed.setValue(HISPEED[0]);
		this.setScrollPane(sequence);
		double[] lanex = { 0.08, 0.135, 0.185, 0.24, 0.29, 0.345, 0.395, 0,
				0.55, 0.605, 0.655, 0.71, 0.76, 0.815, 0.865, 0.92 };
		double[] lanew = { 0.055, 0.05, 0.055, 0.05, 0.055, 0.05, 0.055, 0.08,
				0.055, 0.05, 0.055, 0.05, 0.055, 0.05, 0.055, 0.08 };
		double[] playerx = { 0, 0.55 };
		double[] playerw = { 0.45, 0.45 };
		Color[] lanebg = { Color.rgb(24, 24, 24), Color.BLACK,
				Color.rgb(24, 24, 24), Color.BLACK, Color.rgb(24, 24, 24),
				Color.BLACK, Color.rgb(24, 24, 24), Color.BLACK,
				Color.rgb(24, 24, 24), Color.BLACK, Color.rgb(24, 24, 24),
				Color.BLACK, Color.rgb(24, 24, 24), Color.BLACK,
				Color.rgb(24, 24, 24), Color.BLACK };
		double timex = 0.45;
		double timew = 0.1;
		this.setDrawProperty(lanex, lanew, playerx, playerw, lanebg, timex,
				timew, 330.0);
		this.setHispeedSelector(hispeed);
	}

	@Override
	protected int getKeyOrder(int lane, TimeLine tl, List<LongNote> scrlns) {
		// FLIP処理
		if (playside.getValue().equals(PLAYSIDE[1])) {
			lane = (lane >= 8) ? lane - 8 : lane + 8;
		}
		String op = lane >= 8 ? option2.getValue() : option.getValue();
		int l = lane % 8;
		if (OPTION[1].equals(op)) {
			l = (l == 7 ? 7 : 6 - l);
		}
		if (OPTION[2].equals(op)) {
			l = (l == 7 ? 7 : (lane >= 8 ? random2[l] : random[l]));
		}
		// TODO 現行のALL-SCRではLNのSCR化を行わない。行う場合はLN衝突回避ロジックの実相も必要
		if (OPTION[3].equals(op)) {
			int rn = -1;
			for (int j = 0; j < 7; j++) {
				if (tl.existNote(j + (lane / 8) * 8)
						&& tl.getNote(j + (lane / 8) * 8) instanceof NormalNote) {
					rn = j;
				}
			}
			boolean b = true;
			for (LongNote ln : scrlns) {
				if (ln.getStart().getTime() < tl.getTime()
						&& ln.getEnd().getTime() >= tl.getTime()) {
					b = false;
				}
			}
			if (!tl.existNote(7 + (lane / 8) * 8) && l == rn && b) {
				l = 7;
			}
		}
		return l + (lane / 8) * 8;
	}
	
	protected void drawNote(GraphicsContext gc, double x, double y,
			double width, double height,double scale,  int lane, Note note) {
		if (note instanceof NormalNote) {
			gc.drawImage(
					noteimages[lane % 2 == 0 ? 0 : (lane % 8 == 7 ? 2 : 1)], x,
					y - 3 * scale, width, 6 * scale);
		}
		if (note instanceof LongNote) {
			int n = lane % 2 == 0 ? 0 : (lane % 8 == 7 ? 6 : 3);
			gc.drawImage(
					noteimages_ln[n], x,
					y - height - 2, width, height + 4);
			gc.drawImage(
					noteimages_ln[n + 1], x,
					y -  4, width, 6);
			gc.drawImage(
					noteimages_ln[n + 2], x,
					y - height - 2, width, 6);			
		}
		if (note instanceof MineNote) {
			gc.drawImage(noteimages[3], x, y - 3 * scale, width, 6 * scale);
		}
	}
	
	public void randomChanged() {
		this.checkRandom(keyorder, random);
		this.repaint();
	}
	
	public void random2Changed() {
		this.checkRandom(keyorder2, random2);
		this.repaint();
	}
};
