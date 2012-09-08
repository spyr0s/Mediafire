package gr.valor.mediafire.parser;

import gr.valor.mediafire.helpers.MyLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetFileLinkParser extends JSONParser implements Elements {

	public static final String TAG = "DownloadLink";
	public String url;
	private String quickKey;

	public GetFileLinkParser(String jsonString, String quickKey) {
		this.jsonString = jsonString;
		this.quickKey = quickKey;
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
			if (result.equals(SUCCESS) && action.equals(ACTION_GET_FILE_LINK)) {
				JSONArray links = response.getJSONArray(LINKS);
				for (int i = 0; i < links.length(); i++) {
					JSONObject l = (JSONObject) links.get(i);
					if (l.getString(QUICKKEY).equals(quickKey)) {
						this.url = l.getString(DIRECT_DOWNLOAD);
					}
				}
			} else {

			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
