package gr.valor.mediafire.parser;

import gr.valor.mediafire.File;
import gr.valor.mediafire.Folder;
import gr.valor.mediafire.FolderItem;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

public class MyFilesParser extends MediafireXmlParser {
	private static final String TAG = "MyFilesParser";

	protected FolderItem readXml(XmlPullParser parser) throws XmlPullParserException, IOException {
		Log.d(TAG, "Reading XML");
		Folder root = new Folder();
		root.name = Folder.ROOT_NAME;
		root.folderKey = Folder.ROOT_KEY;
		root.parent = null;
		parser.require(XmlPullParser.START_TAG, ns, RESPONSE);
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the tag
			if (name.equals(MYFILES)) {
				readMyFiles(parser, root);
			} else {
				skip(parser);
			}
		}
		return root;
	}

	protected void readMyFiles(XmlPullParser parser, Folder root) throws XmlPullParserException, IOException {
		Log.d(TAG, "Reading my files");
		parser.require(XmlPullParser.START_TAG, ns, MYFILES);
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the tag
			if (name.equals(FOLDERS)) {
				readFolders(parser, root);
			} else if (name.equals(FILES)) {
				readFiles(parser, root);
			} else {
				skip(parser);
			}
		}

	}

	protected void readFolders(XmlPullParser parser, Folder root) throws XmlPullParserException, IOException {
		Log.d(TAG, "Reading folders");
		parser.require(XmlPullParser.START_TAG, ns, FOLDERS);
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the tag
			if (name.equals(FOLDER)) {
				readFolder(parser, root);
			} else {
				skip(parser);
			}
		}

	}

	protected void readFiles(XmlPullParser parser, Folder root) throws XmlPullParserException, IOException {
		Log.d(TAG, "Reading folders");
		parser.require(XmlPullParser.START_TAG, ns, FILES);
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the tag
			if (name.equals(FILE)) {
				readFile(parser, root);
			} else {
				skip(parser);
			}
		}

	}

	protected void readFolder(XmlPullParser parser, Folder root) throws XmlPullParserException, IOException {
		Log.d(TAG, "Reading folder");
		parser.require(XmlPullParser.START_TAG, ns, FOLDER);
		Folder folder = new Folder();
		folder.parent = root.folderKey;
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals(FOLDERKEY)) {
				folder.folderKey = readText(parser);
			} else if (name.equals(NAME)) {
				folder.name = readText(parser);
			} else if (name.equals(DESC)) {
				folder.desc = readText(parser);
			} else if (name.equals(TAGS)) {
				folder.tags = readText(parser);
			} else if (name.equals(CREATED)) {
				folder.created = readText(parser);
			} else if (name.equals(FOLDERS)) {
				readFolders(parser, folder);
			} else if (name.equals(FILES)) {
				readFiles(parser, folder);
			} else {
				skip(parser);
			}
		}
		root.subFolders.add(folder);
		Log.d(TAG, "Got folder " + root);

	}

	protected void readFile(XmlPullParser parser, Folder root) throws XmlPullParserException, IOException {
		Log.d(TAG, "Reading File");
		parser.require(XmlPullParser.START_TAG, ns, FILE);
		File file = new File();
		file.parent = root.folderKey;
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals(QUICKKEY)) {
				file.quickkey = readText(parser);
			} else if (name.equals(FILENAME)) {
				file.filename = readText(parser);
			} else if (name.equals(DESC)) {
				file.desc = readText(parser);
			} else if (name.equals(TAGS)) {
				file.tags = readText(parser);
			} else if (name.equals(CREATED)) {
				file.created = readText(parser);
			} else if (name.equals(DOWNLOADS)) {
				file.downloads = Integer.parseInt(readText(parser));
			} else if (name.equals(SIZE)) {
				file.size = Integer.parseInt(readText(parser));
			} else {
				skip(parser);
			}
		}
		root.files.add(file);
		Log.d(TAG, "Got file " + file);

	}

}
