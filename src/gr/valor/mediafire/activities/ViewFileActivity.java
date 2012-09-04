package gr.valor.mediafire.activities;

import gr.valor.mediafire.R;
import gr.valor.mediafire.api.Connection;
import gr.valor.mediafire.database.FileRecord;
import gr.valor.mediafire.database.FolderItemRecord;
import gr.valor.mediafire.helpers.FileIcon;
import gr.valor.mediafire.helpers.MyLog;
import gr.valor.mediafire.tasks.GetFileLinkTask;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ViewFileActivity extends BaseActivity {

	public static final String FILE_QUICKKEY = "quickkey";
	private String quickkey;
	public FileRecord fileRecord;
	private TextView viewFilename;
	private ImageView viewIcon;
	private TextView viewCreated;
	private TextView viewDownloads;
	private TextView viewPrivacy;
	private TextView viewPasswordProtected;
	private TextView viewSize;
	private TextView viewDescription;
	private TextView viewTags;
	public long enqueue;
	public DownloadManager dm;
	private BroadcastReceiver receiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_file);
		quickkey = getIntent().getStringExtra(FILE_QUICKKEY);
		viewFilename = (TextView) findViewById(R.id.file_view_name);
		viewIcon = (ImageView) findViewById(R.id.file_view_icon);
		viewCreated = (TextView) findViewById(R.id.file_view_created);
		viewDownloads = (TextView) findViewById(R.id.file_view_downloads);
		viewPrivacy = (TextView) findViewById(R.id.file_view_privacy);
		viewPasswordProtected = (TextView) findViewById(R.id.file_view_passwordProtected);
		viewSize = (TextView) findViewById(R.id.file_view_size);
		viewDescription = (TextView) findViewById(R.id.file_view_description);
		viewTags = (TextView) findViewById(R.id.file_view_tags);

		createView();
	}

	private void createView() {
		try {
			fileRecord = new FileRecord(this.quickkey);
			setTitle("File - " + fileRecord.filename);
			viewFilename.setText(fileRecord.filename);
			setIcon();
			viewCreated.setText(fileRecord.getCreated());
			viewDownloads.setText(String.valueOf(fileRecord.downloads));
			viewPrivacy.setText(fileRecord.privacy);
			viewPasswordProtected.setText(fileRecord.passwordProtected);
			viewSize.setText(fileRecord.getSize());
			viewDescription.setText(fileRecord.desc);
			viewTags.setText(fileRecord.tags);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public TextView getViewDownloads() {
		return viewDownloads;
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

	public void downloadFile(View view) {
		MyLog.d(TAG, "Downloading file " + fileRecord.filename);
		if (!mediafire.isOnline()) {
			Toast.makeText(this, "There is no internet connection", Toast.LENGTH_SHORT).show();
		} else {
			Connection connection = new Connection(this);
			GetFileLinkTask download = new GetFileLinkTask(this, connection);
			download.execute(fileRecord.quickkey);
		}

	}
}
