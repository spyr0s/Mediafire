package gr.valor.mediafire.activities;

import gr.valor.mediafire.R;
import gr.valor.mediafire.database.FileRecord;
import gr.valor.mediafire.database.FolderItemRecord;
import gr.valor.mediafire.database.Mediabase;
import gr.valor.mediafire.helpers.FileIcon;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewFileActivity extends BaseActivity {

	public static final String FILE_QUICKKEY = "quickkey";
	private String quickkey;
	private FileRecord fileRecord;
	private TextView viewFilename;
	private ImageView viewIcon;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_file);
		quickkey = getIntent().getStringExtra(FILE_QUICKKEY);
		viewFilename = (TextView) findViewById(R.id.file_view_name);
		viewIcon = (ImageView) findViewById(R.id.file_view_icon);
		createView();
	}

	private void createView() {
		Mediabase m = new Mediabase(this);
		SQLiteDatabase db = m.getReadableDatabase();
		try {
			fileRecord = new FileRecord(db, this.quickkey);
			viewFilename.setText(fileRecord.filename);
			setIcon();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void setIcon() {
		Resources res = getResources();
		Drawable d = null;
		if (fileRecord.fileExtension.equals(FolderItemRecord.TYPE_FILE)) {

		} else {
			String img = fileRecord.fileExtension;
			img = img.replaceAll(" ", "").toLowerCase();
			FileIcon icon = new FileIcon(img, this);
			int r = icon.getIcon();
			if (r == 0) {
				d = res.getDrawable(R.drawable.icon_file);
				viewIcon.setImageDrawable(d);
			} else {
				viewIcon.setImageResource(r);
			}
		}

	}
}
