package gr.valor.mediafire.tasks;

import gr.valor.mediafire.R;
import gr.valor.mediafire.activities.FolderActivity;
import gr.valor.mediafire.api.ApiUrls;
import gr.valor.mediafire.api.Connection;
import gr.valor.mediafire.database.FileRecord;
import gr.valor.mediafire.helpers.MyLog;
import gr.valor.mediafire.parser.Elements;
import gr.valor.mediafire.parser.SimpleParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

public class DeleteFileTask extends AsyncTask<Void, Void, Boolean> implements ApiUrls {
	public static final String TAG = "DeleteFileTask";
	private FolderActivity activity;
	private Connection connection;
	private ArrayList<String> attributes;
	private ProgressDialog d;
	private FileRecord fileRecord;
	private int position;

	public DeleteFileTask(FolderActivity activity, Connection connection, ArrayList<String> attr, FileRecord fileRecord, int position) {
		this.activity = activity;
		this.connection = connection;
		this.attributes = attr;
		this.d = new ProgressDialog(activity);
		this.fileRecord = fileRecord;
		this.position = position - 1;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		this.d.setMessage("Deleting file...");
		this.d.show();
	}

	@Override
	protected void onPostExecute(Boolean success) {
		super.onPostExecute(success);
		this.d.dismiss();
		if (success) {
			this.fileRecord.delete();
			activity.folderItems.remove(position);
			activity.folderAdapter.notifyDataSetChanged();
		} else {
			Toast.makeText(activity, "Could not delete the file", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		MyLog.d(TAG, "Connecting...");
		ArrayList<String> attr = new ArrayList<String>();
		attr.add(SESSION_TOKEN + "=" + activity.mediafire.getSessionToken());
		attr.add(RESPONSE_FORMAT + "=" + JSON);
		attr.addAll(attributes);
		InputStream in = null;
		try {
			in = connection.connect(DOMAIN + "/" + DELETE_FILE_URL, attr);

			if (in == null) {
				Toast.makeText(activity, R.string.error_cant_read, Toast.LENGTH_LONG).show();
				MyLog.e(TAG, "Could not read from " + DELETE_FILE_URL);
			}

			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			String response = builder.toString();
			SimpleParser simple = new SimpleParser(response, Elements.ACTION_DELETE_FILE);
			if (simple.success) {
				return true;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
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
		return false;
	}
}
