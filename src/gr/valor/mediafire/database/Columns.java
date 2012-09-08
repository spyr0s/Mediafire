package gr.valor.mediafire.database;

import android.provider.BaseColumns;

public class Columns {
	public static final String YES = "yes";
	public static final String NO = "no";
	public static final String PUBLIC = "public";
	public static final String PRIVATE = "private";

	public static final class Items implements BaseColumns {
		public static final String ACCOUNT_EMAIL = "account_email";
		public static final String KEY = "key";
		public static final String TYPE = "type";
		public static final String PARENT = "parent";
		public static final String NAME = "name";
		public static final String DESC = "desc";
		public static final String TAGS = "tags";
		public static final String CREATED = "created";
		public static final String INSERTED = "inserted";
		public static final String FLAG = "flag";
		public static final String PRIVACY = "privacy";

		private Items() {
		}

	}

	public static final class Folders implements BaseColumns {
		public static final String FOLDERKEY = "folderkey";
		public static final String FOLDERS = "folders";
		public static final String FILES = "files";
		public static final String SHARED = "shared";
		public static final String REVISION = "revision";
		public static final String EPOCH = "epoch";
		public static final String DROPBOX_ENABLED = "dropbox_enabled";

		private Folders() {
		}

	}

	public static final class Files implements BaseColumns {
		public static final String QUICKKEY = "quickkey";
		public static final String DOWNLOADS = "downloads";
		public static final String SIZE = "size";
		public static final String FILETYPE = "filetype";
		public static final String PASSWORD_PROTECTED = "password_protected";

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