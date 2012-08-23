package gr.valor.mediafire.database;

import gr.valor.mediafire.Folder;
import gr.valor.mediafire.FolderItem;

import java.util.ArrayList;
import java.util.Iterator;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FolderRecord extends FolderItemRecord {

	public static final String TAG = "FolderRecord";

	public String folderKey;
	public String name;
	public int folderCount;
	public int fileCount;
	public int revision;
	public long epoch;
	public String dropboxEnabled;
	public String shared;

	public ArrayList<FolderRecord> subFolders = new ArrayList<FolderRecord>();
	public ArrayList<FileRecord> files = new ArrayList<FileRecord>();

	private boolean fullImport;

	public FolderRecord() {
		name = ROOT_NAME;
		folderKey = ROOT_KEY;
	}

	public FolderRecord(Cursor cur) {
		createFromCursor(cur);
	}

	public FolderRecord(SQLiteDatabase db, String fk) throws Exception {

		String sql = "SELECT " + Columns.Folders.FOLDERKEY + ", " + Columns.Items.NAME + ", " + Columns.Items.TYPE + ", "
				+ Columns.Items.PARENT + ", " + Columns.Items.CREATED + ", " + Columns.Folders.FOLDERS + ", " + Columns.Folders.FILES
				+ " , " + Columns.Items.INSERTED + ", " + Columns.Items.FLAG + " , " + Columns.Items.PRIVACY + ", "
				+ Columns.Folders.SHARED + ", " + Columns.Folders.REVISION + ", " + Columns.Folders.EPOCH + " , "
				+ Columns.Folders.DROPBOX_ENABLED + " FROM " + Mediabase.TABLE_ITEMS + " i " + " LEFT JOIN " + Mediabase.TABLE_FOLDERS
				+ " fo" + " ON i." + Columns.Items.KEY + " = fo." + Columns.Folders.FOLDERKEY + " WHERE " + Columns.Items.KEY + " = ?;";
		Cursor cur = db.rawQuery(sql, new String[] { fk });
		if (cur.getCount() != 1) {
			cur.close();
			throw new Exception(fk + " folder not found in database");

		}
		cur.moveToFirst();
		createFromCursor(cur);
		cur.close();

	}

	public void updateDb(SQLiteDatabase db, boolean fullImport) {
		Log.i(TAG, "Updating db");
		if (fullImport) {
			this.fullImport = true;
			Mediabase.truncateTables(db);
		}
		insertFolderInDb(db, this, true);

	}

	private void insertFolderInDb(SQLiteDatabase db, FolderRecord folderRecord, boolean isRoot) {

		folderRecord.save(db);
		for (Iterator<FolderRecord> it = folderRecord.subFolders.iterator(); it.hasNext();) {
			FolderRecord f = it.next();
			insertFolderInDb(db, f, fullImport);
		}
		for (Iterator<FileRecord> it = folderRecord.files.iterator(); it.hasNext();) {
			FileRecord f = it.next();
			f.save(db);
		}

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

	public boolean save(SQLiteDatabase db) {
		long now = (folderKey == Folder.ROOT_KEY) ? System.currentTimeMillis() / 1000 : 0;
		if (!this.isNew(db, folderKey)) {
			String queryItems = "UPDATE " + Mediabase.TABLE_ITEMS + " SET " + Columns.Items.KEY + "= ? ," + Columns.Items.TYPE + " = ? ,"
					+ Columns.Items.PARENT + " = ? , " + Columns.Items.NAME + " = ? ," + Columns.Items.DESC + " =? ," + Columns.Items.TAGS
					+ "=? , " + Columns.Items.FLAG + "=? ," + Columns.Items.PRIVACY + "=?," + Columns.Items.CREATED + "=?  " + " WHERE "
					+ Columns.Items.KEY + " = ? ";
			Object[] paramItems = new Object[] { folderKey, FolderItem.TYPE_FOLDER, parent, name, desc, tags, flag, privacy, created,
					folderKey };
			db.execSQL(queryItems, paramItems);
			String queryFolder = "UPDATE " + Mediabase.TABLE_FOLDERS + " SET " + Columns.Folders.FOLDERKEY + " = ? ,"
					+ Columns.Folders.FOLDERS + "=? ," + Columns.Folders.SHARED + "=? ," + Columns.Folders.REVISION + "=?,"
					+ Columns.Folders.EPOCH + "=? ," + Columns.Folders.DROPBOX_ENABLED + "=?," + Columns.Folders.FILES + " = ? "
					+ " WHERE " + Columns.Folders.FOLDERKEY + "= ?";
			Object[] paramFolders = new Object[] { folderKey, folderCount, shared, revision, epoch, dropboxEnabled, fileCount, folderKey };
			db.execSQL(queryFolder, paramFolders);
		} else {
			db.execSQL("INSERT INTO " + Mediabase.TABLE_ITEMS + "(" + Columns.Items.KEY + "," + Columns.Items.TYPE + ","
					+ Columns.Items.PARENT + "," + Columns.Items.NAME + "," + Columns.Items.DESC + "," + Columns.Items.TAGS + ","
					+ Columns.Items.FLAG + "," + Columns.Items.PRIVACY + "," + Columns.Items.CREATED + ", " + Columns.Items.INSERTED + ")"
					+ " VALUES (?,?,?,?,?,?,?, ?, ? , ?)", new Object[] { folderKey, FolderItem.TYPE_FOLDER, parent, name, desc, tags,
					flag, privacy, created, now });

			db.execSQL("INSERT OR IGNORE INTO " + Mediabase.TABLE_FOLDERS + "(" + Columns.Folders.FOLDERKEY + "," + Columns.Folders.FOLDERS
					+ "," + Columns.Folders.SHARED + "," + Columns.Folders.REVISION + "," + Columns.Folders.EPOCH + ","
					+ Columns.Folders.DROPBOX_ENABLED + "," + Columns.Folders.FILES + ")" + " VALUES (?,?,?,?,?,?,?)", new Object[] {
					folderKey, folderCount, shared, revision, epoch, dropboxEnabled, fileCount });
		}

		return true;
	}

	public String descr() {
		return "[folderkey:" + this.folderKey + ", parent:" + this.parent + ", name: " + this.name + ", desc:" + this.desc + ", tags:"
				+ this.tags + ", created:" + this.created + "]";
	}

	public boolean isCached(long cacheDuration) {
		Log.d(TAG, "Checking for cached folder in cache dur:" + cacheDuration);
		Log.d(TAG, "now:" + (System.currentTimeMillis() / 1000) + "-" + this.inserted + " = "
				+ (System.currentTimeMillis() / 1000 - this.inserted));
		return System.currentTimeMillis() / 1000 - this.inserted < cacheDuration;

	}

	public String getFullPath(SQLiteDatabase db) {
		String path = this.name;
		try {
			Folder f = Folder.getByFolderKey(db, this.parent);
			while (f.parent != null) {
				path = f.name + "/" + path;
				f = Folder.getByFolderKey(db, f.parent);
			}
			path = f.name + "/" + path;
		} catch (Exception e) {
			return path;
		}
		return path;

	}

}
