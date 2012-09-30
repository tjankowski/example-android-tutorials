package my.todolist;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import my.todolist.model.ToDoItem;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ToDoList extends Activity {

	private static final int MENU_ITEM_ADD_NEW_TODO = Menu.FIRST;
	private static final int MENU_ITEM_REMOVE_TODO = MENU_ITEM_ADD_NEW_TODO + 1;
	private static final int MENU_ITEM_EDIT_TODO = MENU_ITEM_ADD_NEW_TODO + 2;
	
	private static final String PREFERENCE_KEY_TEXT_ENTRY = "PREFERENCE_KEY_TEXT_ENTRY";
	private static final String PREFERENCE_KEY_ADDING_ITEM = "PREFERENCE_KEY_ADDING_ITEM";
	private static final String PREFERENCE_KEY_SELECTED_INDEX = "PREFERENCE_KEY_SELECTED_INDEX";
	
	private static final int CONFIRM_REMOVE_DIALOG = 0;
	
	private static final int ACTIVITY_EDIT_TODO_ITEM = 1;

	private EditText myEditText;
	private ListView myListView;
	private Button saveButton;
	private List<ToDoItem> todoItems;
	private ArrayAdapter<ToDoItem> todoItemsArrayAdapter;
	private boolean addingNew = false;
	private int selectedItem = -1;
	private Cursor toDoListCursor;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		myEditText = (EditText) findViewById(R.id.myEditText);
		myListView = (ListView) findViewById(R.id.myListView);
		saveButton = (Button) findViewById(R.id.saveButton);

		todoItems = new ArrayList<ToDoItem>();
		todoItemsArrayAdapter = new ArrayAdapter<ToDoItem>(this,
				R.layout.todolist_item, todoItems);

		myListView.setAdapter(todoItemsArrayAdapter);

		myEditText.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
						ToDoItem item = new ToDoItem(myEditText.getText().toString());
						addNewItem(item);
						updateArray();
						myEditText.setText("");
						cancelAdd();
						return true;
					}
				}
				return false;
			}
		});
		
		saveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ToDoItem item = new ToDoItem(myEditText.getText().toString());
				addNewItem(item);
				updateArray();
				myEditText.setText("");
				cancelAdd();
			}
			
			
		});

		registerForContextMenu(myListView);
		restoreUIState();
		
		populateToDoList();
	}
	
	private void populateToDoList() {
		ContentResolver contentResolver = getContentResolver();
		toDoListCursor = contentResolver.query(ToDoProvider.CONTENT_URI, null, null, null, null);
		startManagingCursor(toDoListCursor);
		
		updateArray();
	}
	
	private void updateArray() {
		toDoListCursor.requery();
		todoItems.clear();
		
		if(toDoListCursor.moveToFirst()) {
			do {
			long id = toDoListCursor.getLong(toDoListCursor.getColumnIndex(ToDoProvider.KEY_ID));
			String title = toDoListCursor.getString(toDoListCursor.getColumnIndex(ToDoProvider.KEY_TITLE));
			long date = toDoListCursor.getLong(toDoListCursor.getColumnIndex(ToDoProvider.KEY_DATE));
			String text = toDoListCursor.getString(toDoListCursor.getColumnIndex(ToDoProvider.KEY_TEXT));
			ToDoItem item = new ToDoItem(id, title, new Date(date), text);
			todoItems.add(0, item);
			} while (toDoListCursor.moveToNext());
		}
		todoItemsArrayAdapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		SharedPreferences uiState = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = uiState.edit();
		
		editor.putString(PREFERENCE_KEY_TEXT_ENTRY, myEditText.getText().toString());
		editor.putBoolean(PREFERENCE_KEY_ADDING_ITEM, addingNew);
		
		editor.commit();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(PREFERENCE_KEY_SELECTED_INDEX, selectedItem);
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		int position = -1;
		if(savedInstanceState != null) {
			position = savedInstanceState.getInt(PREFERENCE_KEY_SELECTED_INDEX, -1);
		}
		myListView.setSelection(position);
	}
	
	private void restoreUIState() {
		
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		
		String text = settings.getString(PREFERENCE_KEY_TEXT_ENTRY, "");
		Boolean adding = settings.getBoolean(PREFERENCE_KEY_ADDING_ITEM, false);
		
		if(adding) {
			addNewItem();
			myEditText.setText(text);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem addMenuItem = menu.add(0, MENU_ITEM_ADD_NEW_TODO, Menu.NONE,
				R.string.add_new_item);
		MenuItem removeMenuItem = menu.add(0, MENU_ITEM_REMOVE_TODO, Menu.NONE,
				R.string.remove_item);

		addMenuItem.setIcon(R.drawable.add);
		removeMenuItem.setIcon(R.drawable.delete);

		addMenuItem.setShortcut('1','a');
		removeMenuItem.setShortcut('2', 'r');

		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		menu.setHeaderTitle(R.string.list_view_item_context_menu_title);
		menu.add(0, MENU_ITEM_REMOVE_TODO, Menu.NONE, R.string.remove_item);
		menu.add(0, MENU_ITEM_EDIT_TODO, Menu.NONE, R.string.edit_item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		int idx = myListView.getSelectedItemPosition();

		String removeTitle = getString(addingNew ? R.string.cancel
				: R.string.remove_item);
		MenuItem removeMenuItem = menu.findItem(MENU_ITEM_REMOVE_TODO);
		
		removeMenuItem.setTitle(removeTitle);
		removeMenuItem.setVisible(addingNew || idx > -1);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		selectedItem = myListView.getSelectedItemPosition();
		
		switch (item.getItemId()) {
			case (MENU_ITEM_REMOVE_TODO): {
				if (addingNew) {
					cancelAdd();
				} else {
					showDialog(CONFIRM_REMOVE_DIALOG);
				}
				return true;
			}
			case (MENU_ITEM_ADD_NEW_TODO): {
				addNewItem();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		selectedItem = menuInfo.position;
		switch (item.getItemId()) {
			case (MENU_ITEM_REMOVE_TODO): {
				showDialog(CONFIRM_REMOVE_DIALOG);
				return true;
			}
			case (MENU_ITEM_EDIT_TODO): {
				ToDoItem toDoItem = todoItems.get(selectedItem);
				Intent i = new Intent(this, ToDoItemEdit.class);
				i.putExtra(ToDoProvider.KEY_ID, toDoItem.getId());
				i.putExtra(ToDoProvider.KEY_TITLE, toDoItem.getTitle());
				i.putExtra(ToDoProvider.KEY_DATE, toDoItem.getDate().getTime());
				i.putExtra(ToDoProvider.KEY_TEXT, toDoItem.getText());
				startActivityForResult(i, ACTIVITY_EDIT_TODO_ITEM);
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case ACTIVITY_EDIT_TODO_ITEM:
			if(resultCode == RESULT_OK) {
				long id = data.getLongExtra(ToDoProvider.KEY_ID, -1l);
				String title = data.getStringExtra(ToDoProvider.KEY_TITLE);
				String text = data.getStringExtra(ToDoProvider.KEY_TEXT);
				ContentResolver contentResolver = getContentResolver();
				
				ContentValues values = new ContentValues();
				values.put(ToDoProvider.KEY_TITLE, title);
				values.put(ToDoProvider.KEY_TEXT, text);
				
				Uri uri = ContentUris.withAppendedId(ToDoProvider.CONTENT_URI, id);
				contentResolver.update(uri, values, null, null);
				updateArray();
			}
			break;
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
	    switch(id) {
	    case CONFIRM_REMOVE_DIALOG:
	    	return createConfirmRemoveDialog();
	    default:
	        dialog = null;
	    }
	    return dialog;
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
	}
	
	private Dialog createConfirmRemoveDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.remove_confirm_message)
		       .setCancelable(false)
		       .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                removeItem(selectedItem);
		                //resetSelected();
		           }
		       })
		       .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		return alert;
	}
	
	private void cancelAdd() {
		addingNew = false;
		myEditText.setVisibility(View.GONE);
		saveButton.setVisibility(View.GONE);
	}
	
	private void addNewItem() {
		addingNew = true;
		myEditText.setVisibility(View.VISIBLE);
		saveButton.setVisibility(View.VISIBLE);
		myEditText.requestFocus();
	}
	
	private void addNewItem(ToDoItem item) {
		ContentResolver contentResolver = getContentResolver();
		
		ContentValues values = new ContentValues();
		values.put(ToDoProvider.KEY_TITLE, item.getTitle());
		values.put(ToDoProvider.KEY_DATE, item.getDate().getTime());
		values.put(ToDoProvider.KEY_TEXT, item.getText());
		
		contentResolver.insert(ToDoProvider.CONTENT_URI, values);
		
	}
	
	private void removeItem(int index) {
		if(index > -1) {
			ToDoItem item = todoItems.get(index);
			ContentResolver contentResolver = getContentResolver();
			Uri uri = ContentUris.withAppendedId(ToDoProvider.CONTENT_URI, item.getId());
			contentResolver.delete(uri, null, null);
			updateArray();
		}
	}

}