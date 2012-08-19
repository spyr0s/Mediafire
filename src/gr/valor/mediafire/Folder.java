package gr.valor.mediafire;

import gr.valor.mediafire.database.Columns;
import gr.valor.mediafire.database.Mediabase;
import gr.valor.mediafire.helpers.Helper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Folder extends FolderItem {
	private static final String TAG = "Folder";
	public String folderKey;
	public String name;
	public boolean isFolder = true;
	public int folderCount = 0;
	public int fileCount = 0;
	public String shared;
	public int revision;
	public long epoch;
	public String dropboxEnabled;
	public String itemType = TYPE_FOLDER;
	private boolean fullImport = false;

	public Folder(Cursor cur) {
		createFolderFromCursor(cur);
	}

	public Folder() {
		super();
	}

	private void createFolderFromCursor(Cursor cur) {
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

	public void updateDb(SQLiteDatabase db, boolean fullImport) {
		Log.i(TAG, "Updating db");
		if (fullImport) {
			this.fullImport = true;
			Mediabase.truncateTables(db);
		}
		insertFolderInDb(db, this, true);

	}

	public Folder createBackFolder() {
		Folder bf = new Folder();
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
			subFolders.add(0, createBackFolder());

		}
		for (Iterator<Folder> it = subFolders.iterator(); it.hasNext();) {
			Folder folder = it.next();
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

		for (Iterator<File> it = files.iterator(); it.hasNext();) {
			File file = it.next();
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

	private void insertFolderInDb(SQLiteDatabase db, Folder folder, boolean isRoot) {
		db.execSQL("DELETE FROM " + Mediabase.TABLE_FOLDERS + " WHERE  " + Columns.Folders.FOLDERKEY + " IN( SELECT  " + Columns.Items.KEY
				+ " FROM " + Mediabase.TABLE_ITEMS + " WHERE " + Columns.Items.KEY + " = '" + folder.folderKey + "' OR "
				+ Columns.Items.PARENT + " = '" + folder.folderKey + "')");
		db.execSQL("DELETE FROM " + Mediabase.TABLE_FILES + " WHERE  " + Columns.Files.QUICKKEY + " IN( SELECT  " + Columns.Items.KEY
				+ " FROM " + Mediabase.TABLE_ITEMS + " WHERE " + Columns.Items.KEY + " = '" + folder.folderKey + "' OR "
				+ Columns.Items.PARENT + " = '" + folder.folderKey + "')");
		db.execSQL("DELETE FROM " + Mediabase.TABLE_ITEMS + " WHERE " + Columns.Items.KEY + " = '" + folder.folderKey + "' OR "
				+ Columns.Items.PARENT + " = '" + folder.folderKey + "'");

		long now = (isRoot || folder.folderKey == Folder.ROOT_KEY) ? System.currentTimeMillis() / 1000 : 0;

		db.execSQL("INSERT OR IGNORE INTO " + Mediabase.TABLE_ITEMS + "(" + Columns.Items.KEY + "," + Columns.Items.TYPE + ","
				+ Columns.Items.PARENT + "," + Columns.Items.NAME + "," + Columns.Items.DESC + "," + Columns.Items.TAGS + ","
				+ Columns.Items.FLAG + "," + Columns.Items.PRIVACY + "," + Columns.Items.CREATED + ", " + Columns.Items.INSERTED + ")"
				+ " VALUES (?,?,?,?,?,?,?, ?, ? , ?)", new Object[] { folder.folderKey, FolderItem.TYPE_FOLDER, folder.parent, folder.name,
				folder.desc, folder.tags, folder.flag, folder.privacy, folder.created, now });

		db.execSQL("INSERT OR IGNORE INTO " + Mediabase.TABLE_FOLDERS + "(" + Columns.Folders.FOLDERKEY + "," + Columns.Folders.FOLDERS
				+ "," + Columns.Folders.SHARED + "," + Columns.Folders.REVISION + "," + Columns.Folders.EPOCH + ","
				+ Columns.Folders.DROPBOX_ENABLED + "," + Columns.Folders.FILES + ")" + " VALUES (?,?,?,?,?,?,?)",
				new Object[] { folder.folderKey, folder.folderCount, folder.shared, folder.revision, folder.epoch, folder.dropboxEnabled,
						folder.fileCount });

		for (Iterator<Folder> it = folder.subFolders.iterator(); it.hasNext();) {
			Folder f = it.next();
			insertFolderInDb(db, f, fullImport);
		}
		for (Iterator<File> it = folder.files.iterator(); it.hasNext();) {
			File f = it.next();
			insertFileInDb(db, f);
		}

	}

	public String descr() {
		return "[folderkey:" + this.folderKey + ", parent:" + this.parent + ", name: " + this.name + ", desc:" + this.desc + ", tags:"
				+ this.tags + ", created:" + this.created + "]";
	}

	private void insertFileInDb(SQLiteDatabase db, File file) {
		db.execSQL("INSERT OR IGNORE INTO " + Mediabase.TABLE_ITEMS + "(" + Columns.Items.KEY + "," + Columns.Items.TYPE + ","
				+ Columns.Items.PARENT + "," + Columns.Items.NAME + "," + Columns.Items.DESC + "," + Columns.Items.TAGS + ","
				+ Columns.Items.FLAG + "," + Columns.Items.PRIVACY + "," + Columns.Items.CREATED + " )" + " VALUES (?,?,?,?,?,?,?,?,?)",
				new Object[] { file.quickkey, FolderItem.TYPE_FILE, file.parent, file.filename, file.desc, file.tags, file.flag,
						file.privacy, file.created });

		db.execSQL(
				"INSERT OR IGNORE INTO " + Mediabase.TABLE_FILES + "(" + Columns.Files.QUICKKEY + "," + Columns.Files.DOWNLOADS + ","
						+ Columns.Files.FILETYPE + "," + Columns.Files.PASSWORD_PROTECTED + "," + Columns.Files.SIZE + ")"
						+ " VALUES (?,?,?, ?,?)", new Object[] { file.quickkey, file.downloads, file.fileType, file.passwordProtected,
						file.size });

	}

	private String getNumOfItems(Folder folder) {
		if (folder.itemType.equals(TYPE_BACK)) {
			return "";
		}
		int folders = folder.folderCount > 0 ? folder.folderCount : folder.subFolders.size();
		int files = folder.fileCount > 0 ? folder.fileCount : folder.files.size();
		String[] r = new String[2];
		if (files == 0 && folders == 0) {
			return "(Empty)";
		} else {
			r[0] = folders > 0 ? "Folders:" + folders : "";
			r[1] = files > 0 ? "Files:" + files : "";
			return "(" + Helper.implode(r, " ") + ")";
		}

	}

	@Override
	public String toString() {

		return name + "(" + folderKey + ")";
	}

	public static Folder getByFolderKey(SQLiteDatabase db, String fk) throws Exception {
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
		Folder currentFolder = new Folder(cur);
		cur.close();
		return currentFolder;
	}

	public static Folder createRootFolder() {
		Folder f = new Folder();
		f.name = ROOT_NAME;
		f.folderKey = ROOT_KEY;
		return f;
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
