package gr.valor.mediafire.parser;

import gr.valor.mediafire.helpers.MyLog;

import org.json.JSONException;
import org.json.JSONObject;

public class SimpleParser extends JSONParser {
	public static final String TAG = "SimpleParser";
	public String reqAction;
	public boolean success = false;

	public SimpleParser(String jsonString, String reqAction) {
		this.jsonString = jsonString;
		this.reqAction = reqAction;
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
			MyLog.d(TAG, "json:" + jsonString);
			JSONObject obj = new JSONObject(jsonString);
			response = obj.getJSONObject(RESPONSE);
			action = response.getString(ACTION);
			result = response.getString(RESULT);
			MyLog.d(TAG, action + " " + result);
			if (result.equals(SUCCESS) && action.equals(reqAction)) {
				success = true;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
