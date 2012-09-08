package gr.valor.mediafire.api;

public interface ApiUrls {
	String APP_ID = "7144";
	String API_KEY = "hys4xtc99lb5ehk4gybryvl7cmmtum9dw60pyc9g";
	String DOMAIN = "https://www.mediafire.com/api";
	// String DOMAIN = "http://10.0.2.2/mediafire/api";
	// String DOMAIN = "http://192.168.42.93/mediafire/api";
	String GET_LOGIN_TOKEN_URL = "user/get_login_token.php";
	String GET_SESSION_TOKEN_URL = "user/get_session_token.php";
	String MYFILES_URL = "folder/get_content.php";
	String MYFILES_REVISION_URL = "user/myfiles-revision.php";
	String RENEW_SESSION_TOKEN_URL = "user/renew_session_token.php";
	String GET_LINKS_URL = "file/get_links.php";
	String UPDATE_FILE_URL = "file/update.php";
	String UPDATE_FOLDER_URL = "folder/update.php";
	String DELETE_FILE_URL = "file/delete.php";
	String CREATE_FOLDER_URL = "folder/create.php";

	// PARAMETERS
	String SESSION_TOKEN = "session_token";
	String RESPONSE_FORMAT = "response_format";
	String JSON = "json";
	String QUICKKEY = "quick_key";
	String LINK_TYPE = "link_type";
	String NORMAL_DOWNLOAD = "normal_download";
	String DIRECT_DOWNLOAD = "direct_download";
	String EMAIL = "email";
	String PASSWORD = "password";
	String APPLICATION_ID = "application_id";
	String SIGNATURE = "signature";
	String FOLDER_KEY = "folder_key";
	String CONTENT_TYPE = "content_type";
	String FOLDERS = "folders";
	String FILES = "files";
	String PRIVACY = "privacy";
	String PUBLIC = "public";
	String PRIVATE = "private";
	String PARENT_KEY = "parent_key";
	String FOLDER_NAME = "foldername";
}
