package gr.valor.mediafire;

import gr.valor.mediafire.api.ApiUrls;
import gr.valor.mediafire.api.Connection;
import gr.valor.mediafire.parser.SessionToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends BaseActivity {
	private static final String TAG = "LoginActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "Creating activity");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		checkCredentials();
	}

	private void checkCredentials() {
		if (mediafire.hasSavedCredentials()) {
			autoLogin();
		} else {
			if (mediafire.isEmptyDb()) {
				Log.d(TAG, "Empty DB - hide offline button");
				findViewById(R.id.login_offline).setVisibility(View.GONE);
			} else {
				Log.d(TAG, "Not Empty DB - show offline button");
				findViewById(R.id.login_offline).setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkCredentials();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	public void autoLogin() {
		Log.d(TAG, "Auto login");
		if (mediafire.isTokenValid()) {
			Log.d(TAG, "Valid token show folders");
			showFolders();
		} else {
			Log.d(TAG, "Not valid token - login");
			login();
		}
	}

	public void doLogin(View view) {
		EditText et_email = (EditText) findViewById(R.id.login_username);
		EditText et_password = (EditText) findViewById(R.id.login_password);
		CheckBox cb_remember = (CheckBox) findViewById(R.id.login_remember);
		mediafire.setEmail(et_email.getText().toString());
		mediafire.setPassword(et_password.getText().toString());
		mediafire.setRememberMe(cb_remember.isChecked());
		login();
	}

	private void login() {
		String email = mediafire.getEmail();
		String password = mediafire.getPassword();
		Connection connection = new Connection(this);
		if (mediafire.isOnline()) {
			Log.d(TAG, "Getting session token for " + email + " " + password);
			LoginTask session = new LoginTask(email, password, this, connection);
			session.execute();
		} else {
			showFolders();
		}
	}

	public void doOffline(View view) {

		showFolders();
	}

	void showFolders() {
		Intent intent = new Intent(this, FolderActivity.class);
		startActivity(intent);
	}

}

class LoginTask extends AsyncTask<String, Void, String> implements ApiUrls {
	private static final String TAG = "Session";
	public static final String PREF_NAME = "sessionToken";
	private String email;
	private String password;
	private LoginActivity activity;
	private Connection connection;
	private String token;
	private ProgressDialog d;

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
		this.d.setMessage("Login in...");
		this.d.show();
	}

	@Override
	protected void onPostExecute(String result) {
		this.d.dismiss();
		super.onPostExecute(result);
		if (result == null || result.equals("null")) {
			Log.d(TAG, "Wrong credentials");
			Toast.makeText(LoginTask.this.activity, "Could not log you in", Toast.LENGTH_SHORT).show();
		} else {
			Log.d(TAG, "Setting credentials");
			activity.mediafire.setSessionToken(result);
			activity.mediafire.setSessionTokenCreationTime(System.currentTimeMillis() / 1000);
			if (activity.mediafire.isTokenValid()) {
				Log.d(TAG, "Saving credentials and showing folders");
				activity.mediafire.saveCredentials();
				activity.showFolders();
			} else {
				Log.d(TAG, "Not valid token");
			}
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
			SessionToken s = new SessionToken(response);
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

	public static boolean renew() {
		// TODO Auto-generated method stub
		return true;
	}

}