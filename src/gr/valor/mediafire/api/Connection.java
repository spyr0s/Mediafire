package gr.valor.mediafire.api;

import gr.valor.mediafire.Helper;
import gr.valor.mediafire.R;
import gr.valor.mediafire.parser.Elements;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

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

	public InputStream connect(String stUrl, String[] params) throws IOException {
		stUrl += "?" + Helper.implode(params, "&");
		Log.d(TAG, "Connecting to url:" + stUrl);
		InputStream is = null;

		KeyStore trustStore = null;
		try {
			trustStore = KeyStore.getInstance("BKS");
		} catch (KeyStoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		InputStream instream = context.getResources().openRawResource(R.raw.mediafire);
		try {
			trustStore.load(instream, "medspy21".toCharArray());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				instream.close();
			} catch (Exception ignore) {
			}
		}

		// Create socket factory with given keystore.
		try {
			SSLSocketFactory socketFactory = new SSLSocketFactory(trustStore);
		} catch (KeyManagementException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnrecoverableKeyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (KeyStoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		SSLSocketFactory sockFac = null;
		try {
			sockFac = new SSLSocketFactory(trustStore);
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Scheme sch = new Scheme("https", sockFac, 443);
		HttpClient httpclient = wrapClient(new DefaultHttpClient());
		// httpclient.getConnectionManager().getSchemeRegistry().register(sch);

		// HttpClient cl = getNewHttpClient();
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

	public HttpClient getNewHttpClient() {
		try {
			InputStream in = null;
			// Load default system keystore
			KeyStore trusted = KeyStore.getInstance(KeyStore.getDefaultType());
			try {
				in = new BufferedInputStream(new FileInputStream(System.getProperty("javax.net.ssl.trustStore"))); // Normally:
																													// "/system/etc/security/cacerts.bks"
				trusted.load(in, null); // no password is "changeit"
			} finally {
				if (in != null) {
					in.close();
					in = null;
				}
			}

			// Load application keystore & merge with system
			try {
				KeyStore appTrusted = KeyStore.getInstance("BKS");
				in = context.getResources().openRawResource(R.raw.mediafire);
				appTrusted.load(in, context.getString(R.string.store_password).toCharArray());
				for (Enumeration<String> e = appTrusted.aliases(); e.hasMoreElements();) {
					final String alias = e.nextElement();
					final KeyStore.Entry entry = appTrusted.getEntry(alias, null);
					trusted.setEntry(System.currentTimeMillis() + ":" + alias, entry, null);
				}
			} finally {
				if (in != null) {
					in.close();
					in = null;
				}
			}

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SSLSocketFactory sf = new SSLSocketFactory(trusted);
			sf.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			return new DefaultHttpClient();
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
