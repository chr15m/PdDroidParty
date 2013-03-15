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
import java.lang.System;

import org.puredata.android.utils.Properties;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.graphics.Color;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

public class PatchSelector extends Activity implements OnItemClickListener {
	
	private ListView patchList;
	private final Map<String, String> patches = new HashMap<String, String>();
	Resources res = null;
	private static long SPLASHTIME = 2000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		res = getResources();
		// if we have a baked patch, jump straight into Pd initialisation
		if (testForBakedPatch()) {
			doSplash();
		} else {
			initGui();
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
		TextView item = (TextView) v;
		String name = item.getText().toString();
		launchDroidParty(patches.get(name));
	}
	
	private void launchDroidParty(String path) {
		Intent intent = new Intent(this, PdDroidParty.class);
		intent.putExtra(PdDroidParty.PATCH, path);
		startActivity(intent);
	}
	
	private boolean testForBakedPatch() {
		return res.getIdentifier("patch", "raw", getPackageName()) != 0;
	}
	
	private String unpackBakedPatch() {
		// if we have a patch zip
		if (testForBakedPatch()) {
			//IoUtils.extractZipResource(res.openRawResource(R.raw.abstractions), libDir, false);
			//IoUtils.extractZipResource(res.openRawResource(Properties.hasArmeabiV7a ? R.raw.externals_v7a : R.raw.externals), libDir, false);
			// where we will be storing the patch on the sdcard
			String basedir = "/sdcard/" + res.getString(res.getIdentifier("dirname", "string", getPackageName()));
			// version file for the existing patch
			File version = new File(basedir + "/patch/VERSION-" + res.getInteger(res.getIdentifier("revno", "integer", getPackageName())));
			// if the version file does not exist
			if (!version.exists()) {
				try {
					IoUtils.extractZipResource(getResources().openRawResource(res.getIdentifier("patch", "raw", getPackageName())), new File(basedir), false);
				} catch (IOException e) {
					// do nothing
					return null;
				}
			}
			//PdBase.addToSearchPath(libDir.getAbsolutePath());
			PdBase.addToSearchPath(basedir + "/patch");
			// also add the recordings directory
			(new File(basedir, "recordings")).mkdirs();
			// also add the safefiles directory
			(new File(basedir, "savefiles")).mkdirs();
			// return the patch path to launch Pd
			return basedir + "/patch/droidparty_main.pd";
		}
		return null;
	}
	
	private void initPd(final ProgressDialog progress) {
		new Thread() {
			@Override
			public void run() {
				long start = System.currentTimeMillis();
				// see if this is an app with a zip to unpack instead
				String bakedpatch = unpackBakedPatch();
				long elapsed = System.currentTimeMillis() - start;
				if (elapsed < SPLASHTIME) {
					try {
						this.sleep(SPLASHTIME - elapsed);
					} catch (Exception e) {
					}
				}
				if (bakedpatch != null) {
					launchDroidParty(bakedpatch);
					finish();
				} else {
					List<File> list = IoUtils.find(new File("/sdcard/PdDroidParty"), ".*droidparty_main\\.pd$");
					for (File f: list) {
						String[] parts = f.getParent().split("/");
						// exclude generic patch directories found in apps based on PdDroidParty
						if (!parts[parts.length - 1].equals("patch")) {
							patches.put(parts[parts.length - 1], f.getAbsolutePath());
						}
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
							if (progress != null) {
								progress.dismiss();
							}
						}
					});
				}
			};
		}.start();		
	}
	
	private void doSplash() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	        // Create a new ImageView
		ImageView imageView = new ImageView(this);
		// Set the background color to white
		imageView.setBackgroundColor(Color.WHITE);
		int splashres = res.getIdentifier("splash", "raw", getPackageName());
		if (splashres != 0) {
			// Parse the SVG file from the resource
			SVG svg = SVGParser.getSVGFromResource(getResources(), splashres);
			// Get a drawable from the parsed SVG and set it as the drawable for the ImageView
			imageView.setImageDrawable(svg.createPictureDrawable());
			// Set the ImageView as the content view for the Activity
			setContentView(imageView);
		}
		// initialise Pd without a progress thing
		initPd(null);
	}
	
	private void initGui() {
		setContentView(R.layout.patch_selector);
		patchList = (ListView) findViewById(R.id.patch_selector);
		final ProgressDialog progress = new ProgressDialog(this);
		progress.setMessage("Finding patches...");
		progress.setCancelable(false);
		progress.setIndeterminate(true);
		progress.show();
		initPd(progress);
		patchList.setOnItemClickListener(this);
	}
}
