package gr.valor.mediafire.api;

import gr.valor.mediafire.Helper;
import gr.valor.mediafire.R;
import gr.valor.mediafire.parser.SessionTokenParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Session extends Token {
	private static final String TAG = "Session";
	public static final String PREF_NAME = "sessionToken";

	public Session(String email, String password, Context context, Connection connection) {
		super(email, password, context, connection);
	}

	protected void getToken() throws IOException {
		Log.d(TAG, "Connecting...");
		InputStream in = connection.connect(DOMAIN + "/" + GET_SESSION_TOKEN_URL,
				new String[] { "email=" + email, "password=" + password,
						"application_id=" + APP_ID,
						"signature=" + Helper.getSignature(email, password) });

		if (in == null) {
			Log.e(TAG, "Could not read from " + GET_SESSION_TOKEN_URL);
		}
		try {
			SessionTokenParser p = new SessionTokenParser(in);
			String t = p.getToken();
			if (!t.equals(null)) {
				Log.d(TAG,"token " + t);
				token = t.trim();
			} else {
				Toast.makeText(context, R.string.wrong_credentials,
						Toast.LENGTH_SHORT).show();
			}

		} finally {
			in.close();
		}
	}
}
