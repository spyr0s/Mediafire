package gr.valor.mediafire.database;

import java.util.Map;

import android.database.Cursor;

public class FileRecord extends FolderItemRecord {

	public String quickkey;
	public int downloads;
	public String filename;
	public String fileExtension;
	public String fileType;
	public String passwordProtected;
	public long size;

	private static final String[] Q = new String[] { "", "K", "M", "G", "T", "P", "E" };

	public FileRecord() {
		id = 0;
	}

	public FileRecord(Cursor cur) {
		createFromCursor(cur);
	}

	public FileRecord(String qk) throws Exception {
		String sql = "SELECT " + Columns.Files.QUICKKEY + ", " + Columns.Items.NAME + ", " + Columns.Items.TYPE + ", "
				+ Columns.Items.PARENT + ", " + Columns.Items.CREATED + ", " + Columns.Files.DOWNLOADS + ", " + Columns.Files.SIZE + ", "
				+ Columns.Items.INSERTED + ", " + Columns.Items.FLAG + ", " + Columns.Items.PRIVACY + ", " + Columns.Files.FILETYPE + ", "
				+ Columns.Files.PASSWORD_PROTECTED + " FROM " + Mediabase.TABLE_ITEMS + " i " + " LEFT JOIN " + Mediabase.TABLE_FILES
				+ " fi " + " ON i." + Columns.Items.KEY + " = fi." + Columns.Files.QUICKKEY + " WHERE " + Columns.Files.QUICKKEY + " = ? ";
		Cursor cur = getDb().rawQuery(sql, new String[] { qk });
		if (cur.getCount() != 1) {
			cur.close();
			throw new Exception(qk + " file not found in database");

		}
		cur.moveToFirst();
		createFromCursor(cur);
		cur.close();
	}

	@Override
	protected void createFromCursor(Cursor cur) {
		quickkey = cur.getString(cur.getColumnIndex(Columns.Files.QUICKKEY));
		itemType = FolderItemRecord.TYPE_FILE;
		filename = cur.getString(cur.getColumnIndex(Columns.Items.NAME));
		parent = cur.getString(cur.getColumnIndex(Columns.Items.PARENT));
		setCreated(cur.getString(cur.getColumnIndex(Columns.Items.CREATED)));
		inserted = cur.getLong(cur.getColumnIndex(Columns.Items.CREATED));
		downloads = cur.getInt(cur.getColumnIndex(Columns.Files.DOWNLOADS));
		size = cur.getLong(cur.getColumnIndex(Columns.Files.SIZE));
		fileType = cur.getString(cur.getColumnIndex(Columns.Files.FILETYPE));
		privacy = cur.getString(cur.getColumnIndex(Columns.Items.PRIVACY));
		passwordProtected = cur.getString(cur.getColumnIndex(Columns.Files.PASSWORD_PROTECTED));
		fileExtension = getFileExtension();
	}

	public boolean save() {

		if (!this.isNew(quickkey)) {
			String queryItems = "UPDATE " + Mediabase.TABLE_ITEMS + " SET " + Columns.Items.KEY + "= ? ," + Columns.Items.TYPE + " = ? ,"
					+ Columns.Items.PARENT + " = ? , " + Columns.Items.NAME + " = ? ," + Columns.Items.DESC + " =? ," + Columns.Items.TAGS
					+ "=? , " + Columns.Items.FLAG + "=? ," + Columns.Items.PRIVACY + "=?," + Columns.Items.CREATED + "=? " + " WHERE "
					+ Columns.Items.KEY + " = ? ";
			Object[] paramItems = new Object[] { quickkey, FolderItemRecord.TYPE_FILE, parent, filename, desc, tags, flag, privacy,
					getCreated(), quickkey };
			getDb().execSQL(queryItems, paramItems);
			String queryFolder = "UPDATE " + Mediabase.TABLE_FILES + " SET " + Columns.Files.QUICKKEY + "=?," + Columns.Files.DOWNLOADS
					+ "=?," + Columns.Files.FILETYPE + "=?," + Columns.Files.PASSWORD_PROTECTED + "=?," + Columns.Files.SIZE + "=? WHERE "
					+ Columns.Files.QUICKKEY + "= ?";
			Object[] paramFolders = new Object[] { quickkey, downloads, fileType, passwordProtected, size, quickkey };
			getDb().execSQL(queryFolder, paramFolders);
		} else {
			getDb().execSQL(
					"INSERT OR IGNORE INTO " + Mediabase.TABLE_ITEMS + "(" + Columns.Items.KEY + "," + Columns.Items.TYPE + ","
							+ Columns.Items.PARENT + "," + Columns.Items.NAME + "," + Columns.Items.DESC + "," + Columns.Items.TAGS + ","
							+ Columns.Items.FLAG + "," + Columns.Items.PRIVACY + "," + Columns.Items.CREATED + " )"
							+ " VALUES (?,?,?,?,?,?,?,?,?)",
					new Object[] { quickkey, FolderItemRecord.TYPE_FILE, parent, filename, desc, tags, flag, privacy, getCreated() });

			getDb().execSQL(
					"INSERT OR IGNORE INTO " + Mediabase.TABLE_FILES + "(" + Columns.Files.QUICKKEY + "," + Columns.Files.DOWNLOADS + ","
							+ Columns.Files.FILETYPE + "," + Columns.Files.PASSWORD_PROTECTED + "," + Columns.Files.SIZE + ")"
							+ " VALUES (?,?,?, ?,?)", new Object[] { quickkey, downloads, fileType, passwordProtected, size });
		}
		return true;
	}

	public String getFileExtension() {
		int pos = filename.lastIndexOf(".");
		if (pos > 0 && filename.length() > pos) {
			return filename.substring(pos + 1).toLowerCase();
		} else {
			return FolderItemRecord.TYPE_FILE;
		}

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

	public Map<String, String> updateAdapterItem(Map<String, String> map) {
		map.put(ICON, getFileExtension());
		map.put(TYPE, TYPE_FILE);
		map.put(NAME, filename);
		map.put(QUICKKEY, quickkey);
		map.put(CREATED, getCreated());
		map.put(PRIVACY, privacy);
		map.put(DOWNLOADS, String.valueOf(downloads));
		map.put(DOWNLOAD_ICON, YES);
		map.put(SIZE, getSize());
		map.put(SIZE_ICON, YES);
		return map;
	}
}
