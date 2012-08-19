package gr.valor.mediafire.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;
import gr.valor.mediafire.Folder;
import gr.valor.mediafire.Helper;
import gr.valor.mediafire.LoginActivity;
import gr.valor.mediafire.R;
import gr.valor.mediafire.parser.Elements;
import gr.valor.mediafire.parser.LoginTokenParser;
import gr.valor.mediafire.parser.MyFilesParser;


public class MyOnlineFiles extends MyFiles implements ApiUrls, Elements{
	public static final String TAG = "MyOnlineFiles";
	public String content_filter;
	public static final String ALL = "all";
	public static final String FILES = "files";
	public static final String FOLDERS = "folders";
	
	public String content_format;
	public static final String TREE = "tree";
	public static final String LIST = "list";
	
	public String recursive;
	public static final String YES = "yes";
	public static final String NO = "no";
	
	public int start;
	public int limit;
	
	protected Context context;
	protected Connection connection;
	private String sessionToken;
	private SQLiteDatabase db;
	
	public MyOnlineFiles(Context context, Connection connection, String sessionToken){
		this.connection = connection;
		this.context = context;
		this.sessionToken = sessionToken;
	}
	
	public Folder getFiles(String order) throws Exception{
		Log.d(TAG, "Connecting...");
		InputStream in = connection.connect(DOMAIN + "/" + MYFILES_URL,
				new String[] { "session-token=" + sessionToken });

		if (in == null) {
			Log.e(TAG, "Could not read from " + GET_LOGIN_TOKEN_URL);
		}
		try {
			Log.d(TAG, "Starting my files parser");
			MyFilesParser p = new MyFilesParser();
			Folder f = (Folder) p.parse(in);
			return f;
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			in.close();
		}
		return null;
	}
	
	
	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}


}
