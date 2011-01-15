package cx.mccormick.pddroidparty;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdService;
import org.puredata.core.PdBase;
import org.puredata.core.PdReceiver;
import org.puredata.core.utils.PdUtils;

import android.app.Activity;
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
import android.opengl.GLSurfaceView;
import android.view.Window;
import android.view.WindowManager;

import cx.mccormick.pddroidparty.PdParser;

public class PdDroidParty extends Activity {

	public GLSurfaceView view = null;
	public static final String PATCH = "PATCH";
	private static final String PD_CLIENT = "PdDroidParty";
	private static final int SAMPLE_RATE = 22050;
	private String path;
	private PdService pdService = null;
	private String patch;  // the path to the patch receiver is defined in res/values/strings.xml
	public int width;
	public int height;
	ArrayList<Widget> widgets = new ArrayList<Widget>();
	
	// receive messages and prints back from Pd
	private final PdReceiver receiver = new PdReceiver() {
		@Override public void receiveSymbol(String source, String symbol) {}
		@Override public void receiveMessage(String source, String symbol, Object... args) {}
		@Override public void receiveList(String source, Object... args) {}
		@Override public void receiveFloat(String source, float x) {}
		@Override public void receiveBang(String source) {}

		@Override public void print(String s) {
			Log.e("PdDroidParty", s);
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
			pdService = ((PdService.PdBinder) service).getService();
			initPd();
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
		bindService(new Intent(this, PdService.class), serviceConnection, BIND_AUTO_CREATE);
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
	
	// if we receive a touch even
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// osc1
		// osc2
		Log.e("EV:", event.getX() + ", " + event.getY());
		PdBase.sendFloat("osc1", event.getX());
		PdBase.sendFloat("osc2", event.getY());
		// TODO: for down/move events, loop through all sliders and test
		if (widgets != null) {
			for (Widget widget: widgets) {
				widget.touch(event);
			}
		}

		return true;
	}
	
	// initialise the GUI with the OpenGL rendering engine
	private void initGui() {
		//setContentView(R.layout.main);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		view = new GLSurfaceView(this);
		view.setRenderer(new OpenGLRenderer(widgets));
		setContentView(view);
	}
	
	// initialise Pd asking for the desired sample rate, parameters, etc.
	private void initPd() {
		if (AudioParameters.suggestSampleRate() < SAMPLE_RATE) {
			post("required sample rate not available; exiting");
			finish();
			return;
		}
		int nIn = Math.min(AudioParameters.suggestInputChannels(), 1);
		if (nIn == 0) {
			post("warning: audio input not available");
		}
		int nOut = Math.min(AudioParameters.suggestOutputChannels(), 2);
		if (nOut == 0) {
			post("audio output not available; exiting");
			finish();
			return;
		}
		Resources res = getResources();
		PdBase.setReceiver(receiver);
		try {
			pdService.initAudio(SAMPLE_RATE, nIn, nOut, -1);   // negative values default to PdService preferences
			patch = PdUtils.openPatch(path);
			// parse the patch for GUI elements
			PdParser p = new PdParser();
			// p.printAtoms(p.parsePatch(path));
			buildUI(p, p.parsePatch(path));
			// start the audio thread
			String name = res.getString(R.string.app_name);
			pdService.startAudio(new Intent(this, PdDroidParty.class), R.drawable.icon, name, "Return to " + name + ".");
		} catch (IOException e) {
			post(e.toString() + "; exiting now");
			finish();
		}
	}
	
	// build a user interface using the stuff found in the patch
	private void buildUI(PdParser p, ArrayList<String[]> atomlines) {
		//ArrayList<String> canvases = new ArrayList<String>();
		int level = 0;
		
		for (String[] line: atomlines) {
			if (line.length >= 4) {
				// find canvas begin and end lines
				if (line[1].equals("canvas")) {
					/*if (canvases.length == 0) {
						canvases.add(0, "self");
					} else {
						canvases.add(0, line[6]);
					}*/
					width = Integer.parseInt(line[4]);
					height = Integer.parseInt(line[5]);
					level += 1;
				} else if (line[1].equals("restore")) {
					//canvases.remove(0);
					level -= 1;
				// find different types of UI element in the top level patch
				} else if (level == 1) {
					if (line[4].equals("vsl")) {
						p.printAtom(line);
					} else if (line[4].equals("hsl")) {
						widgets.add(new Slider(this, line));
					} else if (line[4].equals("tgl")) {
						p.printAtom(line);
					}
				}
			}
		}
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
}
