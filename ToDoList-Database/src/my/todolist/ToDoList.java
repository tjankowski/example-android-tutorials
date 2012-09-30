package my.todolist;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import my.todolist.model.ToDoItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class ToDoList extends Activity {

	private static final int MENU_ITEM_ADD_NEW_TODO = Menu.FIRST;
	private static final int MENU_ITEM_REMOVE_TODO = MENU_ITEM_ADD_NEW_TODO + 1;
	
	private static final String PREFERENCE_KEY_TEXT_ENTRY = "PREFERENCE_KEY_TEXT_ENTRY";
	private static final String PREFERENCE_KEY_ADDING_ITEM = "PREFERENCE_KEY_ADDING_ITEM";
	private static final String PREFERENCE_KEY_SELECTED_INDEX = "PREFERENCE_KEY_SELECTED_INDEX";
	
	private static final int CONFIRM_REMOVE_DIALOG = 0;

	private EditText myEditText;
	private ListView myListView;
	private List<ToDoItem> todoItems;
	private ArrayAdapter<ToDoItem> todoItemsArrayAdapter;
	private boolean addingNew = false;
	private int selectedItem = -1;
	private ToDoDBAdapter toDoDBAdapter;
	private Cursor toDoListCursor;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		myEditText = (EditText) findViewById(R.id.myEditText);
		myListView = (ListView) findViewById(R.id.myListView);

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
						toDoDBAdapter.insertToDoItem(item);
						updateArray();
						myEditText.setText("");
						cancelAdd();
						return true;
					}
				}
				return false;
			}
		});

		registerForContextMenu(myListView);
		restoreUIState();
		
		toDoDBAdapter = new ToDoDBAdapter(this);
		toDoDBAdapter.open();
		populateToDoList();
	}
	
	private void populateToDoList() {
		toDoListCursor = toDoDBAdapter.getAllToDoItemsCursor();
		startManagingCursor(toDoListCursor);
		
		updateArray();
	}
	
	private void updateArray() {
		toDoListCursor.requery();
		todoItems.clear();
		
		if(toDoListCursor.moveToFirst()) {
			do {
			String title = toDoListCursor.getString(toDoListCursor.getColumnIndex(ToDoDBAdapter.KEY_TITLE));
			long date = toDoListCursor.getLong(toDoListCursor.getColumnIndex(ToDoDBAdapter.KEY_DATE));
			String text = toDoListCursor.getString(toDoListCursor.getColumnIndex(ToDoDBAdapter.KEY_TEXT));
			
			ToDoItem item = new ToDoItem(title, new Date(date), text);
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
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		toDoDBAdapter.close();
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
		
		switch (item.getItemId()) {
			case (MENU_ITEM_REMOVE_TODO): {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				selectedItem = menuInfo.position;
				showDialog(CONFIRM_REMOVE_DIALOG);
				return true;
			}
		}
		
		return false;
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
	}
	
	private void addNewItem() {
		addingNew = true;
		myEditText.setVisibility(View.VISIBLE);
		myEditText.requestFocus();
	}
	
	private void removeItem(int index) {
		if(index > -1) {
			toDoDBAdapter.removeToDoItem(todoItems.size() - index);
			updateArray();
		}
	}

}