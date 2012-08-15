package gr.valor.mediafire;

import gr.valor.mediafire.api.Connection;
import gr.valor.mediafire.helpers.Helper;
import android.content.Intent;
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
		if (!mediafire.isOnline()) {
			showFolders();
		} else {
			checkCredentials();
		}
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
		if (!Helper.isValidEmail(et_email.getText().toString())) {
			Toast.makeText(this, "This is not a valid email address", Toast.LENGTH_SHORT).show();
			return;
		}
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
		Log.d(TAG, "Getting session token for " + email + " " + password);
		LoginTask session = new LoginTask(email, password, this, connection);
		session.execute();
	}

	public void doOffline(View view) {

		showFolders();
	}

	void showFolders() {
		Intent intent = new Intent(this, FolderActivity.class);
		startActivity(intent);
	}

}
