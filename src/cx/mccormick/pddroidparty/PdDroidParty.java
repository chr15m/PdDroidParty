package cx.mccormick.pddroidparty;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.io.File;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdService;
import org.puredata.core.PdBase;
import org.puredata.core.PdReceiver;
import org.puredata.core.utils.PdUtils;
import org.puredata.core.utils.PdDispatcher;
import org.puredata.core.utils.IoUtils;

import android.app.ProgressDialog;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.text.Html;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;
import android.widget.TextView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.Menu;
import android.view.MenuItem;
import android.text.Spanned;
import android.text.util.Linkify;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;

import cx.mccormick.pddroidparty.PdParser;

public class PdDroidParty extends Activity {
	public PdDroidPatchView patchview = null;
	public static final String PATCH = "PATCH";
	private static final String PD_CLIENT = "PdDroidParty";
	private static final String TAG = "PdDroidParty";
	private static final int SAMPLE_RATE = 22050;
	public static final int DIALOG_NUMBERBOX = 1;
	public static final int DIALOG_SAVE = 2;
	public static final int DIALOG_LOAD = 3;
	
	private String path;
	private PdService pdService = null;
	private String patch;  // the path to the patch receiver is defined in res/values/strings.xml
	private final Object lock = new Object();
	public Menu ourmenu = null;
	Map<String, DroidPartyReceiver> receivemap = new HashMap<String, DroidPartyReceiver>();
	ArrayList<String[]> atomlines = null;
	Widget widgetpopped = null;
	
	private MenuItem menuabout = null;
	private MenuItem menuexit = null;
	
	private final PdDispatcher dispatcher = new PdDispatcher() {
		@Override
		public void print(String s) {
			Log.e("Pd [print]", s);
		}
	};
	
	// post a 'toast' alert to the Android UI
	private void post(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), PD_CLIENT + ": " + msg, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	// our connection to the Pd service
	private final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			synchronized(lock) {
				pdService = ((PdService.PdBinder) service).getService();
				initPd();
			}
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// this method will never be called
		}
	};
	
	// called when the app is launched
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		path = intent.getStringExtra(PATCH);
		initGui();
		new Thread() {
			@Override
			public void run() {
				bindService(new Intent(PdDroidParty.this, PdService.class), serviceConnection, BIND_AUTO_CREATE);
			}
		}.start();
	}

	// this callback makes sure that we handle orientation changes without audio glitches
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		initGui();
	}

	// When the app shuts down
	@Override
	protected void onDestroy() {
		super.onDestroy();
		cleanup();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	// menu launch yeah
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		// about menu item
		menuabout = menu.add(0, Menu.FIRST + menu.size(), 0, "About");
		menuabout.setIcon(android.R.drawable.ic_menu_info_details); 
		// add the menu bang menu items
		MenuBang.setMenu(menu);
		// TODO: preferences = ic_menu_preferences
		// exit menu item
		menuexit = menu.add(0, Menu.FIRST + menu.size(), 0, "Exit");
		menuexit.setIcon(android.R.drawable.ic_menu_close_clear_cancel); 
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == menuabout) {
			// load in the about dialog contents from assets/about.html
			StringBuffer sb = new StringBuffer();
			try {
				AssetManager assets = getAssets();
				InputStreamReader reader = new InputStreamReader(assets.open("about.html"), "UTF-8");
				BufferedReader br = new BufferedReader(reader);
				String line = br.readLine();
				while(line != null) {
					sb.append(line + "\n");
					line = br.readLine();
				}
			} catch (IOException e) {
				sb.append("Copyright Chris McCormick, 2011");
			}
			
			// convert the string to HTML for the about dialog
			final SpannableString s = new SpannableString(Html.fromHtml(sb.toString()));
			Linkify.addLinks(s, Linkify.ALL);
			
			AlertDialog ab = new AlertDialog.Builder(this)
			.setTitle("About")
			.setIcon(android.R.drawable.ic_dialog_info)
			.setMessage(s)
			.setPositiveButton("ok", null)
			.create();
			ab.show();
			// make the links clickable
			((TextView)ab.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
		} else if (item == menuexit) {
			finish();
		} else {
			// pass the menu selection through to the MenuBang manager
			MenuBang.hit(item);
		}
		return super.onOptionsItemSelected(item);
	}
	
	// send a Pd atom-string 's' to a particular receiver 'dest'
	public void send(String dest, String s) {
		List<Object> list = new ArrayList<Object>();
		String[] bits = s.split(" ");
		
		for (int i=0; i < bits.length; i++) {
			try {
				list.add(Float.parseFloat(bits[i]));
			} catch (NumberFormatException e) {
				list.add(bits[i]);
			}
		}
		
		Object[] ol = list.toArray();
		PdBase.sendList(dest, ol);
	}
	
	public void registerReceiver(String name, Widget w) {
		DroidPartyReceiver r = receivemap.get(name);
		if (r == null) {
			r = new DroidPartyReceiver(patchview, w);
			receivemap.put(name, r);
			dispatcher.addListener(name, r.listener);
		} else {
			r.addWidget(w);
		}
	}
	
	// initialise the GUI with the OpenGL rendering engine
	private void initGui() {
		//setContentView(R.layout.main);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		patchview = new PdDroidPatchView(this, this);
		setContentView(patchview);
		patchview.requestFocus();
		MenuBang.clear();
	}
	
	// initialise Pd asking for the desired sample rate, parameters, etc.
	private void initPd() {
		final ProgressDialog progress = new ProgressDialog(this);
		progress.setMessage("Loading...");
		progress.setCancelable(false);
		progress.setIndeterminate(true);
		progress.show();
		new Thread() {
			@Override
			public void run() {
				int sRate = AudioParameters.suggestSampleRate();
				Log.e(TAG, "suggested sample rate: " + sRate);
				if (sRate < SAMPLE_RATE) {
					Log.e(TAG, "warning: sample rate is only " + sRate);
				}
				// clamp it
				sRate = Math.min(sRate, SAMPLE_RATE);
				Log.e(TAG, "actual sample rate: " + sRate);
				
				int nIn = Math.min(AudioParameters.suggestInputChannels(), 1);
				Log.e(TAG, "input channels: " + nIn);
				if (nIn == 0) {
					Log.e(TAG, "warning: audio input not available");
				}
				
				int nOut = Math.min(AudioParameters.suggestOutputChannels(), 2);
				Log.e(TAG, "output channels: " + nOut);
				if (nOut == 0) {
					Log.e(TAG, "audio output not available; exiting");
					finish();
					return;
				}
				
				Resources res = getResources();
				PdBase.setReceiver(dispatcher);
				
				try {
					// parse the patch for GUI elements
					PdParser p = new PdParser();
					// p.printAtoms(p.parsePatch(path));
					// get the actual lines of atoms from the patch
					atomlines = p.parsePatch(path);
					// some devices don't have a mic and might be buggy
					// so don't create the audio in unless we really need it
					// TODO: check a config option for this
					//if (!hasADC(atomlines)) {
					//	nIn = 0;
					//}
					// go ahead and intialise the audio
					try {
						pdService.initAudio(sRate, nIn, nOut, -1);   // negative values default to PdService preferences
					} catch (IOException e) {
						Log.e(TAG, e.toString());
						finish();
					}
					patch = PdUtils.openPatch(path);
					patchview.buildUI(p, atomlines);
					// start the audio thread
					String name = res.getString(R.string.app_name);
					pdService.startAudio(new Intent(PdDroidParty.this, PdDroidParty.class), R.drawable.icon, name, "Return to " + name + ".");
					// tell the patch view everything has been loaded
					patchview.loaded();
					// dismiss the progress meter
					progress.dismiss();
				} catch (IOException e) {
					post(e.toString() + "; exiting now");
					finish();
				}
			}
		}.start();
	}
	
	public boolean hasADC(ArrayList<String[]> al) {
		boolean has = false;
		for (String[] line: al) {
			if (line.length >= 5) {
				// find canvas begin and end lines
				if (line[4].equals("adc~")) {
					has = true;
				}
			}
		}
		return has;
	}
	
	public File getPatchFile() {
		return new File(path);
	}
	
	// close the app and exit
	@Override
	public void finish() {
		cleanup();
		super.finish();
	}
	
	// quit the Pd service and release other resources
	private void cleanup() {
		// make sure to release all resources
		if (pdService != null) pdService.stopAudio();
		PdUtils.closePatch(patch);
		PdBase.sendMessage("pd", "quit", "bang");
		PdBase.release();
		try {
			unbindService(serviceConnection);
		} catch (IllegalArgumentException e) {
			// already unbound
			pdService = null;
		}
	}
	
	public void launchDialog(Widget which, int type) {
		widgetpopped = which;
		if (type == DIALOG_NUMBERBOX) {
			Intent it = new Intent(this, NumberboxDialog.class);
			it.putExtra("number", which.getval());
			startActivityForResult(it, DIALOG_NUMBERBOX);
		} else if (type == DIALOG_SAVE) {
			Intent it = new Intent(this, SaveDialog.class);
			it.putExtra("filename", ((LoadSave)which).getFilename());
			it.putExtra("directory", ((LoadSave)which).getDirectory());
			it.putExtra("extension", ((LoadSave)which).getExtension());
			startActivityForResult(it, DIALOG_SAVE);
		} else if (type == DIALOG_LOAD) {
			Intent it = new Intent(this, LoadDialog.class);
			it.putExtra("filename", ((LoadSave)which).getFilename());
			it.putExtra("directory", getPatchRelativePath(((LoadSave)which).getDirectory()));
			it.putExtra("extension", ((LoadSave)which).getExtension());
			startActivityForResult(it, DIALOG_LOAD);
		}
	}
	
	public String getPatchRelativePath(String dir) {
		String root = getPatchFile().getParent();
		if (dir.equals(".")) {
			return root;
		} else if (dir.charAt(0) != '/') {
			return root + "/" + dir;
		} else {
			return dir;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data); 
		if (resultCode == RESULT_OK) {
			if (widgetpopped != null) {
				if (requestCode == DIALOG_NUMBERBOX) {
					widgetpopped.receiveFloat(data.getFloatExtra("number", 0));
					widgetpopped.send("" + widgetpopped.getval());
				} else if (requestCode == DIALOG_SAVE) {
					((LoadSave)widgetpopped).gotFilename("save", data.getStringExtra("filename"));
				} else if (requestCode == DIALOG_LOAD) {
					((LoadSave)widgetpopped).gotFilename("load", data.getStringExtra("filename"));
				}
				// we're done with our originating widget and dialog
				widgetpopped = null;
				// force a redraw
				patchview.invalidate();
			}
		}
	}
}
