package gr.valor.mediafire;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class FolderItem implements ItemConstants {
	public static final String ROOT_NAME = "root";
	public static final String ROOT_KEY = "rootkey";

	public String parent = null;
	public boolean isFolder;
	public String desc;
	public String tags;
	public String created;
	public long inserted = 0L;
	public int flag;
	public String privacy;
	public String itemType;
	public ArrayList<Folder> subFolders = new ArrayList<Folder>();
	public ArrayList<File> files = new ArrayList<File>();
	List<Map<String, String>> folderItems = new ArrayList<Map<String, String>>();
	List<Map<String, String>> fileItems = new ArrayList<Map<String, String>>();

}
