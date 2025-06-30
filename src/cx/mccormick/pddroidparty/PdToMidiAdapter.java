package cx.mccormick.pddroidparty;

import org.puredata.core.PdMidiReceiver;
import android.media.midi.MidiInputPort;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Adapter class for connecting MIDI output from Pd to input for AndroidMidi.
 * 
 * Adapted from:
 * https://github.com/nettoyeurny/btmidi/blob/master/AndroidMidi/src/com/noisepages/nettoyeur/midi/ToWireConverter.java
 */
 
public class PdToMidiAdapter implements PdMidiReceiver {
	private Map<Integer, MidiInputPort> inputPorts = new HashMap<Integer, MidiInputPort>();

	public void open(MidiInputPort inputPort, int pdPort) {
		close(inputPort);
		close(pdPort);
		inputPorts.put(pdPort, inputPort);
	}

	public void close(MidiInputPort inputPort) {
		if(! inputPorts.containsValue(inputPort)) return;
		for (Entry<Integer, MidiInputPort> entry : inputPorts.entrySet()) {
			if (entry.getValue().equals(inputPort)) {
				close(entry.getKey());
			}
		}
	}

	public void close(int pdPort) {
		MidiInputPort inputPort = inputPorts.get(pdPort);
		if(inputPort != null) {
			try {
				inputPort.close();
			} catch(Exception e) {}
			inputPorts.remove(pdPort);
		}
		
	}

	@Override
	public void receiveNoteOn(int channel, int pitch, int velocity) {
		write(0x90, channel, pitch, velocity);
	}

	@Override
	public void receivePolyAftertouch(int channel, int pitch, int value) {
		write(0xa0, channel, pitch, value);
	}

	@Override
	public void receiveControlChange(int channel, int controller, int value) {
		write(0xb0, channel, controller, value);
	}

	@Override
	public void receiveProgramChange(int channel, int program) {
		write(0xc0, channel, program);
	}

	@Override
	public void receiveAftertouch(int channel, int value) {
		write(0xd0, channel, value);
	}

	@Override
	public void receivePitchBend(int channel, int value) {
		value += 8192;
		write(0xe0, channel, (value & 0x7f), (value >> 7));
	}

	@Override
	public void receiveMidiByte(int port, int value) {
		final byte[] message = {(byte) value};
		writeMessage(port, message);
	}

	private static byte firstByte(int msg, int ch) {
		return (byte) (msg | (ch & 0x0f));
	}

	private void write(int msg, int ch, int a) {
		final byte[] message = {firstByte(msg, ch), (byte) a};
		writeMessage(ch, message);
	}

	private void write(int msg, int ch, int a, int b) {
		final byte[] message = {firstByte(msg, ch), (byte) a, (byte) b};
		writeMessage(ch, message);
	}

	private void writeMessage(int channel, byte[] message) {
		MidiInputPort inputPort = inputPorts.get(channel / 16);
		if(inputPort != null) try {
			inputPort.send(message, 0, message.length);
		} catch(Exception e) {}
	}

	@Override
	public boolean beginBlock() {
		return false;
	}

	@Override
	public void endBlock() {
	}
}
