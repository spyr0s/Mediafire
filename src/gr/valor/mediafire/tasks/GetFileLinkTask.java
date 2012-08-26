package gr.valor.mediafire.tasks;

import gr.valor.mediafire.Mediafire;
import gr.valor.mediafire.PrefConstants;
import gr.valor.mediafire.R;
import gr.valor.mediafire.activities.ViewFileActivity;
import gr.valor.mediafire.api.ApiUrls;
import gr.valor.mediafire.api.Connection;
import gr.valor.mediafire.parser.GetFileLink;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class GetFileLinkTask extends AsyncTask<String, Void, String> implements ApiUrls {

	public static final String TAG = "DownloadTask";
	private ViewFileActivity activity;
	private Connection connection;
	private Mediafire mediafire;
	private ProgressDialog d;

	public GetFileLinkTask(ViewFileActivity viewFileActivity, Connection connection) {
		this.activity = viewFileActivity;
		this.connection = connection;
		this.mediafire = activity.mediafire;
		this.d = new ProgressDialog(activity);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		this.d.setMessage("Geting link...");
		this.d.show();
	}

	@Override
	protected void onPostExecute(String url) {
		super.onPostExecute(url);
		this.d.dismiss();
		if (url != null) {
			Log.d(TAG, "Saving link: " + url);
			mediafire.setPref(PrefConstants.FILE_PREF_DOWNLOAD_LINKS, PrefConstants.PREF_TYPE_STRING, activity.fileRecord.quickkey, url);
			activity.dm = (DownloadManager) activity.getSystemService(activity.DOWNLOAD_SERVICE);
			if (new File(Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DOWNLOADS + "/"
					+ activity.fileRecord.filename).exists()) {
				Log.w(TAG, "File already exists");
			}
			Request request = new Request(Uri.parse(url)).setTitle("Downloading " + activity.fileRecord.filename)
					.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, activity.fileRecord.filename);

			activity.enqueue = activity.dm.enqueue(request);
			Toast.makeText(activity, "The file is being downloaded", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected String doInBackground(String... args) {
		String quickKey = args[0];
		String dlink = (String) mediafire.getPref(PrefConstants.FILE_PREF_DOWNLOAD_LINKS, PrefConstants.PREF_TYPE_STRING, quickKey, null);
		if (!dlink.equals("null")) {
			Log.d(TAG, "Saved link " + dlink);
			return dlink;
		}
		Log.d(TAG, "Connecting...");

		InputStream in = null;
		ArrayList<String> attr = new ArrayList<String>();
		attr.add(SESSION_TOKEN + "=" + activity.mediafire.getSessionToken());
		attr.add(RESPONSE_FORMAT + "=" + JSON);
		attr.add(QUICKKEY + "=" + quickKey);
		attr.add(LINK_TYPE + "=" + DIRECT_DOWNLOAD);
		try {
			in = connection.connect(DOMAIN + "/" + GET_LINKS_URL, attr);

			if (in == null) {
				Toast.makeText(activity, R.string.error_cant_read, Toast.LENGTH_LONG).show();
				Log.e(TAG, "Could not read from " + GET_LINKS_URL);
			}

			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			String response = builder.toString();
			GetFileLink d = new GetFileLink(response, quickKey);
			return d.url;

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
		return null;
	}

}
