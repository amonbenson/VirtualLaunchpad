package de.amonbenson.vlp;

import javax.sound.midi.MidiMessage;

public interface MidiInputListener {
	public void reveiceMidi(MidiMessage message, long tick);
}
