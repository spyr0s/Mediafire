package gr.valor.mediafire.activities;

import eu.erikw.PullToRefreshListView;
import gr.valor.mediafire.File;
import gr.valor.mediafire.Folder;
import gr.valor.mediafire.R;
import gr.valor.mediafire.api.Connection;
import gr.valor.mediafire.api.MyOfflineFiles;
import gr.valor.mediafire.binders.FolderViewBinder;
import gr.valor.mediafire.database.Mediabase;
import gr.valor.mediafire.helpers.ActivitySwipeDetector;
import gr.valor.mediafire.helpers.SwipeInterface;
import gr.valor.mediafire.listeners.FolderItemsListener;
import gr.valor.mediafire.listeners.FolderRefreshListener;
import gr.valor.mediafire.tasks.MyOnlineFilesTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FolderActivity extends BaseActivity implements SwipeInterface {
	private static final String TAG = "FolderActivity";
	public static final String FOLDERKEY = "gr.valor.mediafire.FOLDERKEY";
	public static final String ONLINE = "gr.valor.mediafire.ONLINE";
	public static final String EMPTY_DB = "gr.valor.mediafire.EMPTY_DB";
	public static final String FULL_IMPORT = "gr.valor.mediafire.FULL_IMPORT";
	public Folder folder;

	private static final String[] FOLDER_FROM = { Folder.TYPE, Folder.NAME, Folder.CREATED, File.DOWNLOAD_ICON, File.DOWNLOADS, File.SIZE,
			File.PRIVACY };
	private static final int[] FOLDER_TO = { R.id.folder_item_type, R.id.folder_item_name, R.id.folder_item_created,
			R.id.folder_item_downicon, R.id.folder_item_downloads, R.id.folder_item_size, R.id.folder_item_privacy };
	List<Map<String, String>> folderItems = new ArrayList<Map<String, String>>();
	SimpleAdapter folderAdapter;
	private int folderResource = R.layout.folder_item;

	View.OnTouchListener gestureListener;
	public PullToRefreshListView listView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_folder);
		requestFolder();
		// Gesture detection
		ActivitySwipeDetector activitySwipeDetector = new ActivitySwipeDetector(this);
		RelativeLayout lowestLayout = (RelativeLayout) this.findViewById(R.id.activity_folder_layout);
		lowestLayout.setOnTouchListener(activitySwipeDetector);
		listView = (PullToRefreshListView) findViewById(R.id.listView_items);
		listView.setOnRefreshListener(new FolderRefreshListener(this));
	}

	public void requestFolder() {
		Log.d(TAG, "Showing folder " + mediafire.getCurrentFolder() + " force online: " + mediafire.isForceOnline());
		if (!mediafire.isOnline()) {
			if (mediafire.isForceOnline()) {
				Toast.makeText(this, "There's no internet connection", Toast.LENGTH_SHORT).show();
				mediafire.setForceOnline(false);
				listView.onRefreshComplete();
				return;
			}
			if (!mediafire.isEmptyDb()) {
				createOfflineList();
			} else {
				Toast.makeText(this, "There's no internet connection", Toast.LENGTH_SHORT).show();
			}
		} else {
			if (mediafire.getCurrentFolder().isCached(mediafire.getCacheDuration()) && !mediafire.isForceOnline()) {
				createCachedList();
			} else {
				boolean validToken = false;
				try {
					validToken = mediafire.isTokenValid();
				} catch (Exception e) {
					Intent login = new Intent(this, LoginActivity.class);
					startActivity(login);
				}
				if (!validToken) {
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
		showAlertDialog();

	}

	private void dofullImport() {
		Log.d(TAG, "Creating full import");
		mediafire.setFullImport(true);
		getOnlineFolderItems();
	}

	public void createList() {
		Mediabase m = new Mediabase(this);
		setTitle("Mediafire - " + mediafire.getCurrentFolder().name);
		SQLiteDatabase db = m.getReadableDatabase();
		String path = mediafire.getCurrentFolder().getFullPath(db);
		((TextView) findViewById(R.id.listView_title)).setText(path);
		db.close();
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
				dofullImport();
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
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("Exit Application?");
			alertDialog.setMessage("Do you want to exit the application?");
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d(TAG, "Exiting app");
					mediafire.setCloseApp(true);
					FolderActivity.this.finish();
				}
			});
			alertDialog.show();
		} else {
			Mediabase mb = new Mediabase(this);
			SQLiteDatabase db = mb.getReadableDatabase();
			try {
				mediafire.setCurrentFolder(Folder.getByFolderKey(db, mediafire.getCurrentFolder().parent));
				createOfflineList();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				db.close();
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	private void getOnlineFolderItems() {
		Connection connection = new Connection(this);
		MyOnlineFilesTask onlineFiles = new MyOnlineFilesTask(this, connection);
		onlineFiles.execute(new Folder[] { mediafire.getCurrentFolder() });
	}

	public void getOfflineFolderItems() {
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

	private void createFolderItems(Folder curFolder) {
		if (curFolder != null) {
			folderItems = curFolder.getFolderItems();
		}

	}

	private void createAdapter() {
		folderAdapter = new SimpleAdapter(this, folderItems, folderResource, FOLDER_FROM, FOLDER_TO);
		folderAdapter.setViewBinder(new FolderViewBinder(this));
	}

	private void populateList() {
		ListView listFolders = (ListView) findViewById(R.id.listView_items);
		listFolders.setAdapter(folderAdapter);
		listFolders.setOnItemClickListener(new FolderItemsListener(this));
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

}
