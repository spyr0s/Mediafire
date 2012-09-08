package gr.valor.mediafire.tasks;

import gr.valor.mediafire.R;
import gr.valor.mediafire.activities.FolderActivity;
import gr.valor.mediafire.api.Connection;
import gr.valor.mediafire.database.FolderItemRecord;
import gr.valor.mediafire.database.FolderRecord;
import gr.valor.mediafire.helpers.Helper;
import gr.valor.mediafire.helpers.MyLog;
import gr.valor.mediafire.parser.CreateFolderParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.widget.Toast;

public class CreateFolderTask extends MediafireTask<Void, Void, String> {

	private static final String TAG = "CreateFolderTask";
	private FolderActivity activity;

	public CreateFolderTask(FolderActivity activity, Connection connection, ArrayList<String> attr) {
		this.activity = activity;
		this.connection = connection;
		this.attributes = attr;
		this.message = "Creating folder...";
		this.d = new ProgressDialog(activity);
	}

	@Override
	protected void onPostExecute(String folderKey) {
		super.onPostExecute(folderKey);
		this.d.dismiss();
		if (folderKey != null) {
			FolderRecord folderRecord = new FolderRecord();
			folderRecord.folderKey = folderKey;
			folderRecord.itemType = FolderItemRecord.TYPE_FOLDER;
			folderRecord.name = Helper.getAttributeValue(attributes.get(1));
			folderRecord.parent = Helper.getAttributeValue(attributes.get(0));
			folderRecord.save();
			activity.folderItems.add(folderRecord.getMapItem());
			activity.folderAdapter.notifyDataSetChanged();
		} else {
			Toast.makeText(activity, "Could not create the folder", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected String doInBackground(Void... params) {
		MyLog.d(TAG, "Connecting...");
		ArrayList<String> attr = new ArrayList<String>();
		attr.add(SESSION_TOKEN + "=" + activity.mediafire.getSessionToken());
		attr.add(RESPONSE_FORMAT + "=" + JSON);
		attr.addAll(attributes);
		InputStream in = null;
		try {
			in = connection.connect(DOMAIN + "/" + CREATE_FOLDER_URL, attr);

			if (in == null) {
				Toast.makeText(activity, R.string.error_cant_read, Toast.LENGTH_LONG).show();
				MyLog.e(TAG, "Could not read from " + CREATE_FOLDER_URL);
			}

			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			String response = builder.toString();
			CreateFolderParser c = new CreateFolderParser(response);
			return c.folderKey;

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
		return null;
	}

}
