package my.todolist;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ToDoProvider extends ContentProvider {
	
	public static final String KEY_ID = "id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_TEXT = "todo_text";
	public static final String KEY_DATE = "todo_date";
	
	public static final int ID_COLUMN = 1;
	public static final int TITLE_COLUMN = 2;
	public static final int DATE_COLUMN = 3;
	public static final int TEXT_COLUMN = 4;
	
	
	public static final Uri CONTENT_URI = Uri
	.parse("content://my.todolist.provider.todo/todolist");

	private static final String DATABASE_NAME = "todo_list.db";
	private static final String DATABASE_TABLE = "toDo_items";
	private static final int DATABASE_VERSION = 1;
	
	private static final int LIST = 1;
	private static final int ITEM = 2;
	
	private static final UriMatcher uriMatcher;

	private SQLiteDatabase database;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("my.todolist.provider.todo", "todolist", LIST);
		uriMatcher.addURI("my.todolist.provider.todo", "todolist/#", ITEM);
	}

	@Override
	public boolean onCreate() {
		Context context = getContext();
		
		ToDoDBOpenHelper dbHelper = new ToDoDBOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
		database = dbHelper.getWritableDatabase();
		return (database == null)? false : true;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		 case LIST:
			return "vnd.android.cursor.dir/vnd.todolist.todo";
		case ITEM:
			return "vnd.android.cursor.item/vnd.todolist.todo";
		default:
			throw new IllegalArgumentException("Unsuported URI: " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DATABASE_TABLE);
		
		switch (uriMatcher.match(uri)) {
			case ITEM:
				builder.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
				break;
			default:
				break;
		}
		
		String orderBy;
		if(TextUtils.isEmpty(sortOrder)) {
			orderBy = KEY_DATE;
		} else {
			orderBy = sortOrder;
		}
		
		Cursor c = builder.query(database, projection, selection, selectionArgs, null, null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		
		return c;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowID = database.insert(DATABASE_TABLE, null, values);
		if(rowID > 0) {
			Uri returnUri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			return returnUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count;
		switch (uriMatcher.match(uri)) {
			case LIST:
				count = database.delete(DATABASE_TABLE, selection, selectionArgs);
				break;
			case ITEM:
				String segment = uri.getPathSegments().get(1);
				count = database.delete(DATABASE_TABLE, KEY_ID + "=" + segment + (!(TextUtils.isEmpty(selection)) ? "AND (" + selection + ")" : ""), selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri );
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count;
		switch (uriMatcher.match(uri)) {
		case LIST:
			count = database.update(DATABASE_TABLE, values, selection, selectionArgs);
			break;
		case ITEM:
			String segment = uri.getPathSegments().get(1);
			count = database.update(DATABASE_TABLE, values, KEY_ID + "=" + segment + (!(TextUtils.isEmpty(selection)) ? "AND (" + selection + ")" : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri );
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	private static class ToDoDBOpenHelper extends SQLiteOpenHelper {

		public ToDoDBOpenHelper(Context context, String databaseName,
				CursorFactory factory, int version) {
			super(context, databaseName, factory, version);
		}

		private static final String SQL_DATABASE_CREATE = "CREATE TABLE "
				+ DATABASE_TABLE + " (" 
				+ KEY_ID + " integer primary key autoincrement, " 
				+ KEY_TITLE + " text not null, "
				+ KEY_DATE + " long, "
				+ KEY_TEXT+ " text);";
		
		private static final String SQL_DATABASE_DROP = "DROP TABLE IF EXISTS " + DATABASE_TABLE;

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(ToDoDBAdapter.class.getName(), "Upgrading from version "
					+ oldVersion + " to " + newVersion);
			
			db.execSQL(SQL_DATABASE_DROP);
			onCreate(db);
		}
		
	}

}
