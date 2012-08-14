package gr.valor.mediafire.parser;

import gr.valor.mediafire.api.JSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class SessionToken extends JSONParser implements Elements {
	public static final String TAG = "SessionToken";
	public String sessionToken = null;

	public SessionToken(String jsonString) {
		this.jsonString = jsonString;
		try {
			parse();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void parse() throws JSONException {
		try {
			Log.d(TAG, "Session json:" + jsonString);
			JSONObject obj = new JSONObject(jsonString);
			response = obj.getJSONObject(RESPONSE);
			action = response.getString(ACTION);
			result = response.getString(RESULT);
			Log.d(TAG, action + " " + result);
			if (result.equals(SUCCESS) && action.equals(GET_SESSION_TOKEN_ACTION)) {
				sessionToken = response.getString(SESSION_TOKEN);
				Log.d(TAG, "session token " + sessionToken);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
