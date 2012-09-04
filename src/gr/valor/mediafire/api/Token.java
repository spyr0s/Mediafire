package gr.valor.mediafire.api;

import gr.valor.mediafire.helpers.MyLog;
import gr.valor.mediafire.parser.Elements;

import java.io.IOException;

import android.content.Context;

public abstract class Token implements Elements, ApiUrls {
	public static String TAG;
	protected String email;
	protected String password;
	protected Context context;
	protected Connection connection;
	public String token = null;

	public Token(String email, String password, Context context, Connection connection) {
		setEmail(email);
		setPassword(password);
		setContext(context);
		setConnection(connection);
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

	public void run(Object... params) throws IOException {
		MyLog.d(TAG, "Getting the token in background");
		getToken();
	}

	protected abstract void getToken() throws IOException;

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}
}
