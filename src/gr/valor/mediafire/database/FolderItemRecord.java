package gr.valor.mediafire.database;

import gr.valor.mediafire.ItemConstants;
import gr.valor.mediafire.Mediafire;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class FolderItemRecord implements ItemConstants {
	public static final String ROOT_NAME = "root";
	public static final String ROOT_KEY = "rootkey";

	public int id;
	public String parent = null;
	public boolean isFolder;
	public String desc;
	public String tags;
	private String created;
	public long inserted = 0L;
	public int flag;
	public String privacy;
	public String itemType;
	public ArrayList<FolderRecord> subFolders = new ArrayList<FolderRecord>();
	public ArrayList<FileRecord> files = new ArrayList<FileRecord>();
	List<Map<String, String>> folderItems = new ArrayList<Map<String, String>>();
	List<Map<String, String>> fileItems = new ArrayList<Map<String, String>>();

	protected abstract void createFromCursor(Cursor c);

	protected SQLiteDatabase getDb() {
		return Mediafire.getDb();
	}

	protected boolean isNew(String key) {
		Cursor cur = getDb().rawQuery(
				"SELECT " + Columns.Items.KEY + " FROM " + Mediabase.TABLE_ITEMS + " WHERE " + Columns.Items.KEY + "=?",
				new String[] { key });
		if (cur.getCount() > 0) {
			cur.close();
			return false;
		}
		cur.close();
		return true;
	}

	/**
	 * @param created
	 *            the created to set
	 */
	public void setCreated(String created) {
		this.created = created;
	}

	/**
	 * @return the created
	 */
	public String getCreated() {
		return created;
	}

}
