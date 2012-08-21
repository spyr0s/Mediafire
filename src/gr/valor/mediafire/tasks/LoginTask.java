package gr.valor.mediafire.tasks;

import gr.valor.mediafire.Mediafire;
import gr.valor.mediafire.R;
import gr.valor.mediafire.activities.LoginActivity;
import gr.valor.mediafire.api.ApiUrls;
import gr.valor.mediafire.api.Connection;
import gr.valor.mediafire.helpers.Helper;
import gr.valor.mediafire.parser.SessionToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class LoginTask extends AsyncTask<String, Void, String> implements ApiUrls {
	private static final String TAG = "Session";
	public static final String PREF_NAME = "sessionToken";
	private String email;
	private String password;
	private LoginActivity activity;
	private Connection connection;
	private String token;
	private ProgressDialog d;
	private Mediafire mediafire;

	public LoginTask(String email, String password, LoginActivity activity, Connection connection) {
		this.email = email;
		this.password = password;
		this.activity = activity;
		this.connection = connection;
		this.d = new ProgressDialog(activity);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		this.d.setMessage("Authenticating...");
		this.d.show();
	}

	@Override
	protected void onPostExecute(String result) {
		this.d.dismiss();
		super.onPostExecute(result);
		if (result == null || result.equals("null")) {
			Log.d(TAG, "Wrong credentials");
			Toast.makeText(this.activity, "Wrong username or password", Toast.LENGTH_LONG).show();
		} else {
			Log.d(TAG, "Setting credentials");
			activity.mediafire.setSessionToken(result);
			activity.mediafire.setSessionTokenCreationTime(System.currentTimeMillis() / 1000);
			Log.d(TAG, "Saving credentials and showing folders");
			activity.mediafire.saveCredentials();
			activity.showFolders();

		}

	}

	@Override
	protected String doInBackground(String... arg0) {
		Log.d(TAG, "Connecting...");
		InputStream in = null;
		try {
			in = connection.connect(DOMAIN + "/" + GET_SESSION_TOKEN_URL, new String[] { "email=" + email, "password=" + password,
					"application_id=" + APP_ID, "signature=" + Helper.getSignature(email, password), "response_format=json" });

			if (in == null) {
				Toast.makeText(activity, R.string.error_cant_read, Toast.LENGTH_LONG).show();
				Log.e(TAG, "Could not read from " + GET_SESSION_TOKEN_URL);
			}

			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			String response = builder.toString();
			SessionToken s = new SessionToken(response, false);
			return s.sessionToken;
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

	public String getToken() {
		return token;
	}
}