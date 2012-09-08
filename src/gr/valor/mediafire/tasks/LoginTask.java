package gr.valor.mediafire.tasks;

import gr.valor.mediafire.R;
import gr.valor.mediafire.activities.LoginActivity;
import gr.valor.mediafire.api.Connection;
import gr.valor.mediafire.helpers.Helper;
import gr.valor.mediafire.helpers.MyLog;
import gr.valor.mediafire.parser.SessionTokenParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.widget.Toast;

public class LoginTask extends MediafireTask<String, Void, String> {
	private static final String TAG = "Session";
	public static final String PREF_NAME = "sessionToken";
	private String email;
	private String password;
	private String token;

	public LoginTask(String email, String password, LoginActivity activity, Connection connection) {
		this.email = email;
		this.password = password;
		this.activity = (LoginActivity) activity;
		this.mediafire = ((LoginActivity) activity).mediafire;
		this.connection = connection;
		this.d = new ProgressDialog(activity);
	}

	@Override
	protected void onPostExecute(String result) {

		super.onPostExecute(result);
		if (result == null || result.equals("null")) {
			MyLog.d(TAG, "Wrong credentials");
			Toast.makeText(this.activity, "Wrong username or password", Toast.LENGTH_LONG).show();
		} else {
			MyLog.d(TAG, "Setting credentials");
			mediafire.setSessionToken(result);
			mediafire.setSessionTokenCreationTime(System.currentTimeMillis() / 1000);
			MyLog.d(TAG, "Saving credentials and showing folders");
			mediafire.saveCredentials();
			((LoginActivity) activity).showFolders();

		}

	}

	@Override
	protected String doInBackground(String... arg0) {
		MyLog.d(TAG, "Connecting...");
		ArrayList<String> attr = new ArrayList<String>();
		attr.add(EMAIL + "=" + email);
		attr.add(PASSWORD + "=" + password);
		attr.add(APPLICATION_ID + "=" + APP_ID);
		attr.add(SIGNATURE + "=" + Helper.getSignature(email, password));
		attr.add(RESPONSE_FORMAT + "=" + JSON);
		InputStream in = null;
		try {
			in = connection.connect(DOMAIN + "/" + GET_SESSION_TOKEN_URL, attr);

			if (in == null) {
				Toast.makeText(activity, R.string.error_cant_read, Toast.LENGTH_LONG).show();
				MyLog.e(TAG, "Could not read from " + GET_SESSION_TOKEN_URL);
			}

			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			String response = builder.toString();
			SessionTokenParser s = new SessionTokenParser(response, false);
			return s.sessionToken;
		} catch (IOException e) {
			MyLog.d(TAG, "I/O Exception");
			notConnected = true;
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
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