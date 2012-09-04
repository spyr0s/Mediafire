package gr.valor.mediafire.helpers;

import android.util.Log;

public class MyLog {
	public static final boolean DEBUG = true;

	public static void d(String TAG, String message) {
		if (DEBUG) {
			Log.d(TAG, message);
		}
	}

	public static void e(String TAG, String message) {
		if (DEBUG) {
			Log.e(TAG, message);
		}
	}

	public static void w(String TAG, String message) {
		if (DEBUG) {
			Log.w(TAG, message);
		}
	}

	public static void i(String TAG, String message) {
		if (DEBUG) {
			Log.i(TAG, message);
		}
	}

}
