package gr.valor.mediafire.parser;

import gr.valor.mediafire.helpers.MyLog;

import org.json.JSONException;
import org.json.JSONObject;

public class SessionTokenParser extends JSONParser implements Elements {
	public static final String TAG = "SessionToken";
	public String sessionToken = null;
	private boolean renew;

	public SessionTokenParser(String jsonString, boolean renew) {
		this.jsonString = jsonString;
		this.renew = renew;
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
			MyLog.d(TAG, "Session json:" + jsonString);
			JSONObject obj = new JSONObject(jsonString);
			response = obj.getJSONObject(RESPONSE);
			action = response.getString(ACTION);
			result = response.getString(RESULT);
			MyLog.d(TAG, action + " " + result);
			if (result.equals(SUCCESS) && action.equals(renew ? ACTION_RENEW_SESSION_TOKEN : ACTION_GET_SESSION_TOKEN)) {
				sessionToken = response.getString(SESSION_TOKEN);
				MyLog.d(TAG, "session token " + sessionToken);
			} else {
				sessionToken = null;
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
