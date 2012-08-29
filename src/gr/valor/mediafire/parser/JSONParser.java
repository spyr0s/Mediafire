package gr.valor.mediafire.parser;


import org.json.JSONException;
import org.json.JSONObject;

public abstract class JSONParser implements Elements {
	protected JSONObject response;
	protected String jsonString;
	protected String action;
	protected String result;

	public abstract void parse() throws JSONException;

	public boolean checkAction(String act) throws JSONException {
		JSONObject obj = new JSONObject(jsonString);
		response = obj.getJSONObject(RESPONSE);
		action = response.getString(ACTION);
		result = response.getString(RESULT);
		if (result.equals(SUCCESS) && act.equals(action)) {
			return true;
		}
		throw new JSONException("Wrong action " + action + " instead of requested " + act);
	}

	public String getStringValue(JSONObject obj, String name) throws JSONException {
		return getStringValue(obj, name, "");
	}

	public String getStringValue(JSONObject obj, String name, String defaultValue) throws JSONException {
		return obj.has(name) ? obj.getString(name) : defaultValue;
	}

}
