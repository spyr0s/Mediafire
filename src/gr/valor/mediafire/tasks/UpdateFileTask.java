package gr.valor.mediafire.tasks;

import gr.valor.mediafire.R;
import gr.valor.mediafire.activities.FolderActivity;
import gr.valor.mediafire.api.ApiUrls;
import gr.valor.mediafire.api.Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class UpdateFileTask extends AsyncTask<Void, Void, Boolean> implements ApiUrls {
	public static final String TAG = "UpdateFileTask";
	private FolderActivity activity;
	private Connection connection;
	private ArrayList<String> attributes;
	private ProgressDialog d;

	public UpdateFileTask(FolderActivity activity, Connection connection, ArrayList<String> attr) {
		this.activity = activity;
		this.connection = connection;
		this.attributes = attr;
		this.d = new ProgressDialog(activity);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		this.d.setMessage("Geting link...");
		this.d.show();
	}

	@Override
	protected void onPostExecute(Boolean success) {
		super.onPostExecute(success);
		this.d.dismiss();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		Log.d(TAG, "Connecting...");
		ArrayList<String> attr = new ArrayList<String>();
		attr.add(SESSION_TOKEN + "=" + activity.mediafire.getSessionToken());
		attr.add(RESPONSE_FORMAT + "=" + JSON);
		attr.addAll(attributes);
		InputStream in = null;
		try {
			in = connection.connect(DOMAIN + "/" + UPDATE_FILE_URL, attr);

			if (in == null) {
				Toast.makeText(activity, R.string.error_cant_read, Toast.LENGTH_LONG).show();
				Log.e(TAG, "Could not read from " + UPDATE_FILE_URL);
			}

			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			String response = builder.toString();

			// GetFileLink d = new GetFileLink(response);
			return true;

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
		return false;
	}
}
