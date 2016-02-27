package bms.tool.bmanalyzer;

import java.util.*;

import bms.model.BMSModel;

public class MediaInformationView {
	
	public MediaInformation[] createInformation(BMSModel model) {
		List<MediaInformation>  l = new ArrayList<MediaInformation>();
		// TODO ログ追加
		return l.toArray(new MediaInformation[0]);
	}

	public static class MediaInformation {
		
		// メディアログ候補一覧
		// 音源重複
		// 音源未使用
		
		private boolean selected;
		
		private String path;
		
		private int status;

		public boolean isSelected() {
			return selected;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}
	}
}
