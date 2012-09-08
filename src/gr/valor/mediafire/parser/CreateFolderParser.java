package gr.valor.mediafire.parser;

import gr.valor.mediafire.helpers.MyLog;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateFolderParser extends JSONParser implements Elements {

	public static final String TAG = "CreateFolderParser";
	public String folderKey = null;

	public CreateFolderParser(String jsonString) {
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
			MyLog.d(TAG, "json:" + jsonString);
			JSONObject obj = new JSONObject(jsonString);
			response = obj.getJSONObject(RESPONSE);
			action = response.getString(ACTION);
			result = response.getString(RESULT);
			MyLog.d(TAG, action + " " + result);
			if (result.equals(SUCCESS) && action.equals(ACTION_CREATE_FOLDER)) {
				folderKey = response.getString(FOLDER_KEY);
			} else {

			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
