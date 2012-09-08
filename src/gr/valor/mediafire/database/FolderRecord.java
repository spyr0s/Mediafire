package gr.valor.mediafire.database;

import gr.valor.mediafire.Mediafire;
import gr.valor.mediafire.helpers.MyLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.database.Cursor;

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
		folderKey = getRootKey();
	}

	public FolderRecord(Cursor cur) {
		createFromCursor(cur);
	}

	public FolderRecord(String fk) throws Exception {
		String sql = "SELECT " + Columns.Folders.FOLDERKEY + ", " + Columns.Items.ACCOUNT_EMAIL + ", " + Columns.Items.NAME + ", "
				+ Columns.Items.TYPE + ", " + Columns.Items.PARENT + ", " + Columns.Items.CREATED + ", " + Columns.Folders.FOLDERS + ", "
				+ Columns.Folders.FILES + " , " + Columns.Items.INSERTED + ", " + Columns.Items.FLAG + " , " + Columns.Items.PRIVACY + ", "
				+ Columns.Folders.SHARED + ", " + Columns.Folders.REVISION + ", " + Columns.Folders.EPOCH + " , "
				+ Columns.Folders.DROPBOX_ENABLED + " FROM " + Mediabase.TABLE_ITEMS + " i " + " LEFT JOIN " + Mediabase.TABLE_FOLDERS
				+ " fo" + " ON i." + Columns.Items.KEY + " = fo." + Columns.Folders.FOLDERKEY + " WHERE " + Columns.Items.KEY + " = ?;";
		Cursor cur = getDb().rawQuery(sql, new String[] { fk });
		if (cur.getCount() != 1) {
			cur.close();
			throw new Exception(fk + " folder not found in database");

		}
		cur.moveToFirst();
		createFromCursor(cur);
		cur.close();

	}

	public void updateDb(boolean fullImport) {
		MyLog.i(TAG, "Updating db");
		if (fullImport) {
			this.fullImport = true;
			Mediabase.truncateTables(getDb());
		}
		insertFolderInDb(this, true);

	}

	private void insertFolderInDb(FolderRecord folderRecord, boolean isRoot) {
		if (isRoot || folderRecord.folderKey == FolderRecord.getRootKey()) {
			folderRecord.inserted = System.currentTimeMillis() / 1000;
			// Check for deleted items
			removeDeletedItems(folderRecord);
		} else {
			folderRecord.inserted = 0;
		}
		folderRecord.save();

		for (Iterator<FolderRecord> it = folderRecord.subFolders.iterator(); it.hasNext();) {
			FolderRecord f = it.next();
			insertFolderInDb(f, fullImport);
		}
		for (Iterator<FileRecord> it = folderRecord.files.iterator(); it.hasNext();) {
			FileRecord f = it.next();
			f.save();
		}

	}

	private void removeDeletedItems(FolderRecord folderRecord) {
		Cursor c = getDb().query(Mediabase.TABLE_ITEMS, new String[] { Columns.Items.KEY, Columns.Items.TYPE },
				Columns.Items.PARENT + " = ? AND " + Columns.Items.ACCOUNT_EMAIL + " = ?",
				new String[] { folderRecord.folderKey, folderRecord.account_email }, null, null, null, null);
		while (c.moveToNext()) {
			String key = c.getString(c.getColumnIndex(Columns.Items.KEY));
			String type = c.getString(c.getColumnIndex(Columns.Items.TYPE));
			boolean fileExists = false;
			// Search in folders
			if (type.equals(FolderItemRecord.TYPE_FOLDER)) {
				for (Iterator<FolderRecord> it = folderRecord.subFolders.iterator(); it.hasNext();) {
					FolderRecord f = it.next();
					if (key.equals(f.folderKey)) {
						fileExists = true;
						break;
					}
				}
			}
			// Search in files
			if (type.equals(FolderItemRecord.TYPE_FILE)) {
				for (Iterator<FileRecord> it = folderRecord.files.iterator(); it.hasNext();) {
					FileRecord f = it.next();
					if (key.equals(f.quickkey)) {
						fileExists = true;
						break;
					}
				}
			}
			if (!fileExists) {
				getDb().delete(Mediabase.TABLE_ITEMS, Columns.Items.KEY + " = ? AND " + Columns.Items.ACCOUNT_EMAIL + " = ?",
						new String[] { key, account_email });
				if (type.equals(FolderItemRecord.TYPE_FOLDER)) {
					getDb().delete(Mediabase.TABLE_FOLDERS, Columns.Folders.FOLDERKEY + " = ? ", new String[] { key });
				} else if (type.equals(FolderItemRecord.TYPE_FILE)) {
					getDb().delete(Mediabase.TABLE_FILES, Columns.Files.QUICKKEY + " = ? ", new String[] { key });
				}
			}
		}
		c.close();
	}

	public static FolderRecord createNewFromCursor(Cursor cur) {
		return new FolderRecord(cur);
	}

	@Override
	protected void createFromCursor(Cursor cur) {
		folderKey = cur.getString(cur.getColumnIndex(Columns.Folders.FOLDERKEY));
		account_email = Mediafire.getAccountEmail();
		itemType = FolderItemRecord.TYPE_FOLDER;
		name = cur.getString(cur.getColumnIndex(Columns.Items.NAME));
		parent = cur.getString(cur.getColumnIndex(Columns.Items.PARENT));
		setCreated(cur.getString(cur.getColumnIndex(Columns.Items.CREATED)));
		inserted = cur.getLong(cur.getColumnIndex(Columns.Items.INSERTED));
		privacy = cur.getString(cur.getColumnIndex(Columns.Items.PRIVACY));
		folderCount = cur.getInt(cur.getColumnIndex(Columns.Folders.FOLDERS));
		fileCount = cur.getInt(cur.getColumnIndex(Columns.Folders.FILES));
		revision = cur.getInt(cur.getColumnIndex(Columns.Folders.REVISION));
		epoch = cur.getLong(cur.getColumnIndex(Columns.Folders.EPOCH));
		dropboxEnabled = cur.getString(cur.getColumnIndex(Columns.Folders.DROPBOX_ENABLED));
		shared = cur.getString(cur.getColumnIndex(Columns.Folders.SHARED));
	}

	public boolean save() {
		if (!this.isNew(folderKey)) {
			MyLog.d(TAG, "Updating folder " + this.toString() + " " + inserted);
			String queryItems = "UPDATE " + Mediabase.TABLE_ITEMS + " SET " + Columns.Items.KEY + "= ? ," + Columns.Items.ACCOUNT_EMAIL
					+ "= ? ," + Columns.Items.TYPE + " = ? ," + Columns.Items.PARENT + " = ? , " + Columns.Items.NAME + " = ? ,"
					+ Columns.Items.DESC + " =? ," + Columns.Items.TAGS + "=? , " + Columns.Items.FLAG + "=? ," + Columns.Items.PRIVACY
					+ "=?," + Columns.Items.CREATED + "=?,  " + Columns.Items.INSERTED + " = ?  WHERE " + Columns.Items.KEY + " = ? ";
			Object[] paramItems = new Object[] { folderKey, account_email, FolderItemRecord.TYPE_FOLDER, parent, name, desc, tags, flag,
					privacy, getCreated(), inserted, folderKey };
			getDb().execSQL(queryItems, paramItems);
			String queryFolder = "UPDATE " + Mediabase.TABLE_FOLDERS + " SET " + Columns.Folders.FOLDERKEY + " = ? ,"
					+ Columns.Folders.FOLDERS + "=? ," + Columns.Folders.SHARED + "=? ," + Columns.Folders.REVISION + "=?,"
					+ Columns.Folders.EPOCH + "=? ," + Columns.Folders.DROPBOX_ENABLED + "=?," + Columns.Folders.FILES + " = ? "
					+ " WHERE " + Columns.Folders.FOLDERKEY + "= ?";
			Object[] paramFolders = new Object[] { folderKey, folderCount, shared, revision, epoch, dropboxEnabled, fileCount, folderKey };
			getDb().execSQL(queryFolder, paramFolders);
		} else {
			MyLog.d(TAG, "Importing folder " + this.toString() + " " + inserted);
			try {
				getDb().execSQL(
						"INSERT INTO " + Mediabase.TABLE_ITEMS + "(" + Columns.Items.KEY + "," + Columns.Items.ACCOUNT_EMAIL + ","
								+ Columns.Items.TYPE + "," + Columns.Items.PARENT + "," + Columns.Items.NAME + "," + Columns.Items.DESC
								+ "," + Columns.Items.TAGS + "," + Columns.Items.FLAG + "," + Columns.Items.PRIVACY + ","
								+ Columns.Items.CREATED + ", " + Columns.Items.INSERTED + ")" + " VALUES (?,?,?,?,?,?,?,?, ?, ? , ?)",
						new Object[] { folderKey, account_email, FolderItemRecord.TYPE_FOLDER, parent, name, desc, tags, flag, privacy,
								getCreated(), inserted });

				getDb().execSQL(
						"INSERT INTO " + Mediabase.TABLE_FOLDERS + "(" + Columns.Folders.FOLDERKEY + "," + Columns.Folders.FOLDERS + ","
								+ Columns.Folders.SHARED + "," + Columns.Folders.REVISION + "," + Columns.Folders.EPOCH + ","
								+ Columns.Folders.DROPBOX_ENABLED + "," + Columns.Folders.FILES + ")" + " VALUES (?,?,?,?,?,?,?)",
						new Object[] { folderKey, folderCount, shared, revision, epoch, dropboxEnabled, fileCount });
			} catch (Exception e) {
				e.printStackTrace();

			}
		}

		return true;
	}

	public String descr() {
		return "[folderkey:" + this.folderKey + ", parent:" + this.parent + ", name: " + this.name + ", desc:" + this.desc + ", tags:"
				+ this.tags + ", created:" + this.getCreated() + "]";
	}

	public boolean isCached(long cacheDuration) {
		MyLog.d(TAG, "Checking for cached folder in cache dur:" + cacheDuration);
		MyLog.d(TAG, "now:" + (System.currentTimeMillis() / 1000) + "-" + this.inserted + " = "
				+ (System.currentTimeMillis() / 1000 - this.inserted));
		return System.currentTimeMillis() / 1000 - this.inserted < cacheDuration;

	}

	public String getFullPath() {
		String path = this.name;
		try {
			FolderRecord f = new FolderRecord(this.parent);
			while (f.parent != null) {
				path = f.name + "/" + path;
				f = new FolderRecord(f.parent);
			}
			path = f.name + "/" + path;
		} catch (Exception e) {
			return path;
		}
		return path;

	}

	public static FolderRecord createNoItemsFolder() {
		FolderRecord ef = new FolderRecord();
		ef.name = "No Items yet!!!";
		ef.itemType = TYPE_EMPTY;
		ef.privacy = PRIVACY_INVISIBLE;
		return ef;
	}

	public static FolderRecord createBackFolder() {
		FolderRecord bf = new FolderRecord();
		bf.name = "...";
		bf.itemType = TYPE_BACK;
		bf.privacy = PRIVACY_INVISIBLE;
		return bf;
	}

	public List<Map<String, String>> getFolderItems() {
		if (!folderItems.isEmpty()) {
			folderItems.clear();
		}
		if (this.parent != null) {
			MyLog.d(TAG, "Adding back header");
			subFolders.add(0, FolderRecord.createBackFolder());
		}
		if (subFolders.size() == 1 && files.isEmpty()) {
			MyLog.d(TAG, "Adding empty folder");
			subFolders.add(FolderRecord.createNoItemsFolder());
		}
		for (Iterator<FolderRecord> it = subFolders.iterator(); it.hasNext();) {
			FolderRecord folder = it.next();
			Map<String, String> map = new HashMap<String, String>();
			map.put(TYPE, folder.itemType);
			map.put(ICON, folder.itemType);
			map.put(FOLDERKEY, folder.folderKey);
			map.put(NAME, folder.name);
			map.put(CREATED, folder.getCreated());
			map.put(PRIVACY, folder.privacy);
			map.put(DOWNLOAD_ICON, NO);
			map.put(SIZE_ICON, NO);
			folderItems.add(map);
		}

		for (Iterator<FileRecord> it = files.iterator(); it.hasNext();) {
			FileRecord file = it.next();
			Map<String, String> map = new HashMap<String, String>();
			file.updateAdapterItem(map);
			folderItems.add(map);
		}

		return folderItems;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public void updateAdapterItem(Map<String, String> map) {
		map.put(TYPE, this.itemType);
		map.put(ICON, TYPE_FOLDER);
		map.put(FOLDERKEY, this.folderKey);
		map.put(NAME, this.name);
		map.put(CREATED, this.getCreated());
		map.put(PRIVACY, this.privacy);
		map.put(DOWNLOAD_ICON, NO);
		map.put(SIZE_ICON, NO);
	}

	public static ArrayList<FolderRecord> getAll() {
		ArrayList<FolderRecord> f = new ArrayList<FolderRecord>();
		String sql = "SELECT " + Columns.Folders.FOLDERKEY + ", " + Columns.Items.ACCOUNT_EMAIL + ", " + Columns.Items.NAME + ", "
				+ Columns.Items.TYPE + ", " + Columns.Items.PARENT + ", " + Columns.Items.CREATED + ", " + Columns.Folders.FOLDERS + ", "
				+ Columns.Folders.FILES + " , " + Columns.Items.INSERTED + ", " + Columns.Items.FLAG + " , " + Columns.Items.PRIVACY + ", "
				+ Columns.Folders.SHARED + ", " + Columns.Folders.REVISION + ", " + Columns.Folders.EPOCH + " , "
				+ Columns.Folders.DROPBOX_ENABLED + " FROM " + Mediabase.TABLE_ITEMS + " i " + " LEFT JOIN " + Mediabase.TABLE_FOLDERS
				+ " fo" + " ON i." + Columns.Items.KEY + " = fo." + Columns.Folders.FOLDERKEY + " WHERE " + Columns.Items.ACCOUNT_EMAIL
				+ " = ? AND " + Columns.Items.TYPE + " = ?;";
		Cursor cur = getDb().rawQuery(sql, new String[] { Mediafire.getAccountEmail(), FolderItemRecord.TYPE_FOLDER });
		while (cur.moveToNext()) {
			f.add(createNewFromCursor(cur));
		}
		cur.close();
		return f;
	}

}
