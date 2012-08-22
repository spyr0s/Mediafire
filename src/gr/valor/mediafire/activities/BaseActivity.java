package gr.valor.mediafire.activities;

import gr.valor.mediafire.Folder;
import gr.valor.mediafire.Mediafire;
import gr.valor.mediafire.R;
import gr.valor.mediafire.R.id;
import gr.valor.mediafire.R.menu;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

public abstract class BaseActivity extends Activity {
	public Mediafire mediafire;
	public static final String TAG = "BaseActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mediafire = (Mediafire) getApplication();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Log.d(TAG, "Calling menu Preferences");
			startActivity(new Intent(this, PrefsActivity.class));
			break;
		case R.id.menu_logout:
			Log.d(TAG, "Calling menu Logout");
			mediafire.removePref(Mediafire.PREF_KEY_EMAIL);
			mediafire.removePref(Mediafire.PREF_KEY_PASSWORD);
			mediafire.setEmail(null);
			mediafire.setPassword(null);
			mediafire.setSessionToken(null);
			mediafire.setSessionTokenCreationTime(0);
			intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			break;
		case R.id.menu_full_import:
			Log.d(TAG, "Calling menu Full import");
			mediafire.setCurrentFolder(Folder.createRootFolder());
			mediafire.setFullImport(true);
			intent = new Intent(this, FolderActivity.class);
			startActivity(intent);
			break;

		}
		return true;
	}
}
