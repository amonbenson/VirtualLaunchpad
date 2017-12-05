package de.amonbenson.vlp.emulator;

import java.awt.Point;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import javafx.scene.paint.Color;

public class LaunchpadMiniEmulator extends LaunchpadEmulator {

	public static final int CMD_NOTE_ON_RAPID_LED = 146;

	private int rapidLEDCount;

	public LaunchpadMiniEmulator() {
		super(new Mode[] {Mode.XY_MODE, Mode.USER_MODE});
		resetAll();
	}

	@Override
	public void processIncomingMessage(MidiMessage message) {
		if (message instanceof ShortMessage) {
			ShortMessage sm = (ShortMessage) message;
			int cmd = sm.getCommand() + sm.getChannel();
			int dt1 = sm.getData1();
			int dt2 = sm.getData2();

			// Set grid LEDs
			if (cmd == CMD_NOTE_ON || cmd == CMD_NOTE_OFF) {
				rapidLEDCount = 0;
				if (cmd == CMD_NOTE_OFF) dt2 = 0;
				
				Point p = getModePosition(dt1);
				if (p != null) {
					setPad(p.x, p.y, toColor(dt2));
					update();
				}
			}

			if (cmd == CMD_CONTROL_CHANGE) {
				rapidLEDCount = 0;

				if (dt1 == 0) {
					// Reset Launchpad
					if (dt2 == 0)
						resetAll();

					// Set the grid mapping mode
					/*
					if (dt2 == 1)
						setSelectedMode(Mode.XY_MODE);
					if (dt2 == 2)
						setSelectedMode(Mode.USER_MODE);
					*/

					// Turn on all LEDs
					if (dt2 >= 125 && dt2 <= 127) {
						resetAll();
						setGrid(toColor(dt2 - 124, dt2 - 124));
						update();
					}
				}

				// Set Automap/Live control LEDs
				if (dt1 >= 104 && dt1 <= 111) {
					setPad(dt1 - 103, 0, toColor(dt2));
					update();
				}
			}
			
			// Rapid LED update
			if (cmd == CMD_NOTE_ON_RAPID_LED) {
				if (rapidLEDCount < 32) {
					int i = rapidLEDCount * 2;
					int x1 = 1 + i % 8;
					int x2 = 1 + (i + 1) % 8;
					int y1 = 1 + i / 8;
					int y2 = 1 + (i + 1) / 8;
					setPad(x1, y1, toColor(dt1));
					setPad(x2, y2, toColor(dt2));
				} else if (rapidLEDCount < 36) {
					int i = 1 + (rapidLEDCount - 32) * 2;
					setPad(9, i, toColor(dt1));
					setPad(9, i + 1, toColor(dt2));
				} else if (rapidLEDCount < 40) {
					int i = 1 + (rapidLEDCount - 36) * 2;
					setPad(i, 0, toColor(dt1));
					setPad(i + 1, 0, toColor(dt2));
				}
				
				update();
				rapidLEDCount++;
			}
		}
	}

	@Override
	public void processOutgoingButtonPress(int x, int y, boolean pressed) throws InvalidMidiDataException {
		int d2 = pressed ? 127 : 0;
		
		// Grid button pressed
		if (x > 0 && y <= 9 && y > 0 && y < 9) {
			int d1 = getModePositionInverse(x, y);
			
			sendMidi(new ShortMessage(CMD_NOTE_ON, 0, d1, d2));
		}
		
		// Automap/Live button pressed
		if (y == 0) {
			int d1 = x + 103;
			
			sendMidi(new ShortMessage(CMD_CONTROL_CHANGE, 0, d1, d2));
		}
	}
	
	private int getModePositionInverse(int x, int y) {
		if (getSelectedMode().equals(Mode.XY_MODE)) {
			return x + (y - 1) * 16 - 1;
		}

		if (getSelectedMode().equals(Mode.USER_MODE)) {
			if (x == 9) return y + 99;
			if (x < 5) return 64 - (y - 1) * 4 + x - 1;
			if (x < 9) return 96 - (y - 1) * 4 + x - 5;
		}
		
		return 0;
	}

	private Point getModePosition(int i) {
		Point p = null;

		if (getSelectedMode().equals(Mode.XY_MODE)) {
			if (i < 0 || i > 127) return null;
			
			int x = 1 + Math.min(i % 16, 8);
			int y = 1 + i / 16;
			p = new Point(x, y);
		}

		if (getSelectedMode().equals(Mode.USER_MODE)) {
			if (i < 36 || i > 107) return null;
			i -= 36;
			
			int x = 0;
			if (i < 32) x = 1 + i % 4;
			else if (i < 64) x = 5 + i % 4;
			else x = 9;
			
			int y = 0;
			if (i < 64) y = 8 - i / 4 % 8;
			else y = i - 63;
			
			p = new Point(x, y);
		}
		
		return p;
	}

	private void resetAll() {
		selectedMode = 1;
		rapidLEDCount = 0;
		
		resetGrid();
		update();
	}

	private Color toColor(int col) {
		int r = col % 4;
		int g = (col / 16) % 4;
		return Color.color(r / 3.0, g / 3.0, 0.0);
	}

	private Color toColor(int r, int g) {
		return Color.color(r / 3.0, g / 3.0, 0.0);
	}
}
