package gr.valor.mediafire.tasks;

import gr.valor.mediafire.Mediafire;
import gr.valor.mediafire.R;
import gr.valor.mediafire.activities.LoginActivity;
import gr.valor.mediafire.api.ApiUrls;
import gr.valor.mediafire.api.Connection;
import gr.valor.mediafire.helpers.Helper;
import gr.valor.mediafire.helpers.MyLog;
import gr.valor.mediafire.parser.SessionToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.os.AsyncTask;
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
			MyLog.d(TAG, "Wrong credentials");
			Toast.makeText(this.activity, "Wrong username or password", Toast.LENGTH_LONG).show();
		} else {
			MyLog.d(TAG, "Setting credentials");
			activity.mediafire.setSessionToken(result);
			activity.mediafire.setSessionTokenCreationTime(System.currentTimeMillis() / 1000);
			MyLog.d(TAG, "Saving credentials and showing folders");
			activity.mediafire.saveCredentials();
			activity.showFolders();

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
			SessionToken s = new SessionToken(response, false);
			return s.sessionToken;
		} catch (IOException e) {
			MyLog.d(TAG, "I/O Exception");
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