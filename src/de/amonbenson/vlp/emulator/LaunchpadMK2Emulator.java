package de.amonbenson.vlp.emulator;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;

import javafx.scene.paint.Color;

public class LaunchpadMK2Emulator extends LaunchpadEmulator {

	public LaunchpadMK2Emulator() {
		super(new Mode[] {Mode.XY_MODE, Mode.USER_MODE});

		// Not implemented yet. TODO: IMPLEMENT!
		for (int i = 0; i < 8; i++) {
			setPad(i + 2, i + 1, Color.DARKRED);
			setPad(i, i + 1, Color.DARKRED);
			setPad(i + 1, i + 2, Color.DARKRED);
			setPad(i + 1, i, Color.DARKRED);

			setPad(i + 2, 8 - i, Color.DARKRED);
			setPad(i, 8 - i, Color.DARKRED);
			setPad(i + 1, 9 - i, Color.DARKRED);
			setPad(i + 1, 7 - i, Color.DARKRED);
		}
		for (int i = 0; i < 8; i++) {
			setPad(i + 1, i + 1, Color.RED);
			setPad(i + 1, 8 - i, Color.RED);
		}
		update();
	}

	@Override
	public void processIncomingMessage(MidiMessage message) {
		
	}

	@Override
	public void processOutgoingButtonPress(int x, int y, boolean pressed) throws InvalidMidiDataException {
		
	}

}
