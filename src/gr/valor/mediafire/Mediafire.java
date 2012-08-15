package gr.valor.mediafire;

import gr.valor.mediafire.api.Connection;
import gr.valor.mediafire.database.Mediabase;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Mediafire extends Application {
	public static final String TAG = "Mediafire";
	public static final String EMAIL_PREF_NAME = "emailPref";
	public static final String PASSWORD_PREF_NAME = "passwordPref";
	public static final int TOKEN_LIFETIME = 600;
	private boolean isLoggedIn = false;
	private String email = null;
	private String password = null;
	private boolean rememberMe = false;
	private String sessionToken = null;
	private Folder currentFolder = Folder.createRootFolder();
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

	@Override
	public void onCreate() {
		super.onCreate();
		setEmail(getStringPref(EMAIL_PREF_NAME, null));
		setPassword(getStringPref(PASSWORD_PREF_NAME, null));

	}

	public void saveCredentials() {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		if (rememberMe) {
			Log.d(TAG, "Saving credentials");
			editor.putString(EMAIL_PREF_NAME, getEmail());
			editor.putString(PASSWORD_PREF_NAME, getPassword());
		}
		editor.commit();
	}

	public String getStringPref(String prefName, String def) {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs.getString(prefName, def);

	}

	public Long getLongPref(String prefName, long def) {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs.getLong(prefName, def);

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
		return sessionTokenCreationTime;
	}

	public void setSessionTokenCreationTime(long sessionTokenCreationTime) {
		this.sessionTokenCreationTime = sessionTokenCreationTime;
	}

	public boolean isLoggedIn() {
		return isTokenValid();
	}

	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}

	public Folder getCurrentFolder() {
		return currentFolder;
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
		return allowGsm;
	}

	public void setAllowGsm(boolean allowGsm) {
		this.allowGsm = allowGsm;
	}

	public boolean isTokenValid() {
		if (getSessionToken() == null) {
			Log.d(TAG, "Token is null. Need to login");
			return false;
		}
		if (System.currentTimeMillis() / 1000 - getSessionTokenCreationTime() < TOKEN_LIFETIME) {
			Log.d(TAG, "A valid token");
			return true;
		}
		Log.d(TAG, "Renewing token");
		return LoginTask.renew();
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
		return sessionToken;
	}

	public void setSessionToken(String sessionToken) {
		this.sessionToken = sessionToken;
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
		return getLongPref(getString(R.string.pref_cacheKey), 0L);
	}

}
