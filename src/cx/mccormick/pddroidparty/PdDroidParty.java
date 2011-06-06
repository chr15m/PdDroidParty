package cx.mccormick.pddroidparty;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdService;
import org.puredata.core.PdBase;
import org.puredata.core.PdReceiver;
import org.puredata.core.utils.PdUtils;
import org.puredata.core.utils.PdDispatcher;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import cx.mccormick.pddroidparty.PdParser;

public class PdDroidParty extends Activity {
	public PdDroidPatchView patchview = null;
	public static final String PATCH = "PATCH";
	private static final String PD_CLIENT = "PdDroidParty";
	private static final String TAG = "PdDroidParty";
	private static final int SAMPLE_RATE = 22050;
	private static final int DIALOG_NUMBERBOX = 1;
	
	private String path;
	private PdService pdService = null;
	private String patch;  // the path to the patch receiver is defined in res/values/strings.xml
	private final Object lock = new Object();
	Map<String, DroidPartyReceiver> receivemap = new HashMap<String, DroidPartyReceiver>();
	ArrayList<String[]> atomlines = null;
	Numberbox nbxpopped = null;
	
	private final PdDispatcher dispatcher = new PdDispatcher() {
		@Override
		public void print(String s) {
			// TODO: hmmm, maybe just log this?
			post(s);
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
	}
	
	// initialise Pd asking for the desired sample rate, parameters, etc.
	private void initPd() {
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
				//dispatcher.addListener(RJ_IMAGE_ANDROID, overlayListener);
				//dispatcher.addListener(RJ_TEXT_ANDROID, overlayListener);
				
				try {
					// parse the patch for GUI elements
					PdParser p = new PdParser();
					// p.printAtoms(p.parsePatch(path));
					// get the actual lines of atoms from the patch
					atomlines = p.parsePatch(path);
					// some devices don't have a mic and might be buggy
					// so don't create the audio in unless we really need it
					if (!hasADC(atomlines)) {
						nIn = 0;
					}
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
		PdBase.release();
		try {
			unbindService(serviceConnection);
		} catch (IllegalArgumentException e) {
			// already unbound
			pdService = null;
		}
	}
	
	public void launchNumberboxDialog(Numberbox which) {
		nbxpopped = which;
		Intent it = new Intent(this, NumberboxDialog.class);
		it.putExtra("number", which.getval());
		startActivityForResult(it, DIALOG_NUMBERBOX);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data); 
		if (requestCode == DIALOG_NUMBERBOX) {
			if (resultCode == RESULT_OK) {
				if (nbxpopped != null) {
					nbxpopped.setandsend(data.getFloatExtra("number", 0));
				}
			}
			nbxpopped = null;
			patchview.invalidate();
		}
	}
}
