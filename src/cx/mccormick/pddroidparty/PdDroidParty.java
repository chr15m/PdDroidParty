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

	private static final String PD_CLIENT = "Pd Client";
	private static final int SAMPLE_RATE = 22050;
	private PdService pdService = null;
	private String patch;  // the path to the patch receiver is defined in res/values/strings.xml

	private final PdReceiver receiver = new PdReceiver() {
		@Override public void receiveSymbol(String source, String symbol) {}
		@Override public void receiveMessage(String source, String symbol, Object... args) {}
		@Override public void receiveList(String source, Object... args) {}
		@Override public void receiveFloat(String source, float x) {}
		@Override public void receiveBang(String source) {}

		@Override public void print(String s) {
			post(s);
		}
	};

	private void post(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), PD_CLIENT + ": " + msg, Toast.LENGTH_LONG).show();
			}
		});
	}

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initGui();
		bindService(new Intent(this, PdService.class), serviceConnection, BIND_AUTO_CREATE);
	}

	// this callback makes sure that we handle orientation changes without audio glitches
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		initGui();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cleanup();
	}
	
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
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// osc1
		// osc2
		Log.e("EV:", event.getX() + ", " + event.getY());
		PdBase.sendFloat("osc1", event.getX());
		PdBase.sendFloat("osc2", event.getY());
		return true;
	}
	
	private void initGui() {
		//setContentView(R.layout.main);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		GLSurfaceView view = new GLSurfaceView(this);
		view.setRenderer(new OpenGLRenderer());
		setContentView(view);
	}
	
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
		String path = res.getString(R.string.path_to_patch);
		PdBase.setReceiver(receiver);
		try {
			pdService.initAudio(SAMPLE_RATE, nIn, nOut, -1);   // negative values default to PdService preferences
			patch = PdUtils.openPatch(path);
			// parse the patch for GUI elements
			PdParser p = new PdParser();
			p.printAtoms(p.parsePatch(path));
			// start the audio thread
			String name = res.getString(R.string.app_name);
			pdService.startAudio(new Intent(this, PdDroidParty.class), R.drawable.icon, name, "Return to " + name + ".");
		} catch (IOException e) {
			post(e.toString() + "; exiting now");
			finish();
		}
	}

	@Override
	public void finish() {
		cleanup();
		super.finish();
	}

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
