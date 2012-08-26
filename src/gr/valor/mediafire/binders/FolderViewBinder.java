package gr.valor.mediafire.binders;

import gr.valor.mediafire.ItemConstants;
import gr.valor.mediafire.R;
import gr.valor.mediafire.activities.FolderActivity;
import gr.valor.mediafire.database.FolderItemRecord;
import gr.valor.mediafire.helpers.FileIcon;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

public class FolderViewBinder implements SimpleAdapter.ViewBinder {

	private FolderActivity activity;

	public FolderViewBinder(FolderActivity activity) {
		this.activity = activity;
	}

	public boolean setViewValue(View view, Object data, String textRepresentation) {
		if (view.getId() == R.id.folder_item_icon) {
			ImageView im = (ImageView) view;
			Resources res = activity.getResources();
			Drawable d = null;
			if (textRepresentation.equals(FolderItemRecord.TYPE_FOLDER) || textRepresentation.equals(FolderItemRecord.TYPE_BACK)) {
				d = res.getDrawable(R.drawable.icon_folder);
				im.setImageDrawable(d);
			} else if (textRepresentation.equals(FolderItemRecord.TYPE_FILE)) {
				d = res.getDrawable(R.drawable.icon_file);
				im.setImageDrawable(d);
			} else {
				String img = textRepresentation;
				img = img.replaceAll(" ", "").toLowerCase();
				FileIcon icon = new FileIcon(img, activity);
				int r = icon.getIcon();
				if (r == 0) {
					d = res.getDrawable(R.drawable.icon_file);
					im.setImageDrawable(d);
				} else {
					((ImageView) view).setImageResource(r);
				}
			}

			return true;
		} else if (view.getId() == R.id.folder_item_privacy) {
			String img = "privacy_" + textRepresentation;
			img = img.replaceAll(" ", "").toLowerCase();
			FileIcon icon = new FileIcon(img, activity);
			int r = icon.getIcon();
			if (r == 0) {
				((ImageView) view).setVisibility(View.INVISIBLE);
			} else {
				((ImageView) view).setImageResource(r);
			}
			return true;
		} else if (view.getId() == R.id.folder_item_downicon) {
			if (textRepresentation.equals(ItemConstants.NO)) {
				view.setVisibility(View.INVISIBLE);
			} else if (textRepresentation.equals(ItemConstants.YES)) {
				view.setVisibility(View.VISIBLE);
			}
			return true;
		}
		return false;
	}

}
