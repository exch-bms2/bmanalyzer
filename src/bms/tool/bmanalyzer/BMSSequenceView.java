package bms.tool.bmanalyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import bms.model.*;

/**
 * 譜面ビューア
 * 
 * @author exch
 */
public abstract class BMSSequenceView {

	// TODO S-RANDOMオプションの実装

	/**
	 * 譜面ビューアのスクロールパネル
	 */
	private ScrollPane sequence;
	/**
	 * 譜面描画Canvas連結用
	 */
	private VBox views;
	/**
	 * ハイスピード選択ボックス
	 */
	private ComboBox<Double> hispeed;
	/**
	 * 譜面表示するBMS
	 */
	private BMSModel model;

	/**
	 * Canvas分割単位
	 */
	private final int height = 1000;

	protected static final String[] OPTION = { "NORMAL", "MIRROR", "RANDOM",
			"ALL-SCR" };
	protected static final Double[] HISPEED = { 1.0, 1.5, 2.0, 2.5, 3.0, 3.5,
			4.0, 4.5, 5.0, 5.5, 6.0, 6.5, 7.0, 7.5, 8.0, 8.5, 9.0, 9.5, 10.0,
			10.5, 11.0, 11.5, 12.0, 12.5, 13.0, 13.5, 14.0, 14.5, 15.0 };

	private BMSInformationView info;
	protected Image[] noteimages = new Image[] {
			getImage("image/notes/notes_odd.png"),
			getImage("image/notes/notes_even.png"),
			getImage("image/notes/notes_sc.png"),
			getImage("image/notes/notes_mine.png") };
	protected Image[] noteimages_ln = new Image[] {
			getImage("image/notes/notes_odd_ln.png"),
			getImage("image/notes/notes_odd_lns.png"),
			getImage("image/notes/notes_odd_lne.png"),
			getImage("image/notes/notes_even_ln.png"),
			getImage("image/notes/notes_even_lns.png"),
			getImage("image/notes/notes_even_lne.png"),
			getImage("image/notes/notes_sc_ln.png"),
			getImage("image/notes/notes_sc_lns.png"),
			getImage("image/notes/notes_sc_lne.png") };

	private double[] lanex = new double[0];
	private double[] lanew = new double[0];
	private double[] playerx = new double[0];
	private double[] playerw = new double[0];
	private Color[] lanebg = new Color[0];
	private double timex = 0.0;
	private double timew = 0.0;
	private double width = 100.0;

	public BMSModel getModel() {
		return model;
	}

	public void setModel(BMSModel model) {
		this.model = model;
		if (model != null) {
			drawSequence();
		}
	}

	public void repaint() {
		if (model != null) {
			drawSequence();
		}
	}

	public void setProperty(BMSInformationView info) {
		this.info = info;
	}

	/**
	 * 譜面描画後に移動するカーソル位置。ハイスピード変更時にJavaFX Runtime側で 勝手にカーソル位置を変えてしまうため、この値で塗り替える
	 */
	private int viewtime = -1;

	protected void setScrollPane(ScrollPane seq) {
		this.sequence = seq;
		views = new VBox();
		sequence.setContent(views);
		sequence.vvalueProperty().addListener(new ChangeListener<Number>() {
			/**
			 * 無限ループ防止用
			 */
			private boolean lock = false;

			public void changed(ObservableValue<? extends Number> arg0,
					Number arg1, Number arg2) {
				if (!lock) {
					if (viewtime >= 0) {
						lock = true;
						setViewCursor(viewtime);
						viewtime = -1;
						lock = false;
					}
					info.sequenceViewChanged();
				}
			}
		});
	}

	public int getViewCursor() {
		double h = getHeight();
		double rate = h / (h - sequence.getHeight());
		return (int) ((1.0 - sequence.getVvalue()) * getRegionTime() / rate);
	}

	public void setViewCursor(long time) {
		if (sequence != null) {
			double h = getHeight();
			double rate = h / (h - sequence.getHeight());
			sequence.setVvalue(1.0 - (double) (time) / getRegionTime() * rate);
		}
	}

	protected int getRegionTime() {
		return info.getRegionTime();
	}

	protected void checkRandom(TextField keyorder, int[] random) {
		boolean illegal = true;
		if (keyorder.getText().length() == random.length) {
			String s = keyorder.getText();
			int[] key = new int[random.length];
			Arrays.fill(key, -1);
			for (int i = 0; i < random.length; i++) {
				int index = s.indexOf(String.valueOf(i + 1).charAt(0));
				if (index != -1 && key[i] == -1) {
					key[i] = index;
				} else {
					break;
				}
				if (i == random.length - 1) {
					for (int j = 0; j < random.length; j++) {
						random[j] = key[j];
					}
					illegal = false;
				}
			}
		}
		if (illegal) {
			int[] index = new int[random.length];
			for (int i = 0; i < random.length; i++) {
				index[random[i]] = i;
			}
			String s = "";
			for (int i = 0; i < random.length; i++) {
				s += index[i] + 1;
			}
			keyorder.setText(s);
		}

	}

	protected void setDrawProperty(double[] lanex, double[] lanew,
			double[] playerx, double[] playerw, Color[] lanebg, double timex,
			double timew, double width) {
		this.lanex = lanex;
		this.lanew = lanew;
		this.playerx = playerx;
		this.playerw = playerw;
		this.lanebg = lanebg;
		this.timex = timex;
		this.timew = timew;
		this.width = width;
	}

	protected void setHispeedSelector(ComboBox<Double> box) {
		hispeed = box;
		hispeed.valueProperty().addListener(new ChangeListener<Double>() {
			public void changed(ObservableValue<? extends Double> arg0,
					Double arg1, Double arg2) {
				double h = getRegionTime() / 10 * info.getPatternScale() * arg1;
				double rate = h / (h - sequence.getHeight());
				viewtime = (int) ((1.0 - sequence.getVvalue())
						* getRegionTime() / rate);
				int time = getViewCursor();
				drawSequence();
				setViewCursor(time);
			}
		});
	}

	private double getHeight() {
		int regiontime = this.getRegionTime();
		return regiontime / 10 * info.getPatternScale() * hispeed.getValue();
	}

	public void drawSequence() {
		BMSModel model = this.getModel();
		int starttime = 0;
		int regiontime = this.getRegionTime();
		double w = this.width;
		double h = getHeight();
		views.getChildren().clear();
		for (int i = 0; i < h; i += height) {
			Canvas canvas = new Canvas(w, h - i > height ? height : h - i);
			views.getChildren().add(canvas);
		}
		views.prefHeight(h);
		// this.checkRandom(keyorder, random);
		// 背景描画
		for (int lane = 0; lane < lanex.length; lane++) {
			double x = lanex[lane] * w;
			double dx = lanew[lane] * w;
			this.fillRect(x, 0, dx, h, lanebg[lane]);
			this.strokeLine(x, 0, x, h, Color.rgb(64, 64, 64));
			this.strokeLine(x + dx, 0, x + dx, h, Color.rgb(64, 64, 64));
		}
		// 時間軸描画
		this.fillRect(timex * w, 0, timew * w, h, Color.DARKGREEN);
		int[] times = model.getAllTimes();
		for (int i = 0; i < regiontime; i += 1000) {
			double y = h - (i - starttime) * h / regiontime;
			this.strokeLine(timex * w, y, (timex + timew) * w, y,
					Color.rgb(0, 255, 0));
			int time = i / 1000;
			this.strokeText((time / 60) + ":" + (time % 60 < 10 ? "0" : "")
					+ (time % 60), timex * w, y - 2, Color.rgb(0, 255, 0),
					new Font(12));
		}
		int bpm = 0;
		List<LongNote> scrlns = new ArrayList<LongNote>();
		for (int i = 0; i < times.length; i++) {
			if (starttime <= times[i] && times[i] <= starttime + regiontime) {
				TimeLine tl = model.getTimeLine(times[i]);
				double y = h - (times[i] - starttime) * h / regiontime;
				// BPM変化描画
				if ((int) tl.getBPM() != bpm) {
					bpm = (int) tl.getBPM();
					for (int p = 0; p < playerx.length; p++) {
						this.strokeLine(playerx[p] * w, y,
								(playerx[p] + playerw[p]) * w, y, Color.DARKRED);
						this.strokeText("BPM" + bpm, playerx[p] * w + 60,
								y - 2, Color.DARKRED, new Font(12));
					}
				}
				// 小節線描画
				if (tl.getSectionLine()) {
					for (int p = 0; p < playerx.length; p++) {
						this.strokeLine(playerx[p] * w, y,
								(playerx[p] + playerw[p]) * w, y, Color.GRAY);
					}
				}
				// ノート描画
				for (int lane = 0; lane < lanex.length; lane++) {
					Note note = tl
							.getNote(model.getUseKeys() == 9 && lane >= 5 ? lane + 5
									: (model.getUseKeys() > 9 && lane >= 8 ? lane + 1
											: lane));
					if (note != null) {
						int l = this.getKeyOrder(lane, tl, scrlns);
						double x = lanex[l] * w;
						double dx = lanew[l] * w;
						double dy = 1;
						if (note instanceof LongNote) {
							if (((LongNote) note).getStart() == tl) {
								if (l % 8 == 7) {
									scrlns.add((LongNote) note);
								}
								// if (((LongNote) note).getEnd() == null) {
								// Logger.getGlobal().warning(
								// "LN終端がなく、モデルが正常に表示されません。LN開始時間:"
								// + ((LongNote) note)
								// .getStart()
								// .getTime());
								// } else {
								dy = (((LongNote) note).getEnd().getTime() - ((LongNote) note)
										.getStart().getTime()) * h / regiontime;
								// }
							} else {
								dy = 0;
							}
						}
						if (dy > 0) {
							double d = 0;
							for (Node n : views.getChildren()) {
								if (y - dy <= d + height + 10 && y >= d - 10) {
									GraphicsContext gc = ((Canvas) n)
											.getGraphicsContext2D();
									this.drawNote(gc, x, y - d, dx, dy,
											info.getPatternScale(), l, note);
								}
								d += height;
							}
						}
					}
				}
			}
		}
	}

	private void fillRect(double x, double y, double w, double h, Paint p) {
		double d = 0;
		for (Node n : views.getChildren()) {
			if (y <= d + height && y + h >= d) {
				double dy = y - d >= 0 ? y - d : 0;
				GraphicsContext gc = ((Canvas) n).getGraphicsContext2D();
				gc.setFill(p);
				gc.fillRect(x, dy, w, y + h - d - dy > height ? height : y + h
						- d - dy);
			}
			d += height;
		}
	}

	private void strokeLine(double x, double y, double dx, double dy, Paint p) {
		double d = 0;
		for (Node n : views.getChildren()) {
			if (y <= d + height && dy >= d) {
				double yy = y - d >= 0 ? y - d : 0;
				GraphicsContext gc = ((Canvas) n).getGraphicsContext2D();
				gc.setStroke(p);
				gc.strokeLine(x, yy, dx, dy - d > height ? height : dy - d);
			}
			d += height;
		}
	}

	private void strokeText(String s, double x, double y, Paint p, Font font) {
		double d = 0;
		for (Node n : views.getChildren()) {
			if (y <= d + height + font.getSize() * 2 && y >= d) {
				GraphicsContext gc = ((Canvas) n).getGraphicsContext2D();
				gc.setStroke(p);
				gc.setFont(font);
				gc.strokeText(s, x, y - d);
			}
			d += height;
		}
	}

	protected abstract int getKeyOrder(int lane, TimeLine tl,
			List<LongNote> scrlns);

	protected abstract void drawNote(GraphicsContext gc, double x, double y,
			double width, double height, double scale, int lane, Note note);
	
	/**
	 * 画像をImage形式で取得
	 * 
	 * @param imgName
	 *            イメージ名
	 * @return イメージストリーム
	 */
	public static Image getImage(String imgName) {
		return new Image(ClassLoader.getSystemResourceAsStream(imgName));
	}


};
