package cx.mccormick.pddroidparty;

import android.media.midi.MidiReceiver;
import android.util.Log;

import org.puredata.core.PdBase;
import java.util.ArrayList;

public class MidiToPdAdapter extends MidiReceiver {
	ArrayList<Integer> message = new ArrayList<Integer>();
	@Override
	public void onSend(byte[] msg, int offset, int count, long timestamp) {
		while(count-- != 0) {
			int b = msg[offset++] & 0xff;
			if(b > 127) {
				message.clear();
				message.add(b);
				continue;
			}
			message.add(b);
			if(message.size() == 3) {
				int status = (message.get(0) >> 4);
				Log.d("Midi", "message status: " + status + " bytes: " + message.get(1) + " " + message.get(2));
				if(status == 11) {
					int channel = message.get(0) & 0x0f;
					int controller = message.get(1);
					int value = message.get(2);
					//Log.d("Midi", "control chan: " + channel + " ctl: " + controller + " val: " + value);
					PdBase.sendControlChange(channel, controller, value);
				}
			}
			// TODO: handle other MIDI messages
		}
	}
}
