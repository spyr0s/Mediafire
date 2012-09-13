package gr.valor.mediafire.activities;

import gr.valor.mediafire.Mediafire;
import gr.valor.mediafire.R;
import gr.valor.mediafire.database.FolderRecord;
import gr.valor.mediafire.helpers.MyLog;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class BaseActivity extends Activity {
	public Mediafire mediafire;
	public BroadcastReceiver receiver;
	public long enqueue;
	public DownloadManager dm;
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
			MyLog.d(TAG, "Calling menu Preferences");
			startActivity(new Intent(this, PrefsActivity.class));
			break;
		case R.id.menu_logout:
			MyLog.d(TAG, "Calling menu Logout");
			mediafire.setAutoLogin(false);
			mediafire.removePref(Mediafire.PREF_KEY_SESSION_TOKEN);
			mediafire.removePref(Mediafire.PREF_KEY_SESSION_TOKEN_TIME);
			mediafire.setEmail(null);
			mediafire.setPassword(null);
			mediafire.setSessionToken(null);
			mediafire.setSessionTokenCreationTime(0);
			intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			break;
		case R.id.menu_full_import:
			MyLog.d(TAG, "Calling menu Full import");
			mediafire.setCurrentFolder(new FolderRecord());
			mediafire.setFullImport(true);
			intent = new Intent(this, FolderActivity.class);
			startActivity(intent);
			break;

		}
		return true;
	}
}
