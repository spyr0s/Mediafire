package gr.valor.mediafire;

import gr.valor.mediafire.database.Columns;
import gr.valor.mediafire.database.Mediabase;

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
	public int subFoldersCount = 0;
	public int filesCount = 0;

	public Folder(Cursor cur) {
		createFolderFromCursor(cur);
	}

	public Folder() {
		super();
	}

	private void createFolderFromCursor(Cursor cur) {
		Log.d(TAG, "Creating folder from cursor");
		folderKey = cur.getString(cur.getColumnIndex(Columns.Folders.FOLDERKEY));
		name = cur.getString(cur.getColumnIndex(Columns.Items.NAME));
		parent = cur.getString(cur.getColumnIndex(Columns.Items.PARENT));
		created = cur.getString(cur.getColumnIndex(Columns.Items.CREATED));
		inserted = cur.getLong(cur.getColumnIndex(Columns.Items.INSERTED));
		subFoldersCount = cur.getInt(cur.getColumnIndex(Columns.Folders.FOLDERS));
		filesCount = cur.getInt(cur.getColumnIndex(Columns.Folders.FILES));
	}

	public void updateDb(SQLiteDatabase db, boolean fullImport) {
		Log.d(TAG, "Updating db");
		if (fullImport) {
			Mediabase.truncateTables(db);
		}
		insertFolderInDb(db, this);

	}

	public List<Map<String, String>> getFolderItems() {
		if (!folderItems.isEmpty()) {
			folderItems.clear();
		}

		for (Iterator<Folder> it = subFolders.iterator(); it.hasNext();) {
			Folder folder = it.next();
			Map<String, String> map = new HashMap<String, String>();
			map.put(TYPE, TYPE_FOLDER);
			map.put(FOLDERKEY, folder.folderKey);
			map.put(NAME, folder.name);
			map.put(CREATED, folder.created);
			map.put(SIZE, getNumOfItems(folder));
			folderItems.add(map);
		}

		for (Iterator<File> it = files.iterator(); it.hasNext();) {
			File file = it.next();
			Map<String, String> map = new HashMap<String, String>();
			map.put(TYPE, file.getFileType());
			map.put(NAME, file.filename);
			map.put(QUICKKEY, file.quickkey);
			map.put(CREATED, file.created);
			map.put(DOWNLOADS, "downloads:" + file.downloads + ", size: " + file.getSize());
			map.put(SIZE, "");
			folderItems.add(map);
		}

		return folderItems;
	}

	private void insertFolderInDb(SQLiteDatabase db, Folder folder) {
		Log.d(TAG, "Inserting folder " + folder.descr() + " in database");
		Log.d(TAG, "Inserting folder " + folder + " in items");
		db.execSQL("INSERT OR IGNORE INTO " + Mediabase.TABLE_ITEMS + "(" + Columns.Items.KEY + "," + Columns.Items.TYPE + ","
				+ Columns.Items.PARENT + "," + Columns.Items.NAME + "," + Columns.Items.DESC + "," + Columns.Items.TAGS + ","
				+ Columns.Items.CREATED + ", " + Columns.Items.INSERTED + ")" + " VALUES (?,?,?,?,?,?,?,?)", new Object[] {
				folder.folderKey, FolderItem.TYPE_FOLDER, folder.parent, folder.name, folder.desc, folder.tags, folder.created,
				folder.inserted });

		Log.d(TAG, "Inserting folder " + folder + " in folders");
		db.execSQL("INSERT OR IGNORE INTO " + Mediabase.TABLE_FOLDERS + "(" + Columns.Folders.FOLDERKEY + "," + Columns.Folders.FOLDERS
				+ "," + Columns.Folders.FILES + ")" + " VALUES (?,?,?)", new Object[] { folder.folderKey, folder.subFolders.size(),
				folder.filesCount });

		for (Iterator<Folder> it = folder.subFolders.iterator(); it.hasNext();) {
			Folder f = it.next();
			insertFolderInDb(db, f);
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
		Log.d(TAG, "Inserting file " + file + " in database");
		Log.d(TAG, "Inserting file " + file + " in items");
		db.execSQL("INSERT OR IGNORE INTO " + Mediabase.TABLE_ITEMS + "(" + Columns.Items.KEY + "," + Columns.Items.TYPE + ","
				+ Columns.Items.PARENT + "," + Columns.Items.NAME + "," + Columns.Items.DESC + "," + Columns.Items.TAGS + ","
				+ Columns.Items.CREATED + ", " + Columns.Items.INSERTED + " )" + " VALUES (?,?,?,?,?,?,?,?)", new Object[] { file.quickkey,
				FolderItem.TYPE_FILE, file.parent, file.filename, file.desc, file.tags, file.created, System.currentTimeMillis() / 1000 });

		Log.d(TAG, "Inserting file " + file + " in files");
		db.execSQL("INSERT OR IGNORE INTO " + Mediabase.TABLE_FILES + "(" + Columns.Files.QUICKKEY + "," + Columns.Files.DOWNLOADS + ","
				+ Columns.Files.SIZE + ")" + " VALUES (?,?,?)", new Object[] { file.quickkey, file.downloads, file.size });

	}

	private String getNumOfItems(Folder folder) {
		int folders = folder.subFoldersCount > 0 ? folder.subFoldersCount : folder.subFolders.size();
		int files = folder.filesCount > 0 ? folder.filesCount : folder.files.size();
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
		Log.d(TAG, "Getting " + fk + " folder from db");
		String sql = "SELECT " + Columns.Folders.FOLDERKEY + ", " + Columns.Items.NAME + ", " + Columns.Items.TYPE + ", "
				+ Columns.Items.PARENT + ", " + Columns.Items.CREATED + ", " + Columns.Folders.FOLDERS + ", " + Columns.Folders.FILES
				+ " , " + Columns.Items.INSERTED + " FROM " + Mediabase.TABLE_ITEMS + " i " + " LEFT JOIN " + Mediabase.TABLE_FOLDERS
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

	public boolean isCached() {
		Log.d(TAG, "now:" + System.currentTimeMillis() / 1000);
		Log.d(TAG, "ins: " + this.inserted);
		return System.currentTimeMillis() / 1000 - this.inserted < 7200;

	}
}
