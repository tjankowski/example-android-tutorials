package hello.world;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Hello extends Activity {
	
	Button setTextButton;
	EditText editText;
	TextView myTextView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setTextButton = (Button) findViewById(R.id.setTextButton);
        editText = (EditText) findViewById(R.id.editText);
        myTextView = (TextView) findViewById(R.id.myTextView);
        
        setTextButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				myTextView.setText(editText.getText());
				editText.setText("");
			}
		});
    }
}