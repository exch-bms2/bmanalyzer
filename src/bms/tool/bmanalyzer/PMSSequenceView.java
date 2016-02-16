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
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import bms.model.*;

/**
 * PMS譜面ビューア
 * 
 * @author exch
 */
public class PMSSequenceView extends BMSSequenceView implements Initializable {

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

	private static final String[] PLAYSIDE = { "POP", "BM" };
	protected static final String[] OPTION = { "NORMAL", "MIRROR", "RANDOM" };

	protected Image[] popimages = new Image[] {
			getImage("image/notes/pop_w.png"),
			getImage("image/notes/pop_y.png"),
			getImage("image/notes/pop_g.png"),
			getImage("image/notes/pop_b.png"),
			getImage("image/notes/pop_r.png"),
			getImage("image/notes/pop_mine.png") };

	protected Image[] popimages_ln = new Image[] {
			getImage("image/notes/pop_w_ln.png"),
			getImage("image/notes/pop_w_lns.png"),
			getImage("image/notes/pop_w_lne.png"),
			getImage("image/notes/pop_y_ln.png"),
			getImage("image/notes/pop_y_lns.png"),
			getImage("image/notes/pop_y_lne.png"),
			getImage("image/notes/pop_g_ln.png"),
			getImage("image/notes/pop_g_lns.png"),
			getImage("image/notes/pop_g_lne.png"),
			getImage("image/notes/pop_b_ln.png"),
			getImage("image/notes/pop_b_lns.png"),
			getImage("image/notes/pop_b_lne.png"),
			getImage("image/notes/pop_r_ln.png"),
			getImage("image/notes/pop_r_lns.png"),
			getImage("image/notes/pop_r_lne.png") };

	private int[] random = { 0, 1, 2, 3, 4, 5, 6, 7, 8 };

	public void initialize(URL arg0, ResourceBundle arg1) {
		playside.getItems().setAll(PLAYSIDE);
		playside.setValue(PLAYSIDE[0]);
		option.getItems().setAll(OPTION);
		option.setValue(OPTION[0]);
		hispeed.getItems().setAll(HISPEED);
		hispeed.setValue(HISPEED[0]);
		this.setScrollPane(sequence);
		double[] lanex = { 0, 0.1, 0.19, 0.29, 0.38, 0.48, 0.57, 0.67, 0.76 };
		double[] lanew = { 0.1, 0.09, 0.1, 0.09, 0.1, 0.09, 0.1, 0.09, 0.1 };
		double[] playerx = { 0 };
		double[] playerw = { 0.86 };
		Color[] lanebg = new Color[] { Color.rgb(64, 64, 64),
				Color.rgb(64, 64, 0), Color.rgb(0, 64, 0), Color.rgb(0, 0, 64),
				Color.rgb(64, 0, 0), Color.rgb(0, 0, 64), Color.rgb(0, 64, 0),
				Color.rgb(64, 64, 0), Color.rgb(64, 64, 64) };
		double timex = 0.86;
		double timew = 0.14;
		this.setDrawProperty(lanex, lanew, playerx, playerw, lanebg, timex,
				timew, 220.0);
		this.setHispeedSelector(hispeed);
	}

	@Override
	protected int getKeyOrder(int lane, TimeLine tl, List<LongNote> scrlns) {
		int l = lane;
		if (OPTION[1].equals(option.getValue())) {
			l = 8 - lane;
		}
		if (OPTION[2].equals(option.getValue())) {
			l = random[lane];
		}
		return l;
	}

	protected void drawNote(GraphicsContext gc, double x, double y,
			double width, double height, double scale, int lane, Note note) {
		if (note instanceof NormalNote) {
			if (playside.getValue().equals(PLAYSIDE[0])) {
				gc.drawImage(popimages[lane >= 5 ? 8 - lane : lane], x, y - 5
						* scale, width, 10 * scale);
			} else {
				gc.drawImage(noteimages[lane % 2 == 0 ? 0 : 1], x, y - 3
						* scale, width, 6 * scale);
			}
		}
		if (note instanceof LongNote) {
			if (playside.getValue().equals(PLAYSIDE[0])) {
				int n = (lane >= 5 ? 8 - lane : lane) * 3;
				gc.drawImage(popimages_ln[n], x, y - height, width,
						height);
				gc.drawImage(popimages_ln[n + 1], x, y - 4, width, 6);
				gc.drawImage(popimages_ln[n + 2], x, y - height - 2, width, 6);
			} else {
				int n = (lane % 2) * 3;
				gc.drawImage(noteimages_ln[n], x, y - height - 2, width,
						height + 4);
				gc.drawImage(noteimages_ln[n + 1], x, y - 4, width, 6);
				gc.drawImage(noteimages_ln[n + 2], x, y - height - 2, width, 6);
			}
		}
		if (note instanceof MineNote) {
			if (playside.getValue().equals(PLAYSIDE[0])) {
				gc.drawImage(popimages[5], x, y - 5 * scale, width, 10 * scale);
			} else {
				gc.drawImage(noteimages[3], x, y - 3 * scale, width, 6 * scale);
			}
		}
	}

	public void randomChanged() {
		this.checkRandom(keyorder, random);
		this.repaint();
	}
};
