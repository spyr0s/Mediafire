package gr.valor.mediafire;

import gr.valor.mediafire.api.Connection;
import gr.valor.mediafire.database.FolderRecord;
import gr.valor.mediafire.database.Mediabase;
import gr.valor.mediafire.helpers.MyLog;
import gr.valor.mediafire.tasks.RenewTokenTask;

import java.io.File;
import java.util.concurrent.ExecutionException;

import android.app.Application;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Mediafire extends Application implements PrefConstants {
	public static final String TAG = "Mediafire";

	public static final int TOKEN_LIFETIME = 600;
	public static final int TOKEN_RENEW_TIME = 400;
	public static final String CLOSE_APP = "closeApplication";
	private boolean isLoggedIn = false;
	private String email = null;
	private String password = null;
	private boolean rememberMe = false;
	private String sessionToken = null;
	private FolderRecord currentFolder;
	private boolean fullImport = false;
	private boolean online = false;
	private boolean onWifi = false;
	private boolean onGsm = false;
	private boolean emptyDb = false;
	private boolean allowGsm = false;
	private boolean tokenValid = false;
	private long sessionTokenCreationTime = 0L;
	private int cacheDuration = 0;
	private boolean closeApp;
	private boolean forceOnline;
	private static Mediabase mediabase = null;
	private static SQLiteDatabase database;

	private static String accountEmail;

	private String downloadPath;

	@Override
	public void onCreate() {
		super.onCreate();
		setEmail((String) getPref(PREF_TYPE_STRING, PREF_KEY_EMAIL, null));
		setPassword((String) getPref(PREF_TYPE_STRING, PREF_KEY_PASSWORD, null));
		mediabase = new Mediabase(getApplicationContext());
	}

	public static Mediabase getMediabase() {
		return mediabase;
	}

	public static SQLiteDatabase getDb() {
		if (database == null) {
			database = getMediabase().getWritableDatabase();
		}
		return database;
	}

	public void saveCredentials() {
		if (rememberMe) {
			MyLog.d(TAG, "Saving credentials");
			setPref(PREF_TYPE_STRING, PREF_KEY_EMAIL, getEmail());
			setPref(PREF_TYPE_STRING, PREF_KEY_PASSWORD, getPassword());

		}
	}

	public boolean setPref(int type, String pref, Object value) {
		return setPref(null, type, pref, value);
	}

	public boolean setPref(String prefFile, int type, String pref, Object value) {
		SharedPreferences prefs;
		if (prefFile == null) {
			prefs = PreferenceManager.getDefaultSharedPreferences(this);
		} else {
			prefs = getSharedPreferences(prefFile, MODE_PRIVATE);
		}
		SharedPreferences.Editor editor = prefs.edit();
		switch (type) {
		case PREF_TYPE_INT:
			try {
				int intV = Integer.parseInt(String.valueOf(value));
				editor.putInt(pref, intV);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return false;
			}
			break;
		case PREF_TYPE_LONG:
			try {
				long longV = Long.parseLong(String.valueOf(value));
				editor.putLong(pref, longV);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return false;
			}
			break;
		case PREF_TYPE_BOOLEAN:
			// boolean boolV = Boolean.parseBoolean(String.valueOf(value));
			boolean boolV = (Boolean) value;
			editor.putBoolean(pref, boolV);
			break;
		case PREF_TYPE_STRING:
			String strV = String.valueOf(value);
			editor.putString(pref, strV);
			break;
		default:
			break;
		}
		return editor.commit();
	}

	public Object getPref(int type, String pref, Object def) {
		return getPref(null, type, pref, def);
	}

	public Object getPref(String prefFile, int type, String pref, Object def) {
		SharedPreferences prefs;
		if (prefFile == null) {
			prefs = PreferenceManager.getDefaultSharedPreferences(this);
		} else {
			prefs = getSharedPreferences(prefFile, MODE_PRIVATE);
		}
		switch (type) {
		case PREF_TYPE_INT:
			try {
				return prefs.getInt(pref, Integer.parseInt(String.valueOf(def)));

			} catch (Exception e) {
				e.printStackTrace();
				return Integer.parseInt(String.valueOf(def));
			}
		case PREF_TYPE_LONG:
			try {
				return prefs.getLong(pref, Long.parseLong(String.valueOf(def)));
			} catch (Exception e) {
				e.printStackTrace();
				return Long.parseLong(String.valueOf(def));
			}
		case PREF_TYPE_BOOLEAN:
			return prefs.getBoolean(pref, (Boolean) def);
		case PREF_TYPE_STRING:
			return prefs.getString(pref, String.valueOf(def));
		default:
			break;
		}
		return null;
	}

	public void removePref(String pref) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(pref);
		editor.commit();
	}

	public boolean hasSavedCredentials() {
		return email != null && password != null && !email.equals("null") && !password.equals("null");
	}

	public long getSessionTokenCreationTime() {
		if (sessionTokenCreationTime == 0) {
			sessionTokenCreationTime = (Long) getPref(PREF_TYPE_LONG, PREF_KEY_SESSION_TOKEN_TIME, sessionTokenCreationTime);
		}
		return sessionTokenCreationTime;
	}

	public void setSessionTokenCreationTime(long sessionTokenCreationTime) {
		this.sessionTokenCreationTime = sessionTokenCreationTime;
		setPref(PREF_TYPE_LONG, PREF_KEY_SESSION_TOKEN_TIME, sessionTokenCreationTime);
	}

	public boolean isLoggedIn() throws Exception {
		return isTokenValid();
	}

	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}

	public FolderRecord getCurrentFolder() {
		if (currentFolder != null) {
			return currentFolder;
		} else {
			FolderRecord f = new FolderRecord();
			try {
				f = new FolderRecord(FolderRecord.getRootKey());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return f;

		}
	}

	public void setCurrentFolder(FolderRecord currentFolder) {
		this.currentFolder = currentFolder;
	}

	public boolean isFullImport() {
		return fullImport;
	}

	public void setFullImport(boolean fullImport) {
		this.fullImport = fullImport;
	}

	public boolean isOnline() {
		return isOnWifi() || (isOnGsm() && isAllowGsm());
	}

	public boolean isOnWifi() {
		return Connection.isActive(getApplicationContext(), Connection.WIFI);
	}

	public boolean isOnGsm() {
		return Connection.isActive(getApplicationContext(), Connection.GSM);
	}

	public boolean isEmptyDb() {
		return new Mediabase(getApplicationContext()).isEmpty();
	}

	public boolean isAllowGsm() {
		return (Boolean) getPref(PREF_TYPE_BOOLEAN, getString(R.string.pref_gsmKey), false);
	}

	public void setAllowGsm(boolean allowGsm) {
		this.allowGsm = allowGsm;
	}

	public boolean isTokenValid() throws Exception {
		if (getSessionToken() == null || System.currentTimeMillis() / 1000 - getSessionTokenCreationTime() > TOKEN_LIFETIME) {
			MyLog.d(TAG, "Token is null. Need to login");
			throw new Exception("Null Token");
		} else if (System.currentTimeMillis() / 1000 - getSessionTokenCreationTime() > TOKEN_RENEW_TIME) {
			MyLog.d(TAG, "Renewing token");
			return renewSession();
		} else {
			MyLog.d(TAG, "A valid token");
			return true;
		}

	}

	private boolean renewSession() {
		String email = getEmail();
		String password = getPassword();
		Connection connection = new Connection(this);
		MyLog.d(TAG, "Getting session token for " + email + " " + password);
		RenewTokenTask session = new RenewTokenTask(this, connection);
		session.execute();
		try {
			return session.get() != null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
		Mediafire.accountEmail = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isRememberMe() {
		return rememberMe;
	}

	public void setRememberMe(boolean rememberMe) {
		this.rememberMe = rememberMe;
	}

	public String getSessionToken() {
		if (sessionToken == null) {
			sessionToken = (String) getPref(PREF_TYPE_STRING, PREF_KEY_SESSION_TOKEN, null);
		}
		return sessionToken;
	}

	public void setSessionToken(String sessionToken) {
		this.sessionToken = sessionToken;
		setPref(PREF_TYPE_STRING, PREF_KEY_SESSION_TOKEN, sessionToken);
	}

	/**
	 * @param cacheDuration
	 *            the cacheDuration to set
	 */
	public void setCacheDuration(int cacheDuration) {
		setPref(PREF_TYPE_STRING, getString(R.string.pref_cacheKey), String.valueOf(cacheDuration));
		this.cacheDuration = cacheDuration;
	}

	/**
	 * @return the cacheDuration
	 */
	public int getCacheDuration() {
		return Integer.parseInt(String.valueOf(getPref(PREF_TYPE_STRING, getString(R.string.pref_cacheKey), "0")));

	}

	/**
	 * @param closeApp
	 *            the closeApp to set
	 */
	public void setCloseApp(boolean closeApp) {
		this.closeApp = closeApp;
	}

	/**
	 * @return the closeApp
	 */
	public boolean isCloseApp() {
		return closeApp;
	}

	/**
	 * @param forceOnline
	 *            the forceOnline to set
	 */
	public void setForceOnline(boolean forceOnline) {
		this.forceOnline = forceOnline;
	}

	/**
	 * @return the forceOnline
	 */
	public boolean isForceOnline() {
		return forceOnline;
	}

	public void setDownloadPath(String path) {
		this.downloadPath = path;
	}

	public String getDownloadPath() {
		if (isExternalStoragePresent()) {
			String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/"
					+ getPref(PREF_TYPE_STRING, getString(R.string.pref_downloadDirKey), "mediafire");
			if (!(new File(path).exists())) {
				new File(path).mkdirs();
			}
			return path;
		}
		return null;

	}

	private boolean isExternalStoragePresent() {

		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		if (!((mExternalStorageAvailable) && (mExternalStorageWriteable))) {
			Toast.makeText(getApplicationContext(), "SD card not present", Toast.LENGTH_LONG).show();

		}
		return (mExternalStorageAvailable) && (mExternalStorageWriteable);
	}

	public static String getAccountEmail() {
		return Mediafire.accountEmail;
	}

}
