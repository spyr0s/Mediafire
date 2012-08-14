package gr.valor.mediafire.api;

import gr.valor.mediafire.File;
import gr.valor.mediafire.Folder;
import gr.valor.mediafire.FolderItem;
import gr.valor.mediafire.parser.Elements;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class MyFilesJSONParser extends JSONParser implements Elements {
	public static final String TAG = "MyFilesJSONParser";
	public Folder folder = new Folder();
	public JSONObject folderContent;
	public JSONArray folders;
	public JSONArray files;

	public MyFilesJSONParser(String jsonString) {
		this.jsonString = jsonString;
		try {
			parse();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void parse() throws JSONException {
		if (!checkAction(GET_SESSION_TOKEN_ACTION)) {
			throw new JSONException("Wrong action");
		}
		folder.name = FolderItem.ROOT_NAME;
		folder.folderKey = FolderItem.ROOT_KEY;
		folder.parent = null;
		folderContent = response.getJSONObject(FOLDERCONTENT);
		if (folderContent.has(FOLDERS)) {
			folders = folderContent.getJSONArray(FOLDERS);

			for (int i = 0; i < folders.length(); i++) {
				JSONObject f = (JSONObject) folders.get(i);
				Folder cFolder = new Folder();
				cFolder.name = getStringValue(f, NAME);
				cFolder.created = getStringValue(f, CREATED);
				cFolder.desc = getStringValue(f, DESC);
				cFolder.tags = getStringValue(f, TAGS);
				cFolder.filesCount = f.getInt(FILE_COUNT);
				cFolder.folderKey = getStringValue(f, FOLDERKEY);
				cFolder.isFolder = true;
				cFolder.parent = getStringValue(f, PARENT_FOLDERKEY, FolderItem.ROOT_KEY);
				cFolder.privacy = getStringValue(f, PRIVACY);
				Log.d("INSERT FOLDER IN ROOT", cFolder.descr());

				folder.subFolders.add(cFolder);
			}
		}
		if (folderContent.has(FILES)) {
			files = folderContent.getJSONArray(FILES);
			for (int i = 0; i < files.length(); i++) {
				JSONObject f = (JSONObject) files.get(i);
				Log.d(TAG, f.toString());
				File file = new File();
				file.created = getStringValue(f, CREATED);
				file.desc = getStringValue(f, DESC);
				file.downloads = f.getInt(DOWNLOADS);
				file.filename = getStringValue(f, FILENAME);
				file.fileType = file.getFileType();
				file.isFolder = false;
				file.parent = FolderItem.ROOT_KEY;
				file.privacy = getStringValue(f, PRIVACY);
				file.tags = getStringValue(f, TAGS);
				file.quickkey = getStringValue(f, QUICKKEY);
				file.size = f.getInt(SIZE);
				folder.files.add(file);
			}
		}

	}
}
