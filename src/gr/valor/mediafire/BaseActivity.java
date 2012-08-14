package gr.valor.mediafire;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class BaseActivity extends Activity {
	public Mediafire mediafire;
	public static final String TAG = "BaseActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
			// startActivity(new Intent(this, PrefsActivity.class));
			break;
		case R.id.menu_logout:
			Log.d(TAG, "Calling menu Logout");
			mediafire.removePref(Mediafire.EMAIL_PREF_NAME);
			mediafire.removePref(Mediafire.PASSWORD_PREF_NAME);
			mediafire.setEmail(null);
			mediafire.setPassword(null);
			intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			break;
		case R.id.menu_full_import:
			Log.d(TAG, "Calling menu Full import");
			intent = new Intent(this, FolderActivity.class);
			intent.putExtra(FolderActivity.FOLDERKEY, Folder.ROOT_KEY);
			intent.putExtra(FolderActivity.FULL_IMPORT, true);
			startActivity(intent);
			break;

		}
		return true;
	}
}
