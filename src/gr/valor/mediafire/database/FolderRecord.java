package gr.valor.mediafire.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
		if (isRoot || folderRecord.folderKey == FolderRecord.ROOT_KEY) {
			folderRecord.inserted = System.currentTimeMillis() / 1000;
		} else {
			folderRecord.inserted = 0;
		}
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
		itemType = FolderItemRecord.TYPE_FOLDER;
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
		if (!this.isNew(db, folderKey)) {
			Log.d(TAG, "Updating folder " + this.toString() + " " + inserted);
			String queryItems = "UPDATE " + Mediabase.TABLE_ITEMS + " SET " + Columns.Items.KEY + "= ? ," + Columns.Items.TYPE + " = ? ,"
					+ Columns.Items.PARENT + " = ? , " + Columns.Items.NAME + " = ? ," + Columns.Items.DESC + " =? ," + Columns.Items.TAGS
					+ "=? , " + Columns.Items.FLAG + "=? ," + Columns.Items.PRIVACY + "=?," + Columns.Items.CREATED + "=?,  "
					+ Columns.Items.INSERTED + " = ?  WHERE " + Columns.Items.KEY + " = ? ";
			Object[] paramItems = new Object[] { folderKey, FolderItemRecord.TYPE_FOLDER, parent, name, desc, tags, flag, privacy, created,
					inserted, folderKey };
			db.execSQL(queryItems, paramItems);
			String queryFolder = "UPDATE " + Mediabase.TABLE_FOLDERS + " SET " + Columns.Folders.FOLDERKEY + " = ? ,"
					+ Columns.Folders.FOLDERS + "=? ," + Columns.Folders.SHARED + "=? ," + Columns.Folders.REVISION + "=?,"
					+ Columns.Folders.EPOCH + "=? ," + Columns.Folders.DROPBOX_ENABLED + "=?," + Columns.Folders.FILES + " = ? "
					+ " WHERE " + Columns.Folders.FOLDERKEY + "= ?";
			Object[] paramFolders = new Object[] { folderKey, folderCount, shared, revision, epoch, dropboxEnabled, fileCount, folderKey };
			db.execSQL(queryFolder, paramFolders);
		} else {
			Log.d(TAG, "Importing folder " + this.toString() + " " + inserted);
			db.execSQL("INSERT INTO " + Mediabase.TABLE_ITEMS + "(" + Columns.Items.KEY + "," + Columns.Items.TYPE + ","
					+ Columns.Items.PARENT + "," + Columns.Items.NAME + "," + Columns.Items.DESC + "," + Columns.Items.TAGS + ","
					+ Columns.Items.FLAG + "," + Columns.Items.PRIVACY + "," + Columns.Items.CREATED + ", " + Columns.Items.INSERTED + ")"
					+ " VALUES (?,?,?,?,?,?,?, ?, ? , ?)", new Object[] { folderKey, FolderItemRecord.TYPE_FOLDER, parent, name, desc,
					tags, flag, privacy, created, inserted });

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
			FolderRecord f = new FolderRecord(db, this.parent);
			while (f.parent != null) {
				path = f.name + "/" + path;
				f = new FolderRecord(db, f.parent);
			}
			path = f.name + "/" + path;
		} catch (Exception e) {
			return path;
		}
		return path;

	}

	public static FolderRecord createBackFolder() {
		FolderRecord bf = new FolderRecord();
		bf.name = "...";
		bf.itemType = TYPE_BACK;
		bf.privacy = "";
		return bf;
	}

	public List<Map<String, String>> getFolderItems() {
		if (!folderItems.isEmpty()) {
			folderItems.clear();
		}
		if (this.parent != null) {
			Log.d(TAG, "Adding back header");
			subFolders.add(0, FolderRecord.createBackFolder());

		}
		for (Iterator<FolderRecord> it = subFolders.iterator(); it.hasNext();) {
			FolderRecord folder = it.next();
			Map<String, String> map = new HashMap<String, String>();
			map.put(TYPE, folder.itemType);
			map.put(FOLDERKEY, folder.folderKey);
			map.put(NAME, folder.name);
			map.put(CREATED, folder.created);
			map.put(PRIVACY, folder.privacy);
			map.put(DOWNLOAD_ICON, NO);
			map.put(SIZE_ICON, NO);
			folderItems.add(map);
		}

		for (Iterator<FileRecord> it = files.iterator(); it.hasNext();) {
			FileRecord file = it.next();
			Map<String, String> map = new HashMap<String, String>();
			map.put(TYPE, file.getFileExtension());
			map.put(NAME, file.filename);
			map.put(QUICKKEY, file.quickkey);
			map.put(CREATED, file.created);
			map.put(PRIVACY, file.privacy);
			map.put(DOWNLOADS, String.valueOf(file.downloads));
			map.put(DOWNLOAD_ICON, YES);
			map.put(SIZE, file.getSize());
			map.put(SIZE_ICON, YES);
			folderItems.add(map);
		}

		return folderItems;
	}

	@Override
	public String toString() {
		return this.name + " (" + this.folderKey + ")";
	}

}
