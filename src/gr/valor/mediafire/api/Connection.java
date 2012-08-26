package gr.valor.mediafire.api;

import gr.valor.mediafire.helpers.Helper;
import gr.valor.mediafire.parser.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.util.Log;

public class Connection extends AsyncTask<Object, String, String> implements ApiUrls, Elements {
	private static final String TAG = "Connect";
	public static final int ANY = 0;
	public static final int WIFI = 1;
	public static final int GSM = 2;
	public Context context;

	public Connection(Context context) {
		this.context = context;
	}

	public InputStream connect(String stUrl, ArrayList<String> params) throws IOException {
		stUrl += "?" + Helper.implode(params, "&");
		Log.d(TAG, "Connecting to url:" + stUrl);
		InputStream is = null;
		HttpClient httpclient = wrapClient(new DefaultHttpClient());

		HttpGet http = new HttpGet(stUrl);
		Log.d(TAG, http.getURI().toString());
		HttpResponse response = httpclient.execute(http);
		Log.d(TAG, "The response is: " + response.getAllHeaders());
		is = response.getEntity().getContent();

		return is;
	}

	public static HttpClient wrapClient(HttpClient base) {
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			MySSLSocketFactory ssf = new MySSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = base.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", ssf, 443));
			return new DefaultHttpClient(ccm, base.getParams());
		} catch (Exception ex) {
			return null;
		}
	}

	public static boolean isActive(Context c, int type) {
		ConnectivityManager connMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		State mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		State wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();

		switch (type) {
		case ANY:
			return wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING || mobile == NetworkInfo.State.CONNECTED
					|| mobile == NetworkInfo.State.CONNECTING;
		case WIFI:
			return wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING;
		case GSM:
			return mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING;
		}
		return false;
	}

	public boolean isActive() {
		Log.d(TAG, "Checking connection");
		if (!ApiUrls.DOMAIN.equals("https://www.mediafire.com/api")) {
			Log.d(TAG, "Debug mode");
			return true;
		}
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			Log.d(TAG, "Connection is OK");
			return true;
		} else {
			Log.d(TAG, "No Connection");
			return false;
		}

	}

	@Override
	protected String doInBackground(Object... params) {

		return null;
	}

	@Override
	protected void onPostExecute(String result) {

	}

}
