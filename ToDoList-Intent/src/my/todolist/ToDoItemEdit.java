package my.todolist;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

public class ToDoItemEdit extends Activity {
	
	private static final int MENU_ITEM_OK = Menu.FIRST;
	private static final int MENU_ITEM_CANCEL = MENU_ITEM_OK + 1;
	
	private EditText title;
	private TextView date;
	private EditText text;
	private long id;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit);
		
		title = (EditText) findViewById(R.id.titleEditView);
		date = (TextView) findViewById(R.id.dateTextView);
		text = (EditText) findViewById(R.id.textEditView);
		
		Intent intent = getIntent();
		id = intent.getLongExtra(ToDoProvider.KEY_ID, -1l);
		String titleExtra = intent.getStringExtra(ToDoProvider.KEY_TITLE);
		Date dateExtra = new Date(intent.getLongExtra(ToDoProvider.KEY_DATE, 0l));
		String textExtra = intent.getStringExtra(ToDoProvider.KEY_TEXT);
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy");
		
		title.setText(titleExtra);
		date.setText(simpleDateFormat.format(dateExtra));
		text.setText(textExtra);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem addMenuItem = menu.add(0, MENU_ITEM_OK, Menu.NONE,
				R.string.ok);
		MenuItem removeMenuItem = menu.add(0, MENU_ITEM_CANCEL, Menu.NONE,
				R.string.cancel);

		addMenuItem.setIcon(R.drawable.add);
		removeMenuItem.setIcon(R.drawable.delete);

		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId()) {
			case (MENU_ITEM_OK): {
				Intent intent = new Intent();
				intent.putExtra(ToDoProvider.KEY_ID, id);
				intent.putExtra(ToDoProvider.KEY_TITLE, title.getText().toString());
				intent.putExtra(ToDoProvider.KEY_TEXT, text.getText().toString());
				setResult(RESULT_OK, intent);
				finish();
				return true;
			}
			case (MENU_ITEM_CANCEL): {
				setResult(RESULT_CANCELED, null);
				finish();
				return true;
			}
		}
		return false;
	}

}
