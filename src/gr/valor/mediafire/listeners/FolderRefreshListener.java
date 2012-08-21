package gr.valor.mediafire.listeners;

import android.util.Log;
import eu.erikw.PullToRefreshListView.OnRefreshListener;
import gr.valor.mediafire.activities.FolderActivity;

public class FolderRefreshListener implements OnRefreshListener {
	public static final String TAG = "FolderRefreshListener";
	private FolderActivity activity;

	public FolderRefreshListener(FolderActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onRefresh() {
		activity.listView.postDelayed(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "Start Refreshing");
				activity.mediafire.setForceOnline(true);
				activity.requestFolder();
			}
		}, 1);
	}
}
