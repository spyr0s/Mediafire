package gr.valor.mediafire.tasks;

import gr.valor.mediafire.Mediafire;
import gr.valor.mediafire.activities.FolderActivity;
import gr.valor.mediafire.api.ApiUrls;
import gr.valor.mediafire.api.Connection;
import gr.valor.mediafire.database.FileRecord;
import gr.valor.mediafire.database.FolderRecord;
import gr.valor.mediafire.helpers.MyLog;
import gr.valor.mediafire.parser.Elements;
import gr.valor.mediafire.parser.MyFilesJSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

public class MyOnlineFilesTask extends AsyncTask<FolderRecord, Void, FolderRecord> implements ApiUrls, Elements {
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
	private String currentFolder;

	public MyOnlineFilesTask(FolderActivity activity, Connection connection) {
		this.connection = connection;
		this.activity = activity;
		this.dialog = new ProgressDialog(activity);
		mediafire = (Mediafire) activity.getApplication();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		this.dialog.setMessage("Fetching " + mediafire.getCurrentFolder().name + "...");
		if (!this.dialog.isShowing() && !mediafire.isForceOnline()) {
			this.dialog.show();
		}
	}

	@Override
	protected void onPostExecute(FolderRecord result) {
		super.onPostExecute(result);

		try {
			result.updateDb(mediafire.isFullImport());
			activity.getOfflineFolderItems();
			mediafire.setForceOnline(false);
			activity.listView.onRefreshComplete();
			activity.createList();
			mediafire.setFullImport(false);
			Toast.makeText(activity, "Folder updated", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
		}

	}

	@Override
	protected FolderRecord doInBackground(FolderRecord... params) {
		FolderRecord folder = params[0];
		return getFolderContent(folder);
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		super.onProgressUpdate(values);
		if (!this.dialog.isShowing() && !mediafire.isForceOnline()) {
			this.dialog.setMessage("Fetching " + this.currentFolder + "...");
		}
	}

	private FolderRecord getFolderContent(FolderRecord folder) {
		String[] types = new String[] { FOLDERS, FILES };
		ArrayList<String> attr = new ArrayList<String>();
		attr.add(ApiUrls.SESSION_TOKEN + "=" + mediafire.getSessionToken());
		attr.add(FOLDER_KEY + "=" + folder.folderKey);
		attr.add(RESPONSE_FORMAT + "=" + JSON);
		currentFolder = folder.name;
		publishProgress();
		folder.subFolders = new ArrayList<FolderRecord>();
		folder.files = new ArrayList<FileRecord>();
		folder.inserted = System.currentTimeMillis() / 1000;
		for (int i = 0; i < types.length; i++) {
			if (attr.contains(CONTENT_TYPE)) {
				attr.remove(CONTENT_TYPE);
			}
			attr.add(CONTENT_TYPE + "=" + types[i]);

			InputStream in = null;
			try {
				in = connection.connect(DOMAIN + "/" + MYFILES_URL, attr);

				if (in == null) {
					MyLog.e(TAG, "Could not read from " + GET_LOGIN_TOKEN_URL);
				}

				StringBuilder builder = new StringBuilder();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				String response = builder.toString();
				MyFilesJSONParser p = new MyFilesJSONParser(response);
				FolderRecord current = p.folder;
				for (int j = 0; j < current.subFolders.size(); j++) {
					FolderRecord f = current.subFolders.get(j);
					f.parent = folder.folderKey;
					folder.subFolders.add(f);
					if (mediafire.isFullImport()) {
						getFolderContent(f);
					}
				}
				for (int k = 0; k < current.files.size(); k++) {
					FileRecord f = current.files.get(k);
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