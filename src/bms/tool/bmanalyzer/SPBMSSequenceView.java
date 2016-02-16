package bms.tool.bmanalyzer;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import bms.model.*;

/**
 * SP譜面ビューア
 * 
 * @author exch
 */
public class SPBMSSequenceView extends BMSSequenceView implements Initializable {

	@FXML
	private ScrollPane sequence;
	@FXML
	private ComboBox<String> playside;
	@FXML
	private ComboBox<String> option;
	@FXML
	private TextField keyorder;
	@FXML
	private ComboBox<Double> hispeed;

	private static final String[] PLAYSIDE = { "left", "right" };

	private int[] random = { 0, 1, 2, 3, 4, 5, 6 };

	public void initialize(URL arg0, ResourceBundle arg1) {
		playside.getItems().setAll(PLAYSIDE);
		playside.setValue(PLAYSIDE[0]);
		option.getItems().setAll(OPTION);
		option.setValue(OPTION[0]);
		hispeed.getItems().setAll(HISPEED);
		hispeed.setValue(HISPEED[0]);
		this.setScrollPane(sequence);
		setDrawProperty();
		this.setHispeedSelector(hispeed);
	}
	
	private void setDrawProperty() {
		double[] lanex = PLAYSIDE[0].equals(playside.getValue()) ? new double[] {
			0.15, 0.25, 0.34, 0.44, 0.53, 0.63, 0.72, 0 }
			: new double[] { 0, 0.1, 0.19, 0.29, 0.38, 0.48, 0.57, 0.67 };
	double[] lanew = { 0.1, 0.09, 0.1, 0.09, 0.1, 0.09, 0.1, 0.15 };
	double[] playerx = { 0 };
	double[] playerw = { 0.82 };
	Color[] lanebg = { Color.rgb(24, 24, 24), Color.BLACK,
			Color.rgb(24, 24, 24), Color.BLACK, Color.rgb(24, 24, 24),
			Color.BLACK, Color.rgb(24, 24, 24), Color.BLACK };
	double timex = 0.82;
	double timew = 0.18;
	this.setDrawProperty(lanex, lanew, playerx, playerw, lanebg, timex,
			timew, 180.0);		
	}

	@Override
	protected int getKeyOrder(int lane, TimeLine tl, List<LongNote> scrlns) {
		int l = lane;
		if (OPTION[1].equals(option.getValue())) {
			l = (lane == 7 ? 7 : 6 - lane);
		}
		if (OPTION[2].equals(option.getValue())) {
			l = (lane == 7 ? 7 : random[lane]);
		}
		// TODO 現行のALL-SCRではLNのSCR化を行わない。行う場合はLN衝突回避ロジックの実相も必要
		if (OPTION[3].equals(option.getValue())) {
			int rn = -1;
			for (int j = 0; j < 7; j++) {
				if (tl.existNote(j) && tl.getNote(j) instanceof NormalNote) {
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
			if (!tl.existNote(7) && lane == rn && b) {
				l = 7;
			}
		}
		return l;
	}

	protected void drawNote(GraphicsContext gc, double x, double y,
			double width, double height, double scale, int lane, Note note) {
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

	public void playsideChanged() {
		setDrawProperty();
		this.repaint();
	}
	
	public void randomChanged() {
		this.checkRandom(keyorder, random);
		this.repaint();
	}
};
