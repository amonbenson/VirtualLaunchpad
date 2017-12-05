package de.amonbenson.vlp.emulator;

import javax.sound.midi.MidiMessage;

public interface LaunchpadEmulatorMidiListener {
	public void outgoingMidi(MidiMessage message);
}
