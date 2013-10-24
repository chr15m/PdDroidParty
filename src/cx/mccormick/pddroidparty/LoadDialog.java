package cx.mccormick.pddroidparty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.puredata.core.utils.IoUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class LoadDialog extends Activity {
	private static final String TAG = "LoadDialog";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.load_dialog);
		ArrayList<String> filenames = new ArrayList<String>();
		final ListView filelist = (ListView)findViewById(R.id.filelist);
		Intent intent = getIntent();
		List<File> list = IoUtils.find(new File(intent.getStringExtra("directory")), ".*\\." + intent.getStringExtra("extension") + "$");
		for (File f: list) {
			String fn = f.getName();
			int i = fn.lastIndexOf('.');
			if (i > 0 && i < fn.length() - 1) {
				filenames.add(fn.substring(0, i));
			}
		}
		if (filenames.size() > 0) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(LoadDialog.this, android.R.layout.simple_list_item_1, filenames);
			filelist.setAdapter(adapter);
		}
		
		Button cancel = (Button)findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent result = new Intent();
				setResult(RESULT_CANCELED, result);
				finish();
			}
		});
		
		filelist.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
				TextView item = (TextView) v;
				String name = item.getText().toString();
				Intent result = new Intent();
				result.putExtra("filename", name);
				setResult(RESULT_OK, result);
				finish();
			}
		});
	}
}
