package cx.mccormick.pddroidparty.PatchSelector;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import android.util.Log;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import cx.mccormick.pddroidparty.R;

public class LinksEditor extends Activity {
	static final String TAG = "LinksEditor";

	public static void run(Activity activity, int resultCode, String filename) {
		Intent it = new Intent(activity, LinksEditor.class);
		it.putExtra("filename", filename);
		activity.startActivityForResult(it, resultCode);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.links_editor);

		final EditText text = (EditText)findViewById(R.id.text);
		Intent intent = getIntent();
		String filename = intent.getStringExtra("filename");
		File file = new File(filename);
		String content = "";
		try {
			file.createNewFile();
			byte[] encoded = Files.readAllBytes(Paths.get(filename));
			content = new String(encoded);
		} catch(Exception e) {}
		text.setText(content);

		Button ok = (Button)findViewById(R.id.ok);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent result = new Intent();
				result.putExtra("filename", filename);
				result.putExtra("text", text.getText().toString());
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

	public static void onResult(int resultCode, Intent data) {
		if(resultCode == RESULT_OK) {
			try {
				String text = data.getStringExtra("text");
				Files.write(Paths.get(data.getStringExtra("filename")), text.getBytes());
			} catch (Exception e) {
				e.printStackTrace();
			}
			//Files.writeString(Paths.get(data.getStringExtra("filename")), data.getStringExtra("text"));
		}
	}
}
