package cx.mccormick.pddroidparty;

import cx.mccormick.pddroidparty.R;
 
import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;

public class NumberboxDialog extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.numberbox_dialog);
		
		//Context context = getApplicationContext();
		final EditText number = (EditText)findViewById(R.id.number);
		Intent intent = getIntent();
		number.setText("" + intent.getFloatExtra("number", 0));
		
		Button ok = (Button)findViewById(R.id.ok);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent result = new Intent();
				result.putExtra("number", Float.parseFloat(number.getText().toString()));
				setResult(RESULT_OK, result);
				finish();
			}
		});
		
		Button cancel = (Button)findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent result = new Intent();
				setResult(RESULT_CANCELED, result);
				finish();
			}
		});
	}
}
