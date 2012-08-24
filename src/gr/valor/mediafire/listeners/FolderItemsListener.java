package gr.valor.mediafire.listeners;

import gr.valor.mediafire.activities.FolderActivity;
import gr.valor.mediafire.database.FolderItemRecord;
import gr.valor.mediafire.database.FolderRecord;
import gr.valor.mediafire.database.Mediabase;

import java.util.Map;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class FolderItemsListener implements OnItemClickListener {
	public static final String TAG = "FolderItemsListener";
	private FolderActivity activity;

	public FolderItemsListener(FolderActivity activity) {
		this.activity = activity;
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		@SuppressWarnings("unchecked")
		Map<String, String> fi = (Map<String, String>) parent.getItemAtPosition(position);
		if (fi.get(FolderItemRecord.TYPE).equals(FolderItemRecord.TYPE_BACK)) {
			activity.onBackPressed();
		} else if (fi.get(FolderItemRecord.TYPE).equals(FolderItemRecord.TYPE_FOLDER)) {
			Mediabase mb = new Mediabase(activity);
			SQLiteDatabase db = mb.getReadableDatabase();
			try {
				FolderRecord newFolder = new FolderRecord(db, fi.get(FolderItemRecord.FOLDERKEY));
				activity.mediafire.setCurrentFolder(newFolder);
				activity.requestFolder();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				db.close();
			}
		} else if (fi.get(FolderItemRecord.TYPE).equals(FolderItemRecord.TYPE_FILE)) {
			String quickkey = fi.get(FolderItemRecord.QUICKKEY);
			Log.d(TAG, "Clicked on " + quickkey);
		}
	}
}
