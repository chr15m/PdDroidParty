package cx.mccormick.pddroidparty;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.midi.MidiToPdAdapter;
import org.puredata.android.midi.PdToMidiAdapter;
import org.puredata.android.service.PdService;
import org.puredata.core.PdBase;
import org.puredata.core.utils.PdDispatcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.content.pm.ActivityInfo;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
/* remove com.noisepages.nettoyeur stuff for now (ant1r june 2025)
import com.noisepages.nettoyeur.midi.MidiReceiver;
import com.noisepages.nettoyeur.usb.ConnectionFailedException;
import com.noisepages.nettoyeur.usb.DeviceNotConnectedException;
import com.noisepages.nettoyeur.usb.InterfaceNotAvailableException;
import com.noisepages.nettoyeur.usb.UsbBroadcastHandler;
import com.noisepages.nettoyeur.usb.midi.UsbMidiDevice;
import com.noisepages.nettoyeur.usb.midi.UsbMidiDevice.UsbMidiInput;
import com.noisepages.nettoyeur.usb.midi.UsbMidiDevice.UsbMidiOutput;
import com.noisepages.nettoyeur.usb.midi.util.UsbMidiInputSelector;
import com.noisepages.nettoyeur.usb.midi.util.UsbMidiOutputSelector;
import com.noisepages.nettoyeur.usb.util.AsyncDeviceInfoLookup;
*/

public class PdDroidParty extends Activity {
	public PdDroidPatchView patchview = null;
	public static final String PATCH = "PATCH";
	public static final String FROM_SELECTOR = "FROM_SELECTOR";
	private static final String PD_CLIENT = "PdDroidParty";
	private static final String TAG = "PdDroidParty";
	private static final int SAMPLE_RATE = 44100;
	public static final int DIALOG_NUMBERBOX = 1;
	public static final int DIALOG_SAVE = 2;
	public static final int DIALOG_LOAD = 3;
	public int dollarzero = -1;
	private boolean fromSelector = false;
	private String path;
	private PdService pdService = null;
	private final Object lock = new Object();
	public Menu ourmenu = null;
	Map<String, DroidPartyReceiver> receivemap = new HashMap<String, DroidPartyReceiver>();
	ArrayList<String[]> atomlines = null;
	Widget widgetpopped = null;
	MulticastLock wifiMulticastLock = null;
	private ProgressDialog progress = null;
	private MenuItem menuabout = null;
	private MenuItem menuexit = null;
	private MenuItem menumidi = null;

	/* remove com.noisepages.nettoyeur stuff for now (ant1r june 2025)
	private UsbMidiDevice midiDevice = null;
	private MidiToPdAdapter receiver = new MidiToPdAdapter();
	private PdToMidiAdapter sender;
	*/

	private int RECORD_AUDIO_PERMISSION_CODE = 49295197;

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
		Log.e(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		path = intent.getStringExtra(PATCH);
		fromSelector = intent.getBooleanExtra(FROM_SELECTOR, true);
		Log.e(TAG, "onCreate initGui");
		initGui();
		new Thread() {
			@Override
			public void run() {
				bindService(new Intent(PdDroidParty.this, PdService.class), serviceConnection, BIND_AUTO_CREATE);
			}
		} .start();
	}

	// this callback makes sure that we handle orientation changes without audio glitches
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.e(TAG, "onConfigurationChanged");
		super.onConfigurationChanged(newConfig);
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
		// midi menu
		// test for platforms that don't support USB OTG devices
		try {
			UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
			menumidi = menu.add(0, Menu.FIRST + menu.size(), 0, "Midi");
			menumidi.setIcon(android.R.drawable.ic_menu_manage);
		} catch(NoClassDefFoundError e) {
			// don't care
		}
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
		} else if (menumidi != null && item == menumidi) {
			/* remove com.noisepages.nettoyeur stuff for now (ant1r june 2025)
			if (midiDevice == null) {
				chooseMidiDevice();
			} else {
				midiDevice.close();
				midiDevice = null;
				post("USB MIDI connection closed");
			}
			*/
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

		for (int i = 0; i < bits.length; i++) {
			try {
				list.add(Float.parseFloat(bits[i]));
			} catch (NumberFormatException e) {
				list.add(bits[i]);
			}
		}

		Object[] ol = list.toArray();
		PdBase.sendList(dest, ol);
	}

	public String replaceDollarZero(String name) {
		return name.replace("\\$0", dollarzero + "").replace("$0", dollarzero + "");
	}

	public void registerReceiver(String name, Widget w) {
		// do $0 replacement
		String realname = replaceDollarZero(name);
		Log.e(TAG, "Receiver: " + realname);
		DroidPartyReceiver r = receivemap.get(realname);
		if (r == null) {
			r = new DroidPartyReceiver(patchview, w);
			receivemap.put(realname, r);
			dispatcher.addListener(realname, r.listener);
		} else {
			r.addWidget(w);
		}
	}

	// initialise the GUI with the OpenGL rendering engine
	private void initGui() {
		Log.e(TAG, "initGui runs");
		//setContentView(R.layout.main);
		int flags = WindowManager.LayoutParams.FLAG_FULLSCREEN |
		            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
		            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		getWindow().setFlags(flags, flags);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		atomlines = PdParser.parsePatch(path);
		patchview = new PdDroidPatchView(this, this);
		setContentView(patchview);
		patchview.requestFocus();
		MenuBang.clear();
	}

	// initialise Pd asking for the desired sample rate, parameters, etc.
	private void initPd() {
		Context context = this.getApplicationContext();
		// make sure netreceive can receive broadcast UDP packets
		wifiMulticastLock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).createMulticastLock("PdDroidPartyMulticastLock");
		Log.e(TAG, "Got Multicast Lock (before)? " + wifiMulticastLock.isHeld());
		wifiMulticastLock.acquire();
		Log.e(TAG, "Got Multicast Lock (after)? " + wifiMulticastLock.isHeld());
		// set up the midi stuff
		/*UsbMidiDevice.installBroadcastHandler(this, new UsbBroadcastHandler() {
			@Override
			public void onPermissionGranted(UsbDevice device) {
				if (midiDevice == null || !midiDevice.matches(device)) return;
				try {
					midiDevice.open(PdDroidParty.this);
				} catch (ConnectionFailedException e) {
					post("USB connection failed");
					midiDevice = null;
					return;
				}
				final UsbMidiOutputSelector outputSelector = new UsbMidiOutputSelector(midiDevice) {

					@Override
					protected void onOutputSelected(UsbMidiOutput output, UsbMidiDevice device, int iface, int index) {
						post("Output selection: Interface " + iface + ", Output " + index);
						try {
							sender = new PdToMidiAdapter(output.getMidiOut());
							PdBase.setMidiReceiver(sender);
						} catch (DeviceNotConnectedException e) {
							post("MIDI device has been disconnected");
						} catch (InterfaceNotAvailableException e) {
							post("MIDI interface is unavailable");
						}
					}

					@Override
					protected void onNoSelection(UsbMidiDevice device) {
						post("No output selected");
					}
				};
				new UsbMidiInputSelector(midiDevice) {

					@Override
					protected void onInputSelected(UsbMidiInput input, UsbMidiDevice device, int iface,
							int index) {
						post("Input selection: Interface " + iface + ", Input " + index);
						input.setReceiver(receiver);
						try {
							input.start();
						} catch (DeviceNotConnectedException e) {
							post("MIDI device has been disconnected");
							return;
						} catch (InterfaceNotAvailableException e) {
							post("MIDI interface is unavailable");
							return;
						}
						outputSelector.show(getFragmentManager(), null);
					}

					@Override
					protected void onNoSelection(UsbMidiDevice device) {
						post("No input selected");
						outputSelector.show(getFragmentManager(), null);
					}
				}.show(getFragmentManager(), null);
			}

			@Override
			public void onPermissionDenied(UsbDevice device) {
				if (midiDevice == null || !midiDevice.matches(device)) return;
				post("Permission denied for device " + midiDevice.getCurrentDeviceInfo());
				midiDevice = null;
			}

			@Override
			public void onDeviceDetached(UsbDevice device) {
				if (midiDevice == null || !midiDevice.matches(device)) return;
				midiDevice.close();
				midiDevice = null;
				post("MIDI device disconnected");
			}
		});*/

		// set a progress dialog running
		progress = new ProgressDialog(this);
		progress.setMessage("Loading...");
		progress.setCancelable(false);
		progress.setIndeterminate(true);
		progress.show();

		new Thread() {
			@Override
			public void run() {
				// get the actual lines of atoms from the patch
				String norecord = getFlag(atomlines, "norecord");

				if (Build.VERSION.SDK_INT >= 23 && norecord == null && !hasRecordPermission()) {
					// Show user dialog to grant permission to record audio
					requestPermissions(new String[] {Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_CODE);
				} else {
					startPd();
				}
			}
		} .start();
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// nah
			}
			// regardless of whether permission to record is granted, we still start the patch
			// (with no audio input if it has no permission)
			new Thread() {
				@Override
				public void run() {
					startPd();
				}
			} .start();
			return;
		}
	}

	public boolean hasRecordPermission() {
		return ContextCompat.checkSelfPermission(PdDroidParty.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
	}

	public void startPd() {
		String norecord = getFlag(atomlines, "norecord");

		int sRate = AudioParameters.suggestSampleRate();
		Log.e(TAG, "suggested sample rate: " + sRate);
		if (sRate < SAMPLE_RATE) {
			Log.e(TAG, "warning: sample rate is only " + sRate);
		}
		// clamp it
		sRate = Math.min(sRate, SAMPLE_RATE);
		Log.e(TAG, "actual sample rate: " + sRate);

		int nIn = Math.min(AudioParameters.suggestInputChannels(), 1);
		int nOut = Math.min(AudioParameters.suggestOutputChannels(), 2);

		PdBase.setReceiver(dispatcher);

		if (norecord != null) {
			Log.e(TAG, "norecord flag is set");
			nIn = 0;
		}

		if (!hasRecordPermission()) {
			Log.e(TAG, "no record permission given");
			nIn = 0;
		}

		Log.e(TAG, "input channels: " + nIn);
		if (nIn == 0) {
			Log.e(TAG, "warning: audio input not available");
		}

		Log.e(TAG, "output channels: " + nOut);
		if (nOut == 0) {
			Log.e(TAG, "warning: audio output not available");
		}

		// go ahead and intialise the audio
		try {
			pdService.initAudio(sRate, nIn, nOut, -1);   // negative values default to PdService preferences
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			finish();
		}

		try {
			dollarzero = PdBase.openPatch(path.toString());
		} catch (IOException e) {
			post(e.toString() + "; exiting now");
			finish();
		}
		patchview.buildUI(atomlines);
		// start the audio thread
		Resources res = getResources();
		String name = res.getString(R.string.app_name);
		pdService.startAudio(new Intent(PdDroidParty.this, PdDroidParty.class), R.drawable.icon, name, "Return to " + name + ".");
		// tell the patch view everything has been loaded
		patchview.loaded();
		// dismiss the progress meter
		progress.dismiss();
	}

	public static String getFlag(ArrayList<String[]> al, String flagname) {
		for (String[] line : al) {
			if (line.length >= 5) {
				if (line[4].equals("PdDroidParty.config." + flagname)) {
					return line[5];
				}
			}
		}
		return null;
	}

	public static boolean isLandscape(ArrayList<String[]> al) {
		for (String[] line : al) {
			if (line.length >= 4) {
				// find canvas begin and end lines
				if (line[1].equals("canvas")) {
					return Integer.parseInt(line[4]) > Integer.parseInt(line[5]);
				}
			}
		}
		return true;
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
		// let the screen blank again
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// make sure to release all resources
		if (pdService != null) {
			pdService.stopAudio();
		}
		if (dollarzero != -1) {
			PdBase.closePatch(dollarzero);
		}
		PdBase.sendMessage("pd", "quit", "bang");
		PdBase.release();
		try {
			unbindService(serviceConnection);
		} catch (IllegalArgumentException e) {
			// already unbound
			pdService = null;
		}
		// release midi
		/* remove com.noisepages.nettoyeur stuff for now (ant1r june 2025)
		if (midiDevice != null) {
			midiDevice.close();
		}
		UsbMidiDevice.uninstallBroadcastHandler(this);
		*/
		// release the lock on wifi multicasting
		if (wifiMulticastLock != null && wifiMulticastLock.isHeld())
			wifiMulticastLock.release();
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

	/* remove com.noisepages.nettoyeur stuff for now (ant1r june 2025)
	private void chooseMidiDevice() {
		// set a progress dialog running
		progress = new ProgressDialog(this);
		progress.setMessage("Waiting for USB midi");
		progress.setCancelable(false);
		progress.setIndeterminate(true);
		progress.show();
		final List<UsbMidiDevice> devices = UsbMidiDevice.getMidiDevices(this);
		new AsyncDeviceInfoLookup() {
			@Override
			protected void onLookupComplete() {
				// ok we are done
				progress.dismiss();
				if (!devices.isEmpty()) {
					// loop through all devices and add them
					for (int i = 0; i < devices.size(); ++i) {
						midiDevice = devices.get(i);
						midiDevice.requestPermission(PdDroidParty.this);
					}
					post("Added all midi devices.");
				} else {
					post("No midi devices found.");
				}
			}
		}.execute(devices.toArray(new UsbMidiDevice[devices.size()]));
	}
	*/

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
