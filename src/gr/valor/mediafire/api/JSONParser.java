package gr.valor.mediafire.api;

import gr.valor.mediafire.parser.Elements;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class JSONParser implements Elements {
	protected JSONObject response;
	protected String jsonString;
	protected String action;
	protected String result;

	public abstract void parse() throws JSONException;

	public boolean checkAction(String action) throws JSONException {
		JSONObject obj = new JSONObject(jsonString);
		response = obj.getJSONObject(RESPONSE);
		action = response.getString(ACTION);
		result = response.getString(RESULT);
		if (result.equals(SUCCESS) && action.equals(action)) {
			return true;
		}
		return false;
	}

	public String getStringValue(JSONObject obj, String name) throws JSONException {
		return getStringValue(obj, name, "");
	}

	public String getStringValue(JSONObject obj, String name, String defaultValue) throws JSONException {
		return obj.has(name) ? obj.getString(name) : defaultValue;
	}

}
