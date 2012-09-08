package gr.valor.mediafire.tasks;

import gr.valor.mediafire.Mediafire;
import gr.valor.mediafire.api.ApiUrls;
import gr.valor.mediafire.api.Connection;
import gr.valor.mediafire.helpers.MyLog;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

public abstract class MediafireTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> implements ApiUrls {

	protected static String TAG;
	protected Connection connection;
	protected ArrayList<String> attributes;
	protected ProgressDialog d;
	protected boolean notConnected;
	protected Activity activity;
	protected Mediafire mediafire;

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		this.d.setMessage("Authenticating...");
		this.d.show();
	}

	@Override
	protected void onPostExecute(Result res) {
		super.onPostExecute(res);
		this.d.dismiss();
		if (notConnected) {
			MyLog.d(TAG, "Could not connect");
			Toast.makeText(activity, "Could not connect to Mediafire", Toast.LENGTH_LONG).show();
			this.cancel(true);
		}

	}

}
