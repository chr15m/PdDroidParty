package cx.mccormick.pddroidparty;

/* Based on SceneSelection code by Peter Brinkmann (thanks!) */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.puredata.android.utils.Properties;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PatchSelector extends Activity implements OnItemClickListener {

	private ListView patchList;
	private final Map<String, String> patches = new HashMap<String, String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initGui();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
		TextView item = (TextView) v;
		String name = item.getText().toString();
		Intent intent = new Intent(this, PdDroidParty.class);
		intent.putExtra(PdDroidParty.PATCH, patches.get(name));
		startActivity(intent);
	}
	
	private void initGui() {
		setContentView(R.layout.patch_selector);
		patchList = (ListView) findViewById(R.id.patch_selector);
		final ProgressDialog progress = new ProgressDialog(this);
		progress.setMessage("Finding patches...");
		progress.setCancelable(false);
		progress.setIndeterminate(true);
		progress.show();
		new Thread() {
			@Override
			public void run() {
				List<File> list = IoUtils.find(new File("/sdcard"), ".*droidparty_main\\.pd$");
				for (File f: list) {
					String[] parts = f.getParent().split("/");
					patches.put(parts[parts.length - 1], f.getAbsolutePath());
				}
				ArrayList<String> keyList = new ArrayList<String>(patches.keySet());
				Collections.sort(keyList, new Comparator<String>() {
					public int compare(String a, String b) {
						return a.toLowerCase().compareTo(b.toLowerCase());
					}
				});
				final ArrayAdapter<String> adapter = new ArrayAdapter<String>(PatchSelector.this, android.R.layout.simple_list_item_1, keyList);
				patchList.getHandler().post(new Runnable() {
					@Override
					public void run() {
						patchList.setAdapter(adapter);
						progress.dismiss();
					}
				});
			};
		}.start();
		patchList.setOnItemClickListener(this);
	}
}
