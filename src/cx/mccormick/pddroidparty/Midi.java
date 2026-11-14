package cx.mccormick.pddroidparty;

import org.puredata.android.midi.MidiToPdAdapter;
import org.puredata.android.midi.PdToMidiAdapter;
import org.puredata.android.service.PdService;
import org.puredata.core.PdBase;

import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;

import java.util.ArrayList;
import java.io.IOException;

class Midi {
	private static final String TAG = "Midi";
	private MidiManager midiManager = null;
	private MidiDevice midiDevice = null;
	private PdToMidiAdapter pdToMidiAdapter = new PdToMidiAdapter();

	private String midiDeviceGetName(MidiDeviceInfo device) {
		String name = device.getProperties().getString(MidiDeviceInfo.PROPERTY_NAME);
		if (name == null) {
			name = device.getProperties().getString(MidiDeviceInfo.PROPERTY_PRODUCT);
		}
		return name;
	}

	public void setup(AppCompatActivity activity) {
		PdBase.setMidiReceiver(pdToMidiAdapter);
		midiManager = (MidiManager) activity.getSystemService(Context.MIDI_SERVICE);
		if (midiManager == null) {
			Log.w(TAG, "No MIDI support");
			return;
		}
		midiManager.registerDeviceCallback(new MidiManager.DeviceCallback() {
			@Override
			public void onDeviceAdded(MidiDeviceInfo device) {
				post("MIDI device added: " + midiDeviceGetName(device));
			}

			@Override
			public void onDeviceRemoved(MidiDeviceInfo device) {
				post("MIDI device removed: " + midiDeviceGetName(device));
				if (midiDevice != null && midiDevice.getInfo().equals(device)) {
					disconnectMidiDevice(false);
				}
			}
		}, null);
	}

	public void menuClicked(Context context) {
		if(midiManager == null) return;
		if (midiDevice != null) {
			disconnectMidiDevice(true);
		} else {
			chooseMidiDevice(context);
		}
	}

	private void disconnectMidiDevice(boolean verbose) {
		if(midiManager == null) return;
		if (midiDevice != null) {
			try {
				if(verbose) post("Closing MIDI device: " + midiDeviceGetName(midiDevice.getInfo()));
				pdToMidiAdapter.close(0); // close pdToMidi port 0 (the only one for now)
				midiDevice.close();
			} catch (IOException e) {
				Log.e(TAG, "Error closing MIDI device", e);
				if(verbose) post("Error closing MIDI device.");
			}
			midiDevice = null;
		}
	}

	private void chooseMidiDevice(Context context) {
		if (midiManager == null) {
			post("MIDI not available");
			return;
		}
		final MidiDeviceInfo[] devices = midiManager.getDevices();
		if (devices.length == 0) {
			post("No MIDI devices found");
			return;
		}
		final ArrayList<MidiDeviceInfo> deviceInfos = new ArrayList<>();
		final ArrayList<String> deviceNames = new ArrayList<>();
		for (MidiDeviceInfo info : devices) {
			String name = midiDeviceGetName(info);
			if(name != null) {
				deviceInfos.add(info);
				deviceNames.add(name);
				Log.v(TAG, "adding midi device: " + info.toString() + " : " + name);
			} else {
				Log.v(TAG, "discarding midi device: " + info.toString());
			}
		}
		if (deviceInfos.size() == 0) {
			post("No MIDI devices found");
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Choose MIDI Device");
		builder.setItems(deviceNames.toArray(new String[0]), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				connectMidiDevice(deviceInfos.get(which));
			}
		});
		builder.create().show();
	}

	private void connectMidiDevice(MidiDeviceInfo info) {
		midiManager.openDevice(info, new MidiManager.OnDeviceOpenedListener() {
			@Override
			public void onDeviceOpened(MidiDevice device) {
				if (device == null) {
					post("Could not open MIDI device");
					return;
				}
				midiDevice = device;
				post("MIDI device opened: " + midiDeviceGetName(device.getInfo()));

				// Input from device to Pd

				for (MidiDeviceInfo.PortInfo portInfo : device.getInfo().getPorts()) {
					if (portInfo.getType() == MidiDeviceInfo.PortInfo.TYPE_OUTPUT) {
						MidiOutputPort outputPort = device.openOutputPort(portInfo.getPortNumber());
						if (outputPort != null) {
							outputPort.connect(new MidiToPdAdapter(0));
							post("MIDI input enabled on port " + portInfo.getPortNumber());
							break; // Connect to first available output port
						}
					}
				}

				// Output from Pd to device

				for (MidiDeviceInfo.PortInfo portInfo : device.getInfo().getPorts()) {
					if (portInfo.getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT) {
						MidiInputPort inputPort = device.openInputPort(portInfo.getPortNumber());
						if (inputPort != null) {
							pdToMidiAdapter.open(inputPort, 0);
							post("MIDI output enabled on port " + portInfo.getPortNumber());
							break; // Connect to first available input port
						} else {
							Log.d(TAG, "unable to open Midi inputPort");
						}
					}
				}

			}
		}, null);
	}

	public void finish() {
		// release midi
		disconnectMidiDevice(false);
		if (midiManager != null) {
			// The callback cannot be unregistered, so we just null out the manager.
			// See https://developer.android.com/reference/android/media/midi/MidiManager#unregisterDeviceCallback(android.media.midi.MidiManager.DeviceCallback)
			midiManager = null;
		}
	}

	void post(final String msg) {}
}
