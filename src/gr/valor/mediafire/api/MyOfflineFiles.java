package gr.valor.mediafire.api;

import gr.valor.mediafire.File;
import gr.valor.mediafire.Folder;
import gr.valor.mediafire.FolderItem;
import gr.valor.mediafire.database.Columns;
import gr.valor.mediafire.database.Mediabase;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MyOfflineFiles extends MyFiles {
	private final static String TAG = "MyOfflineFiles";
	private SQLiteDatabase db;
	private String parent;
	private final String ORDER = "ORDER BY " + Columns.Items.TYPE + " DESC, " + Columns.Items.NAME + " ASC";

	public MyOfflineFiles(Context context, SQLiteDatabase db) {
		this.context = context;
		this.db = db;
	}

	public Folder getFiles(String order) throws Exception {
		Log.d(TAG, "Getting " + parent + " folder from db");
		Folder currentFolder = Folder.getByFolderKey(db, parent);
		if (order == null) {
			order = ORDER;
		}
		Log.d(TAG, "Getting subitems from db");
		String sql = "SELECT " + Columns.Folders.FOLDERKEY + ", " + Columns.Files.QUICKKEY + ", " + Columns.Items.NAME + ", "
				+ Columns.Items.TYPE + ", " + Columns.Items.PARENT + ", " + Columns.Items.CREATED + ", " + Columns.Folders.FOLDERS + ", "
				+ Columns.Folders.FILES + ", " + Columns.Files.DOWNLOADS + ", " + Columns.Files.SIZE + ", " + Columns.Items.INSERTED + ", "
				+ Columns.Items.FLAG + ", " + Columns.Items.PRIVACY + ", " + Columns.Folders.SHARED + ", " + Columns.Folders.REVISION
				+ ", " + Columns.Folders.EPOCH + ", " + Columns.Folders.DROPBOX_ENABLED + ", " + Columns.Files.FILETYPE + ", "
				+ Columns.Files.PASSWORD_PROTECTED + " FROM " + Mediabase.TABLE_ITEMS + " i " + " LEFT JOIN " + Mediabase.TABLE_FOLDERS
				+ " fo" + " ON i." + Columns.Items.KEY + " = fo." + Columns.Folders.FOLDERKEY + " LEFT JOIN " + Mediabase.TABLE_FILES
				+ " fi " + " ON i." + Columns.Items.KEY + " = fi." + Columns.Files.QUICKKEY + " WHERE " + Columns.Items.PARENT + " = ? "
				+ order + ";";
		Cursor cur = db.rawQuery(sql, new String[] { parent });
		while (cur.moveToNext()) {
			String type = cur.getString(cur.getColumnIndex(Columns.Items.TYPE));
			if (type.equals(FolderItem.TYPE_FOLDER)) {
				currentFolder.subFolders.add(new Folder(cur));
			} else {
				currentFolder.files.add(new File(cur));
			}
		}

		cur.close();
		db.close();
		return currentFolder;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

}
