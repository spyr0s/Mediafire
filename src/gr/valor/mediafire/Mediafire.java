package gr.valor.mediafire;

import gr.valor.mediafire.api.Connection;
import gr.valor.mediafire.database.Mediabase;
import gr.valor.mediafire.tasks.RenewTokenTask;

import java.util.concurrent.ExecutionException;

import android.app.Application;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

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
	private Folder currentFolder;
	private boolean fullImport = false;
	private boolean online = false;
	private boolean onWifi = false;
	private boolean onGsm = false;
	private boolean emptyDb = false;
	private boolean allowGsm = false;
	private boolean tokenValid = false;
	private long sessionTokenCreationTime = 0L;
	private long cacheDuration = 0L;
	private SharedPreferences prefs;
	private boolean closeApp;
	private boolean forceOnline;

	@Override
	public void onCreate() {
		super.onCreate();
		setEmail((String) getPref(PREF_TYPE_STRING, PREF_KEY_EMAIL, null));
		setPassword((String) getPref(PREF_TYPE_STRING, PREF_KEY_PASSWORD, null));

	}

	public void saveCredentials() {
		if (rememberMe) {
			Log.d(TAG, "Saving credentials");
			setPref(PREF_TYPE_STRING, PREF_KEY_EMAIL, getEmail());
			setPref(PREF_TYPE_STRING, PREF_KEY_PASSWORD, getPassword());

		}
	}

	public boolean setPref(int type, String pref, Object value) {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
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
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		switch (type) {
		case PREF_TYPE_INT:
			int intDef = Integer.parseInt(String.valueOf(def));
			return prefs.getInt(pref, intDef);
		case PREF_TYPE_LONG:
			long longDef = Long.parseLong(String.valueOf(def));
			return prefs.getLong(pref, longDef);
		case PREF_TYPE_BOOLEAN:
			return prefs.getBoolean(pref, (Boolean) def);
		case PREF_TYPE_STRING:
			return prefs.getString(pref, String.valueOf(def));
		default:
			break;
		}
		return null;
	}

	public boolean getBooleanPref(String prefName, boolean def) {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs.getBoolean(prefName, def);

	}

	public int getIntPref(String prefName, int def) {
		prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		String v = prefs.getString(prefName, "");
		try {
			return v.equals("") ? def : Integer.parseInt(v);
		} catch (NumberFormatException e) {
			return def;
		}

	}

	public void removePref(String pref) {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(pref);
		editor.commit();
	}

	public boolean hasSavedCredentials() {
		return email != null & password != null;
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

	public Folder getCurrentFolder() {
		if (currentFolder != null) {
			return currentFolder;
		} else {
			Mediabase m = new Mediabase(this);
			SQLiteDatabase db = m.getReadableDatabase();
			Folder f = Folder.createRootFolder();
			try {
				f = Folder.getByFolderKey(db, Folder.ROOT_KEY);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			db.close();
			return f;

		}
	}

	public void setCurrentFolder(Folder currentFolder) {
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
		return getBooleanPref(getString(R.string.pref_gsmKey), false);
	}

	public void setAllowGsm(boolean allowGsm) {
		this.allowGsm = allowGsm;
	}

	public boolean isTokenValid() throws Exception {
		if (getSessionToken() == null || System.currentTimeMillis() / 1000 - getSessionTokenCreationTime() > TOKEN_LIFETIME) {
			Log.d(TAG, "Token is null. Need to login");
			throw new Exception("Null Token");
		} else if (System.currentTimeMillis() / 1000 - getSessionTokenCreationTime() > TOKEN_RENEW_TIME) {
			Log.d(TAG, "Renewing token");
			return renewSession();
		} else {
			Log.d(TAG, "A valid token");
			return true;
		}

	}

	private boolean renewSession() {
		String email = getEmail();
		String password = getPassword();
		Connection connection = new Connection(this);
		Log.d(TAG, "Getting session token for " + email + " " + password);
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
	public void setCacheDuration(long cacheDuration) {
		this.cacheDuration = cacheDuration;
	}

	/**
	 * @return the cacheDuration
	 */
	public long getCacheDuration() {
		return getIntPref(getString(R.string.pref_cacheKey), 0);
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

}
