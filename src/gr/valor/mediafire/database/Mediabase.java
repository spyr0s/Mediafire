package gr.valor.mediafire.database;

import gr.valor.mediafire.helpers.MyLog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Mediabase extends SQLiteOpenHelper {
	public static final String TAG = "Mediabase";
	public static final String DATABASE = "mediafire.db";
	public static final int VERSION = 2;
	public static final String TABLE_ITEMS = "items";
	public static final String TABLE_FOLDERS = "folders";
	public static final String TABLE_FILES = "files";
	public static final String TABLE_NOTES = "notes";
	public static final String TABLE_REVISIONS = "revisions";

	public Mediabase(Context context) {
		super(context, DATABASE, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		MyLog.d(TAG, "Creating table " + TABLE_ITEMS);
		db.execSQL("CREATE TABLE `" + TABLE_ITEMS + "`" + "(`" + Columns.Items._ID
				+ "` INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE ," + "`" + Columns.Items.ACCOUNT_EMAIL
				+ "` VARCHAR NOT NULL , " + "`" + Columns.Items.KEY + "` VARCHAR NOT NULL  UNIQUE , " + "`" + Columns.Items.TYPE
				+ "` VARCHAR, " + "`" + Columns.Items.PARENT + "` VARCHAR, " + "`" + Columns.Items.NAME + "` VARCHAR, " + "`"
				+ Columns.Items.DESC + "` TEXT, " + "`" + Columns.Items.TAGS + "` TEXT, " + "`" + Columns.Items.FLAG + "` INTEGER, " + "`"
				+ Columns.Items.PRIVACY + "` TEXT, `" + Columns.Items.CREATED + "` DATETIME, " + "`" + Columns.Items.INSERTED
				+ "` INTEGER )");

		MyLog.d(TAG, "Creating table " + TABLE_FOLDERS);
		db.execSQL("CREATE TABLE `" + TABLE_FOLDERS + "` " + "(`" + Columns.Folders._ID
				+ "` INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , " + "`" + Columns.Folders.FOLDERKEY
				+ "` VARCHAR NOT NULL  UNIQUE , " + "`" + Columns.Folders.FOLDERS + "` INTEGER DEFAULT 0, " + "`" + Columns.Folders.SHARED
				+ "` TEXT, " + "`" + Columns.Folders.REVISION + "` INTEGER, " + "`" + Columns.Folders.EPOCH + "` INTEGER, " + "`"
				+ Columns.Folders.DROPBOX_ENABLED + "` TEXT, " + "`" + Columns.Folders.FILES + "` INTEGER DEFAULT 0)");

		MyLog.d(TAG, "Creating table " + TABLE_FILES);
		db.execSQL("CREATE TABLE `" + TABLE_FILES + "` " + "(`" + Columns.Files._ID
				+ "` INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , " + "`" + Columns.Files.QUICKKEY
				+ "` VARCHAR NOT NULL  UNIQUE , " + "`" + Columns.Files.FILETYPE + "` TEXT, " + "`" + Columns.Files.PASSWORD_PROTECTED
				+ "` TEXT, " + "`" + Columns.Files.DOWNLOADS + "` INTEGER DEFAULT 0, " + "`" + Columns.Files.SIZE + "` INTEGER DEFAULT 0)");

		MyLog.d(TAG, "Creating table " + TABLE_NOTES);
		db.execSQL("CREATE TABLE `" + TABLE_NOTES + "` " + "(`" + Columns.Notes._ID
				+ "` INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , " + "`" + Columns.Notes.QUICKKEY
				+ "` VARCHAR NOT NULL  UNIQUE , " + "`" + Columns.Notes.SUBJECT + "` VARCHAR, " + "`" + Columns.Notes.DESCRIPTION
				+ "` TEXT )");

		MyLog.d(TAG, "Creating table " + TABLE_REVISIONS);
		db.execSQL("CREATE TABLE `" + TABLE_REVISIONS + "` " + "(`" + Columns.Revisions._ID
				+ "` INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , " + "`" + Columns.Revisions.REVISION + "` INTEGER, " + "`"
				+ Columns.Revisions.EPOCH + "` INTEGER )");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		MyLog.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLDERS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVISIONS);
		onCreate(db);

	}

	public static void truncateTables(SQLiteDatabase db) {
		MyLog.d(TAG, "Truncating tables");
		db.execSQL("DELETE FROM " + TABLE_NOTES);
		db.execSQL("DELETE FROM " + TABLE_FILES);
		db.execSQL("DELETE FROM " + TABLE_FOLDERS);
		db.execSQL("DELETE FROM " + TABLE_ITEMS);

	}

	public boolean isEmpty() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM " + TABLE_ITEMS + " LIMIT 1", new String[] {});
		int count = c.getCount();
		c.close();
		db.close();
		return count == 0;
	}

	public int getLatestRevision(SQLiteDatabase db) {
		Cursor c = db.rawQuery("SELECT * FROM " + TABLE_REVISIONS + " ORDER BY " + Columns.Revisions.REVISION + " DESC  LIMIT 1",
				new String[] {});
		if (c.getCount() == 0) {
			return 0;
		}
		c.moveToFirst();
		int revision = c.getInt(c.getColumnIndex(Columns.Revisions.REVISION));
		c.close();
		return revision;
	}

}
