package gr.valor.mediafire;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;

public class PrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	public static final String TAG = "PrefsActivity";
	private Mediafire mediafire;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		mediafire = (Mediafire) getApplication();
		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			initSummary(getPreferenceScreen().getPreference(i));
		}
	}

	private void initSummary(Preference p) {
		if (p instanceof PreferenceCategory) {
			PreferenceCategory pCat = (PreferenceCategory) p;
			for (int i = 0; i < pCat.getPreferenceCount(); i++) {
				initSummary(pCat.getPreference(i));
			}
		} else if (p instanceof PreferenceScreen) {
			PreferenceScreen pScreen = (PreferenceScreen) p;
			for (int i = 0; i < pScreen.getPreferenceCount(); i++) {
				initSummary(pScreen.getPreference(i));
			}
		}

		else {
			updatePrefSummary(p);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	private void updatePrefSummary(Preference p) {
		if (p instanceof ListPreference) {
			ListPreference listPref = (ListPreference) p;
			String v = listPref.getEntry() != null ? listPref.getEntry().toString() : "";
			p.setSummary(listPref.getDialogTitle() + ": " + v);
		}
		if (p instanceof EditTextPreference) {
			EditTextPreference editTextPref = (EditTextPreference) p;
			String v = editTextPref.getText() != null ? editTextPref.getText() : "";
			p.setSummary(editTextPref.getDialogMessage() + ": " + v);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		@SuppressWarnings("deprecation")
		Preference pref = findPreference(key);
		updatePrefSummary(pref);
		if (key.equals(getString(R.string.pref_cacheKey))) {
			ListPreference listPref = (ListPreference) pref;
			try {
				Long duration = Long.parseLong(listPref.getValue());
				Log.d(TAG, "Setting cache duration to " + duration);
				mediafire.setCacheDuration(duration);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				Log.w(TAG, "Setting cache duration failed ");
			}
		} else if (key.equals(getString(R.string.pref_gsmKey))) {
			CheckBoxPreference cb = (CheckBoxPreference) pref;
			mediafire.setAllowGsm(cb.isChecked());
		}
	}

}
