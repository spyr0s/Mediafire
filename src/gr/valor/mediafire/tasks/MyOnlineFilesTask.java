package gr.valor.mediafire.tasks;

import gr.valor.mediafire.File;
import gr.valor.mediafire.Folder;
import gr.valor.mediafire.Mediafire;
import gr.valor.mediafire.activities.FolderActivity;
import gr.valor.mediafire.api.ApiUrls;
import gr.valor.mediafire.api.Connection;
import gr.valor.mediafire.database.Mediabase;
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
import android.util.Log;
import android.widget.Toast;

public class MyOnlineFilesTask extends AsyncTask<Folder, Void, Folder> implements ApiUrls, Elements {
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
	protected void onPostExecute(Folder result) {
		super.onPostExecute(result);

		try {
			Mediabase mb = new Mediabase(activity);
			SQLiteDatabase db = mb.getWritableDatabase();
			result.updateDb(db, mediafire.isFullImport());
			activity.getOfflineFolderItems();
			db.close();
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
	protected Folder doInBackground(Folder... params) {
		Folder folder = params[0];
		return getFolderContent(folder);
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		super.onProgressUpdate(values);
		if (!this.dialog.isShowing() && !mediafire.isForceOnline()) {
			this.dialog.setMessage("Fetching " + this.currentFolder + "...");
		}
	}

	private Folder getFolderContent(Folder folder) {
		String[] types = new String[] { "folders", "files" };
		currentFolder = folder.name;
		publishProgress();
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