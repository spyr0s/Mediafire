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
}
