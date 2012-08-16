package gr.valor.mediafire.parser;

public interface Elements {
	// ACTIONS
	String ACTION = "action";
	String LOGIN_TOKEN = "login_token";
	String SESSION_TOKEN = "session_token";
	String GET_SESSION_TOKEN_ACTION = "user/get_session_token";
	String GET_FOLDER_CONTENT_ACTION = "folder/get_content";
	String SUCCESS = "Success";
	String RESULT = "result";
	String RESPONSE = "response";
	String MYFILES = "myfiles";

	// FOLDER CONTENT
	String FOLDERCONTENT = "folder_content";
	String FOLDERS = "folders";
	String FILE_COUNT = "file_count";
	String FOLDER_COUNT = "folder_count";
	String PARENT_FOLDERKEY = "parent_folderkey";
	String PRIVACY = "privacy";
	String FOLDER = "folder";
	String FLAG = "flag";
	String SHARED = "shared";
	String REVISION = "revision";
	String EPOCH = "epoch";
	String DROPBOX_ENABLED = "dropbox_enabled";
	String FILETYPE = "filetype";
	String PASSWORD_PROTECTED = "password_protected";
	String FOLDERKEY = "folderkey";
	String NAME = "name";
	String DESC = "description";
	String TAGS = "tags";
	String CREATED = "created";
	String FILES = "files";
	String FILE = "file";
	String QUICKKEY = "quickkey";
	String FILENAME = "filename";
	String DOWNLOADS = "downloads";
	String SIZE = "size";
}
