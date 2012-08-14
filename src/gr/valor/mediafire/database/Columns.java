package gr.valor.mediafire.database;

import android.provider.BaseColumns;

public class Columns {

	public static final class Items implements BaseColumns {
		public static final String KEY = "key";
		public static final String TYPE = "type";
		public static final String PARENT = "parent";
		public static final String NAME = "name";
		public static final String DESC = "desc";
		public static final String TAGS = "tags";
		public static final String CREATED = "created";
		public static final String INSERTED = "inserted";

		private Items() {
		}

	}

	public static final class Folders implements BaseColumns {
		public static final String FOLDERKEY = "folderkey";
		public static final String FOLDERS = "folders";
		public static final String FILES = "files";

		private Folders() {
		}

	}

	public static final class Files implements BaseColumns {
		public static final String QUICKKEY = "quickkey";
		public static final String DOWNLOADS = "downloads";
		public static final String SIZE = "size";

		private Files() {
		}

	}

	public static final class Notes implements BaseColumns {
		public static final String QUICKKEY = "quickkey";
		public static final String SUBJECT = "subject";
		public static final String DESCRIPTION = "description";

		private Notes() {
		}

	}

	public static final class Revisions implements BaseColumns {
		public static final String REVISION = "revision";
		public static final String EPOCH = "epoch";

		private Revisions() {
		}

	}

}