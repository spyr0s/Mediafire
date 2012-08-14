package gr.valor.mediafire.helpers;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;

public class FileIcon {

	private String ext;

	private Activity activity;

	public static Map<String, String[]> types = new HashMap<String, String[]>();

	{
		types.put("image", new String[] { "jpg", "png", "gif", "bmp", "tiff" });
		types.put("archive", new String[] { "zip", "rar", "7z" });
		types.put("doc", new String[] { "doc" });
		types.put("pdf", new String[] { "pdf" });
		types.put("audio", new String[] { "mp3", "wav", "wma" });
		types.put("text", new String[] { "txt" });
		types.put("video", new String[] { "avi", "mkv", "mp4", "wmv", "mpg" });
	}

	public FileIcon(String ext, Activity activity) {
		this.ext = ext;
		this.activity = activity;
	}

	public int getIcon() {
		for (Map.Entry<String, String[]> icons : types.entrySet()) {
			String[] value = icons.getValue();
			for (int i = 0; i < value.length; i++) {
				if (ext.equals(value[i])) {
					String img = icons.getKey();
					return activity.getResources().getIdentifier(img, "drawable", "gr.valor.mediafire");
				}
			}
		}
		return 0;
	}
}
