package gr.valor.mediafire.receivers;

import gr.valor.mediafire.R;
import gr.valor.mediafire.helpers.MyLog;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

public class DownloadBroadcastReceiver extends BroadcastReceiver {
	public static final String TAG = "DownloadBroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
			long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
			Query query = new Query();
			query.setFilterById(downloadId);
			DownloadManager dm = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
			Cursor c = dm.query(query);
			if (c.moveToFirst()) {
				int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
				if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
					String localUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
					int id = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_ID));
					String filename = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE)).replaceFirst("Downloading", "").trim();
					MyLog.d(TAG, "Download completed");
					String ns = Context.NOTIFICATION_SERVICE;
					NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
					int icon = R.drawable.icon_file;
					CharSequence tickerText = "Download complete";
					long when = System.currentTimeMillis();
					Notification notification = new Notification(icon, tickerText, when);
					notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
					CharSequence contentTitle = "Download of " + filename + "  Completed";
					CharSequence contentText = "The download of " + filename + " is completed";
					Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(localUri));
					PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
					notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
					mNotificationManager.notify(id, notification);

				}
			}
		}
	}
}
