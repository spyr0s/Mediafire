package gr.valor.mediafire.tasks;

import gr.valor.mediafire.Mediafire;
import gr.valor.mediafire.R;
import gr.valor.mediafire.activities.LoginActivity;
import gr.valor.mediafire.api.ApiUrls;
import gr.valor.mediafire.api.Connection;
import gr.valor.mediafire.helpers.MyLog;
import gr.valor.mediafire.parser.SessionToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.os.AsyncTask;
import android.widget.Toast;

public class RenewTokenTask extends AsyncTask<String, Void, String> implements ApiUrls {
	private static final String TAG = "Session";
	public static final String PREF_NAME = "sessionToken";
	private LoginActivity activity;
	private Connection connection;
	private String token;
	private Mediafire mediafire;

	public RenewTokenTask(Mediafire mediafire, Connection connection) {
		this.mediafire = mediafire;
		this.connection = connection;
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
		}

	}

	@Override
	protected String doInBackground(String... arg0) {
		MyLog.d(TAG, "Connecting...");
		ArrayList<String> attr = new ArrayList<String>();
		attr.add(SESSION_TOKEN + "=" + mediafire.getSessionToken());
		attr.add(RESPONSE_FORMAT + "=" + JSON);
		InputStream in = null;
		try {
			in = connection.connect(DOMAIN + "/" + RENEW_SESSION_TOKEN_URL, attr);

			if (in == null) {
				Toast.makeText(activity, R.string.error_cant_read, Toast.LENGTH_LONG).show();
				MyLog.e(TAG, "Could not read from " + RENEW_SESSION_TOKEN_URL);
			}

			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			String response = builder.toString();
			SessionToken s = new SessionToken(response, true);
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