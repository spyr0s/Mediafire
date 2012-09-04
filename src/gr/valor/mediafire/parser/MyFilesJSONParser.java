package gr.valor.mediafire.parser;

import gr.valor.mediafire.database.FileRecord;
import gr.valor.mediafire.database.FolderItemRecord;
import gr.valor.mediafire.database.FolderRecord;
import gr.valor.mediafire.helpers.MyLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyFilesJSONParser extends JSONParser implements Elements {
	public static final String TAG = "MyFilesJSONParser";
	public FolderRecord folder = new FolderRecord();
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
		try {
			checkAction(ACTION_GET_FOLDER_CONTENT);
		} catch (JSONException e) {
			MyLog.w(TAG, e.getMessage());
			throw e;
		}
		folder.name = FolderItemRecord.ROOT_NAME;
		folder.folderKey = FolderItemRecord.ROOT_KEY;
		folder.parent = null;
		folderContent = response.getJSONObject(FOLDERCONTENT);
		if (folderContent.has(FOLDERS)) {
			folders = folderContent.getJSONArray(FOLDERS);

			for (int i = 0; i < folders.length(); i++) {
				JSONObject f = (JSONObject) folders.get(i);
				JSONObject rev = f.getJSONObject(REVISION);
				MyLog.d("XML", f.toString());
				FolderRecord cFolder = new FolderRecord();
				cFolder.name = getStringValue(f, NAME);
				cFolder.setCreated(getStringValue(f, CREATED));
				cFolder.desc = getStringValue(f, DESC);
				cFolder.tags = getStringValue(f, TAGS);
				cFolder.flag = f.getInt(FLAG);
				cFolder.revision = rev.getInt(REVISION);
				cFolder.epoch = rev.getLong(EPOCH);
				cFolder.privacy = getStringValue(f, PRIVACY);
				cFolder.dropboxEnabled = getStringValue(f, DROPBOX_ENABLED);
				cFolder.shared = getStringValue(f, SHARED);
				cFolder.fileCount = f.getInt(FILE_COUNT);
				cFolder.folderCount = f.getInt(FOLDER_COUNT);
				cFolder.folderKey = getStringValue(f, FOLDERKEY);
				cFolder.isFolder = true;
				cFolder.parent = getStringValue(f, PARENT_FOLDERKEY, FolderItemRecord.ROOT_KEY);
				cFolder.privacy = getStringValue(f, PRIVACY);
				MyLog.d("INSERT FOLDER IN ROOT", cFolder.descr());

				folder.subFolders.add(cFolder);
			}
		}
		if (folderContent.has(FILES)) {
			files = folderContent.getJSONArray(FILES);
			for (int i = 0; i < files.length(); i++) {
				JSONObject f = (JSONObject) files.get(i);
				MyLog.d("XML", f.toString());
				FileRecord file = new FileRecord();
				file.setCreated(getStringValue(f, CREATED));
				file.desc = getStringValue(f, DESC);
				file.downloads = f.getInt(DOWNLOADS);
				file.filename = getStringValue(f, FILENAME);
				file.fileExtension = file.getFileExtension();
				file.fileType = getStringValue(f, FILETYPE);
				file.passwordProtected = getStringValue(f, PASSWORD_PROTECTED);
				file.flag = f.getInt(FLAG);
				file.privacy = getStringValue(f, PRIVACY);
				file.isFolder = false;
				file.parent = FolderItemRecord.ROOT_KEY;
				file.privacy = getStringValue(f, PRIVACY);
				file.tags = getStringValue(f, TAGS);
				file.quickkey = getStringValue(f, QUICKKEY);
				file.size = f.getInt(SIZE);
				folder.files.add(file);
			}
		}

	}
}
