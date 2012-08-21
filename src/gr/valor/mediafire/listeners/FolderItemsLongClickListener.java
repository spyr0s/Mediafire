package gr.valor.mediafire.listeners;

import gr.valor.mediafire.activities.FolderActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

public class FolderItemsLongClickListener implements OnItemLongClickListener {

	private FolderActivity activity;

	public FolderItemsLongClickListener(FolderActivity activity) {
		this.activity = activity;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		return false;
	}

}
