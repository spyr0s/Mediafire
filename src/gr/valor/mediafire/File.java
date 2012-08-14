package gr.valor.mediafire;

import gr.valor.mediafire.database.Columns;

import java.util.Map;

import android.database.Cursor;
import android.util.Log;

public class File extends FolderItem {
	private final static String TAG = "File";
	public static final String TYPE_MUSIC = "mp3";
	public String quickkey;
	public String filename;
	public int downloads;
	public long size;
	public Note note;
	public boolean isFolder = false;
	protected Map<String, String> map;
	public String fileType = FolderItem.TYPE_FILE;

	private static final String[] Q = new String[] { "", "K", "M", "G", "T", "P", "E" };

	public File(Cursor cur) {
		createFileFromCursor(cur);
	}

	public File() {
		super();
	}

	private void createFileFromCursor(Cursor cur) {
		quickkey = cur.getString(cur.getColumnIndex(Columns.Files.QUICKKEY));
		filename = cur.getString(cur.getColumnIndex(Columns.Items.NAME));
		parent = cur.getString(cur.getColumnIndex(Columns.Items.PARENT));
		created = cur.getString(cur.getColumnIndex(Columns.Items.CREATED));
		inserted = cur.getLong(cur.getColumnIndex(Columns.Items.CREATED));
		downloads = cur.getInt(cur.getColumnIndex(Columns.Files.DOWNLOADS));
		size = cur.getLong(cur.getColumnIndex(Columns.Files.SIZE));
		fileType = getFileType();
		Log.i(TAG, "Creating file " + fileType + " from cursor");
	}

	public String getFileType() {
		int pos = filename.lastIndexOf(".");
		if (pos > 0 && filename.length() > pos) {
			return filename.substring(pos + 1).toLowerCase();
		} else {
			return FolderItem.TYPE_FILE;
		}

	}

	class Note {
		public String subject;
		public String description;
	}

	public String getSize() {
		for (int i = 6; i > 0; i--) {
			double step = Math.pow(1024, i);
			if (size > step) {
				return String.format("%3.1f%s", size / step, Q[i]);
			}

		}
		return Long.toString(size);
	}

	@Override
	public String toString() {

		return this.filename + "(" + this.quickkey + ")";
	}

}
