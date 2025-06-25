package cx.mccormick.pddroidparty;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SaveDialog extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.save_dialog);

		final EditText filename = (EditText)findViewById(R.id.filename);
		Intent intent = getIntent();
		filename.setText(intent.getStringExtra("filename"));

		Button ok = (Button)findViewById(R.id.ok);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String newname = filename.getText().toString();
				Intent result = new Intent();
				if (newname.equals("")) {
					setResult(RESULT_CANCELED, result);
				} else {
					// TODO: check if the file exists and prompt to confirm overwrite first
					result.putExtra("filename", newname);
					setResult(RESULT_OK, result);
				}
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
