package gr.valor.mediafire.tasks;

import gr.valor.mediafire.R;
import gr.valor.mediafire.activities.FolderActivity;
import gr.valor.mediafire.api.Connection;
import gr.valor.mediafire.database.FolderRecord;
import gr.valor.mediafire.helpers.MyLog;
import gr.valor.mediafire.parser.Elements;
import gr.valor.mediafire.parser.SimpleParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.widget.Toast;

public class DeleteFolderTask extends MediafireTask<Void, Void, Boolean> {
	public static final String TAG = "DeleteFolderTask";
	private FolderRecord folderRecord;
	private int position;

	public DeleteFolderTask(FolderActivity activity, Connection connection, ArrayList<String> attr, FolderRecord folderRecord, int position) {
		this.activity = activity;
		this.connection = connection;
		this.attributes = attr;
		this.message = "Deleting folder...";
		this.d = new ProgressDialog(activity);

		this.mediafire = activity.mediafire;
		this.folderRecord = folderRecord;
		this.position = position - 1;
	}

	@Override
	protected void onPostExecute(Boolean success) {
		super.onPostExecute(success);
		this.d.dismiss();
		if (success) {
			this.folderRecord.delete();
			((FolderActivity) activity).requestFolder();
		} else {
			Toast.makeText(activity, "Could not delete the folder", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		MyLog.d(TAG, "Connecting...");
		ArrayList<String> attr = new ArrayList<String>();
		attr.add(SESSION_TOKEN + "=" + mediafire.getSessionToken());
		attr.add(RESPONSE_FORMAT + "=" + JSON);
		attr.addAll(attributes);
		InputStream in = null;
		try {
			in = connection.connect(DOMAIN + "/" + DELETE_FOLDER_URL, attr);

			if (in == null) {
				Toast.makeText(activity, R.string.error_cant_read, Toast.LENGTH_LONG).show();
				MyLog.e(TAG, "Could not read from " + DELETE_FOLDER_URL);
			}

			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			String response = builder.toString();
			SimpleParser simple = new SimpleParser(response, Elements.ACTION_DELETE_FOLDER);
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
