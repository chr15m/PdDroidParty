package cx.mccormick.pddroidparty;

/* Based on SceneSelection code by Peter Brinkmann (thanks!) */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

public class PatchSelector extends Activity implements OnItemClickListener {
	
	private ListView patchList;
	private final Map<String, String> patches = new HashMap<String, String>();
	Resources res = null;
	private String pdzZipPath;
	private String folderName;
	private float version;
	private float latestVersion;
	private String latestMainPdzPath;
	ProgressDialog progress;
	Handler handler;
	private String latestMainFileName;
	private static long SPLASHTIME = 2000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		res = getResources();
		handler=new Handler();
		// if we have a baked patch, jump straight into Pd initialisation
		if (testForBakedPatch()) {
			doSplash();
		} else {
			initGui();
		}
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d("PatchSelector", "+ onStart");
		final Intent intent = getIntent();
		if (intent != null && intent.getAction().equals(Intent.ACTION_VIEW)) {
			 Log.d("PatchSelector", "> Got intent : " + intent);
			 final Uri data = intent.getData();
			 if (data != null) {
				 Log.d("PatchSelector", "> Got data   : " + data);
				 String scheme = data.getScheme();
				 if (ContentResolver.SCHEME_CONTENT.equals(scheme)) { // if the URI is of type content://
					// handle content uri to get sdcardPath
					 String[] filePathColumn = {MediaColumns.DATA};
					 Cursor cursor = getContentResolver().query(data, filePathColumn, null, null, null);
					 cursor.moveToFirst();
					 int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
					 pdzZipPath = cursor.getString(columnIndex);
					 cursor.close();
					 Log.d("PatchSelector", "> Open file  : " + pdzZipPath);
					 getLatestVersion();
					 process();
				 } else {
					// handle as file uri
					 pdzZipPath = data.getPath();
					 Log.d("PatchSelector", "> Open file  : " + pdzZipPath);
					 getLatestVersion(); // check the version of pdz we just clicked on
					 process();
				 }
			 }
		}
		return;
	}
	private void process() {
		if (!getDiskVersion()) {
			extract();
		}  else if (this.version < latestVersion || latestVersion == 0) {
			new AlertDialog.Builder(PatchSelector.this)
			.setTitle("New .pdz File")
			.setMessage("Do you want to replace " + version
					+ " with latest version " + latestVersion
					+ "").setCancelable(false)
					.setPositiveButton("Okay", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							extract();
						}
					}).setNegativeButton("Cancel", new OnClickListener() {
						@Override
						 public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
						}
					}).show();
		} else {
			 launchDroidParty(latestMainPdzPath);
		}
		
	}

	private void extract() {
		// TODO Auto-generated method stub
		try {
			List<File> listMain = IoUtils.extractZipResource(new FileInputStream(pdzZipPath), new File("/sdcard/PdDroidParty"), true);
			 if (listMain.size() != 0) {
				 for (File file : listMain) {
					 Log.d("Extracting", file.getAbsolutePath());
					
				 }
				 launchDroidParty("sdcard/PdDroidParty/"+folderName+"/"+latestMainFileName);
			 }
		}  catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private boolean getDiskVersion() {
		// TODO Auto-generated method stub
		List<File> listpdMain = IoUtils.find(new File("/sdcard/PdDroidParty"+ "/" + folderName), ".*droidparty_main\\.pd$");
		if (listpdMain.size() != 0) {
			 for (File f : listpdMain) {
				 Log.d("DiskFile", f.getAbsolutePath());
				 try {
					 InputStream is = new FileInputStream(f);
					 BufferedReader reader = new BufferedReader(new InputStreamReader(is));
					 String line;
					 while ((line = reader.readLine()) != null) {
						 String version;
						 if (line.contains(" version: ")) {
							 Log.d("VersionLine", line);
							 version = line.substring(line.lastIndexOf(":") + 1,line.length() - 1);
							 this.version = Float.parseFloat(version);
							 break;
						 } else {
							 version = "0";
							 this.version = Float.parseFloat(version);
						 }
						 
					 }
					 reader.close();
					 Log.d("DiskVersion", version+"");
					 Toast.makeText(PatchSelector.this, "DiskVersion"+version+"", Toast.LENGTH_SHORT).show();
				 } catch (Exception e) {
					 e.printStackTrace();
					 return false;
				 }
			 }
		}
		 return listpdMain.size() != 0;
	}

	private void getLatestVersion() {
		// TODO Auto-generated method stub
		File temp = new File("/sdcard/pdTemp");
		try{
			List<File> listMain = IoUtils.extractZipResource(new FileInputStream(pdzZipPath), temp, true);
			if (listMain.size() != 0) {
				for (File f : listMain) {
					if(f.isDirectory())
						folderName = f.getName();
					  if (f.getAbsolutePath().toLowerCase().contains("droidparty_main.pd")) {
						  latestMainPdzPath = f.getAbsolutePath();
						  latestMainFileName = f.getName();
							 InputStream is = new FileInputStream(f);
							 BufferedReader reader = new BufferedReader(new InputStreamReader(is));
							 String line;
							 while ((line = reader.readLine()) != null) {
								 String version;
								 if (line.contains(" version: ")) {
									 Log.d("LatestVersionLine", line);
									 version = line.substring(line.lastIndexOf(":") + 1, line.length() - 1);
									 this.latestVersion = Float.parseFloat(version);
									 break;
								 } else {
									 version = "0";
									 this.latestVersion = Float.parseFloat(version);
								 }
								 
							 }
							 reader.close();
							 Log.d("LatestVersion", latestVersion+"");
							 Toast.makeText(PatchSelector.this, "ClickedVersion"+latestVersion+"", Toast.LENGTH_SHORT).show();
						 }
						 
					 
				}
			}
		} catch (Exception e){
			e.printStackTrace();
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
		if(progress!=null){
			progress.dismiss();
		}
		startActivity(intent);
		finish();
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
		
		// add abstractions and externals zips
		Resources res = getResources();
		File libDir = getFilesDir();
		try {
			IoUtils.extractZipResource(res.openRawResource(R.raw.abstractions), libDir, true);
			// IoUtils.extractZipResource(res.openRawResource(Properties.hasArmeabiV7a ? R.raw.externals_v7a : R.raw.externals), libDir, true);
		} catch (IOException e) {
			Log.e("PatchSelector", e.toString());
		}
		PdBase.addToSearchPath(libDir.getAbsolutePath());
		
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
							Log.d("AbsPath", f.getAbsolutePath());
						}
					}
					ArrayList<String> keyList = new ArrayList<String>(patches.keySet());
					Collections.sort(keyList, new Comparator<String>() {
						public int compare(String a, String b) {
							return a.toLowerCase().compareTo(b.toLowerCase());
						}
					});
					final ArrayAdapter<String> adapter = new ArrayAdapter<String>(PatchSelector.this, android.R.layout.simple_list_item_1, keyList);
					handler.post(new Runnable() {
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
		progress = new ProgressDialog(this);
		progress.setMessage("Finding patches...");
		progress.setCancelable(false);
		progress.setIndeterminate(true);
		progress.show();
		initPd(progress);
		patchList.setOnItemClickListener(this);
	}
}
