package gr.valor.mediafire.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class FolderRecord extends FolderItemRecord {

	public String folderKey;
	public String name;
	public int folderCount;
	public int fileCount;
	public int revision;
	public long epoch;
	public String dropboxEnabled;
	public String shared;

	public FolderRecord(SQLiteDatabase db, String folderkey) throws Exception {

		String sql = "SELECT " + Columns.Folders.FOLDERKEY + ", " + Columns.Items.NAME + ", " + Columns.Items.TYPE + ", "
				+ Columns.Items.PARENT + ", " + Columns.Items.CREATED + ", " + Columns.Folders.FOLDERS + ", " + Columns.Folders.FILES
				+ " , " + Columns.Items.INSERTED + ", " + Columns.Items.FLAG + " , " + Columns.Items.PRIVACY + ", "
				+ Columns.Folders.SHARED + ", " + Columns.Folders.REVISION + ", " + Columns.Folders.EPOCH + " , "
				+ Columns.Folders.DROPBOX_ENABLED + " FROM " + Mediabase.TABLE_ITEMS + " i " + " LEFT JOIN " + Mediabase.TABLE_FOLDERS
				+ " fo" + " ON i." + Columns.Items.KEY + " = fo." + Columns.Folders.FOLDERKEY + " WHERE " + Columns.Items.KEY + " = ?;";
		Cursor cur = db.rawQuery(sql, new String[] { folderKey });
		if (cur.getCount() != 1) {
			cur.close();
			throw new Exception(folderkey + " folder not found in database");

		}
		cur.moveToFirst();
		createFromCursor(cur);
		cur.close();

	}

	@Override
	protected void createFromCursor(Cursor cur) {
		folderKey = cur.getString(cur.getColumnIndex(Columns.Folders.FOLDERKEY));
		name = cur.getString(cur.getColumnIndex(Columns.Items.NAME));
		parent = cur.getString(cur.getColumnIndex(Columns.Items.PARENT));
		created = cur.getString(cur.getColumnIndex(Columns.Items.CREATED));
		inserted = cur.getLong(cur.getColumnIndex(Columns.Items.INSERTED));
		privacy = cur.getString(cur.getColumnIndex(Columns.Items.PRIVACY));
		folderCount = cur.getInt(cur.getColumnIndex(Columns.Folders.FOLDERS));
		fileCount = cur.getInt(cur.getColumnIndex(Columns.Folders.FILES));
		revision = cur.getInt(cur.getColumnIndex(Columns.Folders.REVISION));
		epoch = cur.getLong(cur.getColumnIndex(Columns.Folders.EPOCH));
		dropboxEnabled = cur.getString(cur.getColumnIndex(Columns.Folders.DROPBOX_ENABLED));
		shared = cur.getString(cur.getColumnIndex(Columns.Folders.SHARED));
	}

}
