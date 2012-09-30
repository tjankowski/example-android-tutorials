package my.todolist;

import java.util.Date;

import my.todolist.model.ToDoItem;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ToDoDBAdapter {

	public static final String KEY_ID = "id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_TEXT = "todo_text";
	public static final String KEY_DATE = "todo_date";

	private static final String DATABASE_NAME = "todo_list.db";
	private static final String DATABASE_TABLE = "toDo_items";
	private static final int DATABASE_VERSION = 1;

	private SQLiteDatabase database;
	private final Context context;
	private ToDoDBOpenHelper toDoDBOpenHelper;

	public ToDoDBAdapter(Context context) {
		this.context = context;
		toDoDBOpenHelper = new ToDoDBOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public void open() throws SQLiteException {
		try {
			database = toDoDBOpenHelper.getWritableDatabase();
		} catch (SQLiteException exception) {
			database = toDoDBOpenHelper.getReadableDatabase();
		}
	}
	
	public long insertToDoItem(ToDoItem item) {
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_TITLE, item.getTitle());
		newValues.put(KEY_DATE, item.getDate().getTime());
		newValues.put(KEY_TEXT, item.getText());
		
		return database.insert(DATABASE_TABLE, null, newValues);
	}
	
	public boolean removeToDoItem(long rowId) {
		return database.delete(DATABASE_TABLE, KEY_ID + "=" + rowId, null) > 0;
	}
	
	public boolean updateToDoItem(long rowId, ToDoItem item) {
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_TITLE, item.getTitle());
		newValues.put(KEY_DATE, item.getDate().getTime());
		newValues.put(KEY_TEXT, item.getText());
		
		return database.update(DATABASE_TABLE, newValues, KEY_ID + "=" + rowId, null) > 0;
	}
	
	public Cursor getAllToDoItemsCursor() {
		return database.query(DATABASE_TABLE, new String[]{KEY_ID, KEY_TITLE, KEY_DATE, KEY_TEXT}, null, null, null, null, null);
	}
	
	public Cursor setCursorToToDoItem(long rowId) throws SQLException {
		Cursor cursor = database.query(DATABASE_TABLE, new String[]{KEY_ID, KEY_TITLE, KEY_DATE, KEY_TEXT}, KEY_ID + "=" + rowId, null, null, null, null);
		
		if((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			throw new SQLException("No to do item found for row: " + rowId);
		}
		
		return cursor;
	}
	
	public ToDoItem getToDoItem(long rowId) throws SQLException {
		Cursor cursor = database.query(DATABASE_TABLE, new String[]{KEY_ID, KEY_TITLE, KEY_DATE, KEY_TEXT}, KEY_ID + "=" + rowId, null, null, null, null);
		
		if((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			throw new SQLException("No to do item found for row: " + rowId);
		}
		
		String title = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
		long date = cursor.getLong(cursor.getColumnIndex(KEY_DATE));
		String text = cursor.getString(cursor.getColumnIndex(KEY_TEXT));
		
		ToDoItem result = new ToDoItem(title, new Date(date), text);
		
		return result;
	}
	public void close() {
		database.close();
	}

	private static class ToDoDBOpenHelper extends SQLiteOpenHelper {

		public ToDoDBOpenHelper(Context context, String databaseName,
				CursorFactory factory, int version) {
			super(context, databaseName, factory, version);
		}

		private static final String SQL_DATABASE_CREATE = "CREATE TABLE "
				+ DATABASE_TABLE + " (" + KEY_ID
				+ " integer primary key autoincrement, " + KEY_TITLE
				+ " text not null, " + KEY_DATE + " long, " + KEY_TEXT
				+ " text);";
		
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
