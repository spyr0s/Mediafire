package gr.valor.mediafire.tasks;

import gr.valor.mediafire.Mediafire;
import gr.valor.mediafire.PrefConstants;
import gr.valor.mediafire.R;
import gr.valor.mediafire.activities.ViewFileActivity;
import gr.valor.mediafire.api.ApiUrls;
import gr.valor.mediafire.api.Connection;
import gr.valor.mediafire.helpers.MyLog;
import gr.valor.mediafire.parser.GetFileLinkParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.EditText;
import android.widget.Toast;

public class GetFileLinkTask extends AsyncTask<String, Void, String> implements ApiUrls {

	public static final String TAG = "DownloadTask";
	private ViewFileActivity activity;
	private Connection connection;
	private Mediafire mediafire;
	private ProgressDialog d;
	private String saveFilename;
	private int answer;

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
	protected void onPostExecute(final String url) {
		super.onPostExecute(url);
		this.d.dismiss();
		if (url != null) {
			answer = -1;
			MyLog.d(TAG, "Saving link: " + url);
			mediafire.setPref(PrefConstants.FILE_PREF_DOWNLOAD_LINKS, PrefConstants.PREF_TYPE_STRING, activity.fileRecord.quickkey, url);
			activity.dm = (DownloadManager) activity.getSystemService(activity.DOWNLOAD_SERVICE);
			final String orFilename = activity.fileRecord.filename;
			saveFilename = activity.fileRecord.filename;
			final String path = mediafire.getDownloadPath();
			if (path == null) {
				return;
			}
			final File file = new File(path + "/" + activity.fileRecord.filename);
			if (file.exists()) {
				AlertDialog.Builder alert = new AlertDialog.Builder(activity);
				alert.setTitle("The file already exists");
				alert.setMessage("Save the file as:");
				final EditText newFilename = new EditText(activity);
				newFilename.setText(saveFilename);
				alert.setView(newFilename);
				alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						saveFilename = newFilename.getText().toString();
						if (saveFilename.equals(orFilename)) {
							file.renameTo(new File(path + "/" + orFilename + ".tmp"));
						}
						MyLog.d(TAG, "Downloading to : " + path + "/" + saveFilename);
						downloadFile(url);
					}
				});

				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				});

				alert.show();
			}
			downloadFile(url);
		}
	}

	private void downloadFile(String url) {
		File file = new File(mediafire.getDownloadPath() + "/" + saveFilename);
		Request request = new Request(Uri.parse(url)).setTitle("Downloading " + saveFilename).setDestinationUri(Uri.fromFile(file));

		activity.enqueue = activity.dm.enqueue(request);
		activity.fileRecord.downloads++;
		activity.getViewDownloads().setText(String.valueOf(activity.fileRecord.downloads));
		activity.fileRecord.save();
		Toast.makeText(activity, "The file is being downloaded", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected String doInBackground(String... args) {
		String quickKey = args[0];
		String dlink = (String) mediafire.getPref(PrefConstants.FILE_PREF_DOWNLOAD_LINKS, PrefConstants.PREF_TYPE_STRING, quickKey, null);
		if (!dlink.equals("null")) {
			MyLog.d(TAG, "Saved link " + dlink);
			return dlink;
		}
		MyLog.d(TAG, "Connecting...");

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
				MyLog.e(TAG, "Could not read from " + GET_LINKS_URL);
			}

			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			String response = builder.toString();
			GetFileLinkParser d = new GetFileLinkParser(response, quickKey);
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
