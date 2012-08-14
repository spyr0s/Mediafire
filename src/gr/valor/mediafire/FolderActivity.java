package gr.valor.mediafire;

import gr.valor.mediafire.api.ApiUrls;
import gr.valor.mediafire.api.Connection;
import gr.valor.mediafire.api.MyFilesJSONParser;
import gr.valor.mediafire.api.MyOfflineFiles;
import gr.valor.mediafire.database.Mediabase;
import gr.valor.mediafire.helpers.FileIcon;
import gr.valor.mediafire.helpers.SwipeInterface;
import gr.valor.mediafire.parser.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class FolderActivity extends BaseActivity implements SwipeInterface {
	private static final String TAG = "FolderActivity";
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	public static final String FOLDERKEY = "gr.valor.mediafire.FOLDERKEY";
	public static final String ONLINE = "gr.valor.mediafire.ONLINE";
	public static final String EMPTY_DB = "gr.valor.mediafire.EMPTY_DB";
	public static final String FULL_IMPORT = "gr.valor.mediafire.FULL_IMPORT";
	public Folder folder;
	public ArrayList<String> path = new ArrayList<String>();

	private static final String[] FOLDER_FROM = { Folder.TYPE, Folder.NAME, Folder.CREATED, File.DOWNLOADS, File.SIZE };
	private static final int[] FOLDER_TO = { R.id.folder_item_type, R.id.folder_item_name, R.id.folder_item_created,
			R.id.folder_item_downloads, R.id.folder_item_size };
	List<Map<String, String>> folderItems = new ArrayList<Map<String, String>>();
	SimpleAdapter folderAdapter;
	private int folderResource = R.layout.folder_item;

	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	private boolean fullImport;
	private boolean online;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_folder);
		requestFolder();
		// Gesture detection
		ActivitySwipeDetector activitySwipeDetector = new ActivitySwipeDetector(this);
		RelativeLayout lowestLayout = (RelativeLayout) this.findViewById(R.id.activity_folder_layout);
		lowestLayout.setOnTouchListener(activitySwipeDetector);

	}

	private void requestFolder() {
		Log.d(TAG, "Showing folder " + mediafire.getCurrentFolder());
		if (!mediafire.isOnline()) {
			if (!mediafire.isEmptyDb()) {
				createOfflineList();
			} else {
				showAlertDialog();
			}
		} else {
			if (mediafire.getCurrentFolder().isCached()) {
				createCachedList();
			} else {
				if (!mediafire.isTokenValid()) {
					if (mediafire.isFullImport()) {
						mediafire.setCurrentFolder(Folder.createRootFolder());
						createOfflineList();
					} else {
						createOfflineList();
					}
				} else {
					if (mediafire.isFullImport()) {
						createFullImportList();
					} else {
						createOnlineList();
					}
				}
			}
		}
	}

	private void createOfflineList() {
		Log.d(TAG, "Creating an offline list of folder " + mediafire.getCurrentFolder());
		getOfflineFolderItems();
		createList();
	}

	private void createCachedList() {
		Log.d(TAG, "Creating a cached list of folder " + mediafire.getCurrentFolder());
		getOfflineFolderItems();
		createList();
	}

	private void createOnlineList() {
		Log.d(TAG, "Creating an online list of folder " + mediafire.getCurrentFolder());
		getOnlineFolderItems();
	}

	private void createFullImportList() {
		Log.d(TAG, "Creating full import");
		mediafire.setFullImport(true);
		getOnlineFolderItems();
	}

	private void createList() {
		LinearLayout h = (LinearLayout) findViewById(R.id.folder_header);
		if (mediafire.getCurrentFolder().folderKey.equals(Folder.ROOT_KEY)) {
			h.setVisibility(View.GONE);
		} else {
			h.setVisibility(View.VISIBLE);
		}
		setTitle("Mediafire - " + mediafire.getCurrentFolder().name);
		path.add(mediafire.getCurrentFolder().name);
		((TextView) findViewById(R.id.listView_title)).setText(Helper.implode(path, "/"));

		createAdapter();
		populateList();
	}

	private void showAlertDialog() {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Import folders and files list from mediafire?");
		alertDialog.setMessage("This may take a while depending on the numbers of folders");
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				createFullImportList();
			}
		});
		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				createOfflineList();

			}
		});

		alertDialog.show();

	}

	@Override
	public void onBackPressed() {
		if (mediafire.getCurrentFolder().folderKey.equals(Folder.ROOT_KEY)) {
			super.onBackPressed();
			return;
		}
		Mediabase mb = new Mediabase(this);
		SQLiteDatabase db = mb.getReadableDatabase();
		try {
			path.remove(path.size() - 1);
			path.remove(path.size() - 1);
			mediafire.setCurrentFolder(Folder.getByFolderKey(db, mediafire.getCurrentFolder().parent));
			createOfflineList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			db.close();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	public void parentFolder(View view) {
		onBackPressed();
	}

	private void getOnlineFolderItems() {
		Log.d(TAG, "Connecting to get files");
		Connection connection = new Connection(this);
		MyOnlineFilesTask onlineFiles = new MyOnlineFilesTask(this, connection);
		onlineFiles.execute(new Folder[] { mediafire.getCurrentFolder() });
	}

	private void getOfflineFolderItems() {
		Log.d(TAG, "Getting files from db");
		Mediabase mb = new Mediabase(this);
		SQLiteDatabase db = mb.getReadableDatabase();

		MyOfflineFiles myFiles = new MyOfflineFiles(this, db);
		myFiles.setParent(mediafire.getCurrentFolder().folderKey);
		try {
			Folder curFolder = myFiles.getFiles(null);
			createFolderItems(curFolder);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean filesChanged() {
		Mediabase mb = new Mediabase(this);
		SQLiteDatabase db = mb.getReadableDatabase();
		int rev = mb.getLatestRevision(db);
		db.close();
		return false;
	}

	private void createFolderItems(Folder curFolder) {
		if (curFolder != null) {
			folderItems = curFolder.getFolderItems();
		}

	}

	private void createAdapter() {
		folderAdapter = new SimpleAdapter(this, folderItems, folderResource, FOLDER_FROM, FOLDER_TO);
		folderAdapter.setViewBinder(new FolderViewBinder());
	}

	private void populateList() {
		Log.d(TAG, "Populating the list");
		ListView listFolders = (ListView) findViewById(R.id.listView_items);
		listFolders.setAdapter(folderAdapter);
		listFolders.setOnItemClickListener(new ListItemListener());
	}

	public class ListItemListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			@SuppressWarnings("unchecked")
			Map<String, String> fi = (Map<String, String>) parent.getItemAtPosition(position);
			if (fi.get(FolderItem.TYPE).equals(FolderItem.TYPE_FOLDER)) {
				Mediabase mb = new Mediabase(FolderActivity.this);
				SQLiteDatabase db = mb.getReadableDatabase();
				try {
					Folder newFolder = Folder.getByFolderKey(db, fi.get(FolderItem.FOLDERKEY));
					mediafire.setCurrentFolder(newFolder);
					requestFolder();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					db.close();
				}
			} else if (fi.get(FolderItem.TYPE).equals(FolderItem.TYPE_FILE)) {
				String quickkey = fi.get(FolderItem.QUICKKEY);
				Log.d(TAG, "Clicked on " + quickkey);
			}
		}
	}

	public class FolderViewBinder implements SimpleAdapter.ViewBinder {

		public boolean setViewValue(View view, Object data, String textRepresentation) {
			if (view.getId() == R.id.folder_item_type) {
				ImageView im = (ImageView) view;
				Resources res = getResources();
				Drawable d = null;
				if (textRepresentation.equals(FolderItem.TYPE_FOLDER)) {
					d = res.getDrawable(R.drawable.icon_folder);
					im.setImageDrawable(d);
				} else if (textRepresentation.equals(FolderItem.TYPE_FILE)) {
					d = res.getDrawable(R.drawable.icon_file);
					im.setImageDrawable(d);
				} else {
					String img = textRepresentation;
					img = img.replaceAll(" ", "").toLowerCase();
					FileIcon icon = new FileIcon(img, FolderActivity.this);
					int r = icon.getIcon();
					Log.d(TAG, "CUSTOM ICON" + textRepresentation + " resource " + r);
					if (r == 0) {
						d = res.getDrawable(R.drawable.icon_file);
						im.setImageDrawable(d);
					} else {
						((ImageView) view).setImageResource(r);
					}
				}

				return true;
			}
			return false;
		}

	}

	public void right2left(View v) {

	}

	@Override
	public void bottom2top(View v) {

	}

	@Override
	public void left2right(View v) {
		onBackPressed();

	}

	@Override
	public void top2bottom(View v) {

	}

	class MyOnlineFilesTask extends AsyncTask<Folder, Void, Folder> implements ApiUrls, Elements {
		public static final String TAG = "MyOnlineFiles";
		public String content_filter;
		public static final String ALL = "all";
		public static final String FILES = "files";
		public static final String FOLDERS = "folders";

		public String content_format;
		public static final String TREE = "tree";
		public static final String LIST = "list";

		public static final String YES = "yes";
		public static final String NO = "no";

		public int start;
		public int limit;

		protected FolderActivity activity;
		protected Connection connection;
		private SQLiteDatabase db;
		private ProgressDialog dialog;
		private Mediafire mediafire;

		public MyOnlineFilesTask(FolderActivity activity, Connection connection) {
			this.connection = connection;
			this.activity = activity;
			this.dialog = new ProgressDialog(activity);
			mediafire = (Mediafire) activity.getApplication();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			this.dialog.setMessage("Fetching folders and files...");
			this.dialog.show();
		}

		@Override
		protected void onPostExecute(Folder result) {
			super.onPostExecute(result);

			try {
				Mediabase mb = new Mediabase(activity);
				SQLiteDatabase db = mb.getWritableDatabase();
				result.updateDb(db, fullImport);
				getOfflineFolderItems();
				db.close();
				createList();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				this.dialog.dismiss();
			}

		}

		@Override
		protected Folder doInBackground(Folder... params) {
			Folder folder = params[0];
			return getFolderContent(folder);
		}

		private Folder getFolderContent(Folder folder) {
			String[] types = new String[] { "folders", "files" };
			folder.subFolders = new ArrayList<Folder>();
			folder.files = new ArrayList<File>();
			folder.inserted = System.currentTimeMillis() / 1000;
			for (int i = 0; i < types.length; i++) {
				InputStream in = null;
				try {
					in = connection.connect(DOMAIN + "/" + MYFILES_URL, new String[] { "session_token=" + mediafire.getSessionToken(),
							"folder_key=" + folder.folderKey, "response_format=json", "content_type=" + types[i] });

					if (in == null) {
						Log.e(TAG, "Could not read from " + GET_LOGIN_TOKEN_URL);
					}
					Log.d(TAG, "Starting my files parser");

					StringBuilder builder = new StringBuilder();
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					String line;
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}
					String response = builder.toString();
					MyFilesJSONParser p = new MyFilesJSONParser(response);
					Folder current = p.folder;
					for (int j = 0; j < current.subFolders.size(); j++) {
						Folder f = current.subFolders.get(j);
						f.parent = folder.folderKey;
						folder.subFolders.add(f);
						if (mediafire.isFullImport()) {
							getFolderContent(f);
						}
					}
					for (int k = 0; k < current.files.size(); k++) {
						File f = current.files.get(k);
						f.parent = folder.folderKey;
						folder.files.add(f);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						in.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return folder;
		}
	}

}
