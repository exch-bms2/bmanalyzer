package bms.tool.bmanalyzer;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

import javafx.animation.AnimationTimer;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import bms.model.*;

/**
 * 譜面情報閲覧用ビュー
 * 
 * @author exch
 */
public class BMSInformationView implements Initializable {

	// TODO 譜面密度分布描画の分離

	/**
	 * JUDGERANK表示用文字列
	 */
	public static final String[] JUDGERANK = { "VERYHARD", "HARD", "NORMAL",
			"EASY", "---" };
	public static final String[] JUDGERANK_COLOR = { "#aa0000", "#aa5500",
			"#aaaa00", "#00aa00", "#888888" };
	public static final String[] DIFFICULTY_COLOR = { "#888888", "#00aa00",
			"#0000aa", "#aaaa00", "#aa0000", "#aa00aa" };

	private Stage stage;

	@FXML
	private Label title;
	@FXML
	private Label genre;
	@FXML
	private Label artist;
	@FXML
	private Label playlevel;
	@FXML
	private Label judgerank;
	@FXML
	private Label totalnotes;
	@FXML
	private Label playtime;
	@FXML
	private Label total;
	@FXML
	private Label comment;
	@FXML
	private Label txt;

	@FXML
	private ImageView banner;
	@FXML
	private VBox randomview;
	@FXML
	private ComboBox<Integer> random;
	@FXML
	private Canvas graph;
	@FXML
	private Polygon graphCursor;
	@FXML
	private Polygon distCursor;
	@FXML
	private Label regionInfo;
	@FXML
	private VBox stack;
	@FXML
	private VBox sequence;
	@FXML
	private SPBMSSequenceView sequenceController;
	@FXML
	private VBox dpsequence;
	@FXML
	private DPBMSSequenceView dpsequenceController;
	@FXML
	private VBox pmssequence;
	@FXML
	private PMSSequenceView pmssequenceController;
	@FXML
	private Canvas notesduration;
	/**
	 * 表示中の譜面ビューア
	 */
	private BMSSequenceView selected;
	/**
	 * 表示中のBMS
	 */
	private BMSModel model;

	private String path;
	private double scale = 1.0;

	private int lntype;

	/**
	 * 譜面密度分布の最大値の上限
	 */
	private final int maxvalue = 100;
	/**
	 * 譜面密度分布の描画色
	 */
	private static final Color[] colors = new Color[] { Color.rgb(70, 255, 0),
			Color.RED, Color.rgb(128, 128, 255), Color.WHITE, Color.DARKRED };

	public void initialize(URL arg0, ResourceBundle arg1) {
		// CSS適用
		// File css = new File("css/bmsinformation.css");
		// this.getStylesheets().add(css.toURI().toString());

		graph.setEffect(new Reflection(0, 0.2, 0.3, 0));
		graph.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				// 左クリック時に指定位置まで譜面スクロール
				if (event.getButton() == MouseButton.PRIMARY
						&& event.getClickCount() == 1 && selected != null) {
					selected.setViewCursor((long) ((event.getX() / graph
							.getWidth()) * getRegionTime()));
				}
				// 右クリック時
				if (event.getButton() == MouseButton.SECONDARY
						&& event.getClickCount() == 1 && selected != null) {
					if (event.getX() > graphCursor.getLayoutX()) {
						distCursor.setLayoutX(event.getX());
						sequenceViewChanged();
					}
				}
			}
		});
		graphCursor.setOnMouseDragged(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				double x = graphCursor.getLayoutX() + event.getX();
				x = x < 0 ? 0 : (x > graph.getWidth() ? graph.getWidth() : x);
				selected.setViewCursor((long) ((x / graph.getWidth()) * getRegionTime()));
				event.consume();
			}
		});
		distCursor.setOnMouseDragged(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				double x = distCursor.getLayoutX() + event.getX();
				x = x < 0 ? 0 : (x > graph.getWidth() ? graph.getWidth() : x);
				distCursor.setLayoutX(x);
				sequenceViewChanged();
			}
		});

		sequenceController.setProperty(this);
		dpsequenceController.setProperty(this);
		pmssequenceController.setProperty(this);
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public BMSModel getModel() {
		return model;
	}

	public void setPatternScale(double d) {
		scale = d;
	}

	public double getPatternScale() {
		return scale;
	}

	public int getLntype() {
		return lntype;
	}

	public void setLntype(int lntype) {
		this.lntype = lntype;
	}

	public void update(String filepath) {
		this.path = filepath;
		filepath = filepath.replace("\\", "/");
		if (filepath.toLowerCase().endsWith(".bmson")) {
			BMSONDecoder decoder = new BMSONDecoder(lntype);
			model = decoder.decode(new File(filepath));
		} else {
			BMSDecoder decoder = new BMSDecoder(lntype);
			model = decoder.decode(new File(filepath));
		}
		// BMS格納ディレクトリ
		if (model.getRandom() > 1) {
			List<Integer> arg0 = new ArrayList<Integer>();
			for (int i = 1; i <= model.getRandom(); i++) {
				arg0.add(i);
			}
			random.getItems().setAll(arg0);
			random.setValue(1);
			randomview.setVisible(true);
		} else {
			randomview.setVisible(false);
		}
		if (stage != null) {
			stage.setTitle("譜面情報:" + model.getFullTitle() + "[" + path + "]");
		}
		update();
	}

	/**
	 * パス先のBMSファイルを読み込み、表示を更新する
	 * 
	 * @param filepath
	 *            BMSファイルのパス
	 */
	public void update() {
		String directorypath = path.substring(0, path.lastIndexOf("/") + 1);
		File txtpath = new File(directorypath);
		File[] txtFiles = txtpath.listFiles(getFileExtensionFilter(".txt"));

		StringBuffer buf = new StringBuffer();
		if (txtFiles.length == 0) {
			txt.setText("テキストファイルはありません。");
		} else {
			for (int i = 0; txtFiles.length > i; i++) {
				String str = null;
				try {
					// 読み込み準備
					BufferedReader br = new BufferedReader(
							new InputStreamReader(new FileInputStream(
									directorypath + txtFiles[i].getName()),
									"MS932"));
					// 一行づつ読み込み
					while ((str = br.readLine()) != null) {
						// 文字列追加
						buf.append(str + "\n");
					}
					// 終わったらclose
					br.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			txt.setText(buf.toString());
		}

		// テキスト折り返し
		title.setWrapText(true);
		title.setPrefWidth(600);
		title.setText(model.getFullTitle());
		artist.setWrapText(true);
		artist.setPrefWidth(600);
		artist.setText(model.getFullArtist());
		genre.setWrapText(true);
		genre.setPrefWidth(600);
		genre.setText(model.getGenre());
		playlevel.setText(String.valueOf(model.getPlaylevel()));
		playlevel.setStyle("-fx-text-fill:"
				+ DIFFICULTY_COLOR[model.getDifficulty()]);
		judgerank.setText(JUDGERANK[model.getJudgerank()]);
		judgerank.setStyle("-fx-text-fill:"
				+ JUDGERANK_COLOR[model.getJudgerank()]);
		int time = model.getLastTime() / 1000;
		playtime.setText(String.format("%d:%02d", time / 60, time % 60));

		// バナー画像
		File bannerpath = new File(directorypath + model.getBanner());
		if (model.getBanner().length() > 0 && bannerpath.exists()) {
			Image bannerimg = new Image(bannerpath.toURI().toString());
			banner.setImage(bannerimg);
		} else {
			Image bannerimg = BMSSequenceView.getImage("image/nobanner.png");
			banner.setImage(bannerimg);
		}

		banner.setEffect(new InnerShadow());

		// ノーツ分布の算出
		int times = this.getRegionTime();
		List<int[]> notes = new ArrayList<int[]>();
		int[] tn = new int[5];
		for (int j = 0; j < times; j += 1000) {
			int[] n = new int[6];

			n[0] = model.getTotalNotes(j, j + 1000, BMSModel.TOTALNOTES_KEY);
			n[1] = model.getTotalNotes(j, j + 1000,
					BMSModel.TOTALNOTES_LONG_KEY);
			n[2] = model
					.getTotalNotes(j, j + 1000, BMSModel.TOTALNOTES_SCRATCH);
			n[3] = model.getTotalNotes(j, j + 1000,
					BMSModel.TOTALNOTES_LONG_SCRATCH);
			n[4] = model.getTotalNotes(j, j + 1000, BMSModel.TOTALNOTES_MINE);

			tn[0] += n[0];
			tn[1] += n[1];
			tn[2] += n[2];
			tn[3] += n[3];
			tn[4] += n[4];

			n[5] = 0;
			double bpm = -1.0;
			for (int tl : model.getAllTimes()) {
				if (tl >= j && tl < j + 1000) {
					if (bpm != -1.0 && bpm != model.getTimeLine(tl).getBPM()) {
						n[5] = 1;
						break;
					}
				}
				bpm = model.getTimeLine(tl).getBPM();
			}
			notes.add(n);
		}
		// 本家TOTAL値の算出
		int totalnotes = tn[0] + tn[1] + tn[2] + tn[3];
		int totalval = (int) (7.605 * totalnotes / (0.01 * totalnotes + 6.5));
		totalval = (totalval > 260) ? totalval : 260;
		if (model.getTotal() > 0 && totalnotes > 0) {
			total.setText(String.valueOf(model.getTotal()) + " (ゲージ増加量: +"
					+ (((int) model.getTotal() * 100 / totalnotes) / 100.0)
					+ "%/Notes" + ", 本家推定TOTAL:" + totalval + " -  "
					+ (int) (model.getTotal() * 100 / totalval) + "%)");
		} else {
			total.setText("未定義");
		}
		String s = totalnotes + " (平均密度:"
				+ Math.round(totalnotes * 100.0 / time) / 100.0 + "Notes/s , ";
		if (model.getUseKeys() == 9) {
			s += "通常:" + tn[0] + " LN:" + tn[1] + ")";
		} else {
			s += "通常:" + tn[0] + " LN:" + tn[1] + " 皿:" + tn[2] + " LN皿:"
					+ tn[3] + ")";
		}
		if (tn[4] > 0) {
			s += " ※地雷ノート:" + tn[4];
		}
		this.totalnotes.setText(s);
		// その他情報
		comment.setText((model.getTotal() > 0 ? ("許容POOR数目安(EASY / NORMAL / HARD(1POOR10%減少)):"
				+ Math.round((model.getTotal() * 1.2 - 60) / 4.8)
				+ " / "
				+ Math.round((model.getTotal() - 60) / 6) + " / " + (70 / 10 + (totalnotes / 10 + 30) / 6))
				: ""));

		this.drawGraph(notes);
		// BMSの種類に応じて譜面ビューアの切り替え
		stack.getChildren().clear();
		// SP
		if (model.getUseKeys() == 5 || model.getUseKeys() == 7) {
			selected = sequenceController;
			stack.getChildren().setAll(sequence);
		}
		// DP
		if (model.getUseKeys() == 10 || model.getUseKeys() == 14) {
			selected = dpsequenceController;
			stack.getChildren().setAll(dpsequence);
		}
		// PMS
		if (model.getUseKeys() == 9) {
			selected = pmssequenceController;
			stack.getChildren().setAll(pmssequence);
		}
		if (selected != null) {
			selected.setModel(model);
			selected.setViewCursor(5000);
		}

		// 縦連分布の描画
		List<Integer> l = new ArrayList<Integer>();
		int[] notetimes = new int[18];
		for (TimeLine tl : model.getAllTimeLines()) {
			for (int lane = 0; lane < 18; lane++) {
				if (tl.existNote(lane)) {
					l.add(tl.getTime() - notetimes[lane]);
					notetimes[lane] = tl.getTime();
				}
			}
		}
		GraphicsContext gc = notesduration.getGraphicsContext2D();
		Integer[] noted = l.toArray(new Integer[0]);
		Arrays.sort(noted);
		int index = 0;
		final int[] judge = { 120, 310, 630, 10000000 };
		for (int i = 0; i < noted.length; i++) {
			gc.setFill(Color
					.hsb(240.0 * ((noted[i] > 1000 ? 1000 : noted[i]) / 1000.0) - 33,
							1.0, 1.0));
			gc.fillRect(notesduration.getWidth() * i / noted.length, 0,
					notesduration.getWidth() / noted.length,
					notesduration.getHeight());
			if (judge[index] < noted[i]) {
				gc.setStroke(Color.BLACK);
				gc.strokeLine(notesduration.getWidth() * i / noted.length, 0,
						notesduration.getWidth() * i / noted.length,
						notesduration.getHeight());
				index++;
			}
		}
	}

	/**
	 * 密度分布グラフの描画
	 * 
	 * @param data
	 */
	private void drawGraph(List<int[]> data) {
		// 最大密度に合わせてグラフの密度最大値を設定
		final int[] order = { 3, 2, 1, 0, 4, 0 };
		int max = 20;
		for (int i = 0; i < data.size(); i++) {
			int[] d = data.get(i);
			if (d[0] + d[1] + d[2] + d[3] + d[4] >= max) {
				max = ((int) (d[0] + d[1] + d[2] + d[3] + d[4]) / 10) * 10 + 10;
			}
		}
		max = max > maxvalue ? maxvalue : max;

		GraphicsContext gc = graph.getGraphicsContext2D();
		double x = 0;
		double y = 0;
		double w = graph.getWidth();
		double h = graph.getHeight();
		// 背景塗りつぶし
		gc.setFill(Color.rgb(48, 0, 0));
		gc.fillRect(x, y, w, h);
		// y軸補助線描画
		for (int i = 1; i < max; i++) {
			if (i % 10 == 0) {
				gc.setStroke(Color.rgb(60, 60, 160));
				gc.strokeLine(x, y + i * h / max, x + w, y + i * h / max);
			}
		}

		for (int i = 0; i < data.size(); i++) {
			// BPM変化地点描画
			int[] n = data.get(i);
			if (n[5] != 0) {
				gc.setFill(Color.rgb(128, 128, 0));
				gc.fillRect(w * i / data.size(), y, w / data.size(), h);
			}
			// x軸補助線描画
			if (i % 30 == 0) {
				gc.setStroke(Color.rgb(96, 96, 96));
				gc.strokeLine(x + i * w / data.size(), y,
						x + i * w / data.size(), y + h);
			}

			for (int j = 0, k = n[order[0]], index = 0; j < max
					&& index < colors.length;) {
				if (k > 0) {
					k--;
					j++;
					gc.setFill(colors[index]);
					gc.fillRect(w * i / data.size(), h - j * (h / max), w
							/ data.size() - 1, (h / max) - 1);
				} else {
					index++;
					k = n[order[index]];
				}
			}
		}
	}

	public void sequenceViewChanged() {
		int time = this.getRegionTime();
		graphCursor.setLayoutX(graph.getWidth() * selected.getViewCursor()
				/ time);
		if (distCursor.getLayoutX() < graphCursor.getLayoutX()) {
			distCursor.setLayoutX(graphCursor.getLayoutX());
		}
		int srctime = (int) (graphCursor.getLayoutX() / graph.getWidth() * time);
		int dsttime = (int) (distCursor.getLayoutX() / graph.getWidth() * time);
		int notes = model.getTotalNotes(srctime, dsttime);
		if (auto == null) {
			if (distCursor.getLayoutX() > graphCursor.getLayoutX() + 10
					&& model.getTotalNotes() > 0) {
				regionInfo.setText("選択範囲:"
						+ String.format("%d:%02d.%03d", srctime / 60000,
								(srctime / 1000) % 60, srctime % 1000)
						+ "〜"
						+ String.format("%d:%02d.%03d", dsttime / 60000,
								(dsttime / 1000) % 60, dsttime % 1000)
						+ "  ノート数:"
						+ notes
						+ ", 平均密度:"
						+ ((int) (notes
								/ ((distCursor.getLayoutX() - graphCursor
										.getLayoutX()) * time / 1000 / graph
											.getWidth()) * 10) / 10.0)
						+ "Notes/s, "
						+ " ゲージ増加量: +"
						+ ((notes * (int) model.getTotal() * 100 / model
								.getTotalNotes()) / 100.0) + "%)");
			} else {
				regionInfo.setText("");
			}
		}
	}

	public void randomChanged() {
		model.setSelectedIndexOfTimeLines(random.getValue());
		update();
	}

	public void reload() {
		update(path);
	}

	int getRegionTime() {
		return model.getLastTime() / 1000 * 1000 + 2000;
	}

	public void onDragOver(DragEvent ev) {
		Dragboard db = ev.getDragboard();
		if (db.hasFiles()) {
			ev.acceptTransferModes(TransferMode.COPY_OR_MOVE);
		}
		ev.consume();
	}

	/**
	 * ドラッグ＆ドロップ時の処理を行う
	 * 
	 * @param ev
	 *            ドラッグ＆ドロップ時に発生したイベント
	 */
	public void fileDragDropped(final DragEvent ev) {
		Dragboard db = ev.getDragboard();
		if (db.hasFiles()) {
			for (File f : db.getFiles()) {
				String path = f.getPath().toLowerCase();
				if (path.endsWith(".bmson") || path.endsWith(".bms")
						|| path.endsWith(".bme") || path.endsWith(".bml")) {
					update(f.getPath());
					return;
				}
			}
		}

	}

	private double[] getWeightNote(BMSModel model) {
		double[] d = new double[model.getLastTime() / 1000 + 1];
		boolean timeweight = true;
		double wt = 2.0;
		double wr = 1.5;
		double wk = 1.2;

		double[][] w = { { wt, wr, wk, 1, 1, 1, 1, 2 },
				{ wr, wt, wr, 1, 1, 1, 1, 2 }, { wk, wr, wt, 1, 1, 1, 1, 2 },
				{ 1, 1, 1, wt, wr, wk, wk, 1 }, { 1, 1, 1, wr, wt, wr, wk, 1 },
				{ 1, 1, 1, wk, wr, wt, wr, 1 }, { 1, 1, 1, wk, wk, wr, wt, 1 },
				{ 2, 2, 2, 1, 1, 1, 1, 2 } };
		int[] times = model.getAllTimes();
		boolean[] ln = new boolean[8];
		Arrays.fill(ln, false);
		TimeLine prev = null;
		int totalnotes = model.getTotalNotes();
		int bordernotes = totalnotes
				- (int) (totalnotes * 100 / model.getTotal());
		int nownotes = 0;
		for (int i = 0; i < times.length && times[i] <= model.getLastTime(); i++) {
			TimeLine tl = model.getTimeLine(times[i]);
			boolean existPrev = false;
			nownotes += tl.getTotalNotes();
			double last = ((timeweight && nownotes > bordernotes) ? (nownotes - bordernotes)
					* 1.0 / (totalnotes - bordernotes) + 1.0
					: 1.0);
			for (int j = 0; j < 8; j++) {
				if (tl.existNote(j)) {
					existPrev = true;
					double dd = 1.0;
					if (prev != null) {
						// 直前ノートからの重み処理
						for (int k = 0; k < 8; k++) {
							if (prev.existNote(k)) {
								dd = dd < w[j][k] ? w[j][k] : dd;
							}
						}
					}
					// 皿隣接処理
					if (j != 7 && tl.existNote(7) && w[7][j] > dd) {
						dd = w[7][j];
					}
					// LN隣接処理
					for (int k = 0; k < 8; k++) {
						if (ln[k] && w[j][k] > 1) {
							dd += 1;
							break;
						}
					}
					d[times[i] / 1000] += dd * last;
				}
			}
			if (existPrev) {
				prev = tl;
			}
			for (int j = 0; j < 8; j++) {
				if (tl.existNote(j) && tl.getNote(j) instanceof LongNote) {
					ln[j] = ln[j] ? false : true;
				}
			}
		}
		return d;
	}

	private AutoplayThread auto = null;

	/**
	 * 自動スクロールON/OFF切り替え
	 */
	public void autoplay() {
		if (auto == null) {
			auto = new AutoplayThread();
			auto.start();
		} else {
			auto.stop();
			auto = null;
		}
	}

	class AutoplayThread extends AnimationTimer {

		private long time = 0;

		@Override
		public void handle(long now) {
			if (time != 0) {
				selected.setViewCursor(selected.getViewCursor() + (now - time)
						/ 1000000);
			}
			time = now;
		}

	}

	/**
	 * フォルダ内に特定のキーワードを含むファイルを探す
	 * 
	 * @param extension
	 */
	public static FilenameFilter getFileExtensionFilter(String extension) {
		final String _extension = extension;
		return new FilenameFilter() {
			public boolean accept(File file, String name) {
				boolean ret = name.endsWith(_extension);
				return ret;
			}
		};
	}
}
