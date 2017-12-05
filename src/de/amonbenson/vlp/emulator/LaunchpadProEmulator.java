package de.amonbenson.vlp.emulator;

import java.awt.Point;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import javafx.scene.paint.Color;

public class LaunchpadProEmulator extends LaunchpadEmulator {

	private Color[] colorPalette;
	
	public LaunchpadProEmulator() {
		super(new Mode[] {Mode.XY_MODE, Mode.NOTE_MODE, Mode.DEVICE_MODE, Mode.USER_MODE});
		selectedMode = 3;

		readColorPalette();
		
		resetGrid();
		update();
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
				if (cmd == CMD_NOTE_OFF) dt2 = 0;
				
				Point p = getModePosition(dt1);
				if (p != null) {
					setPad(p.x, p.y, toColor(dt2));
					update();
				}
			}
		}
	}

	@Override
	public void processOutgoingButtonPress(int x, int y, boolean pressed) throws InvalidMidiDataException {
		
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
			if (i < 36 || i > 123) return null;
			i -= 36;

			int x = 0;
			if (i < 32) x = 1 + i % 4;
			else if (i < 64) x = 5 + i % 4;
			else if (i < 72) x = 9;
			else if (i < 80) x = 0;
			else x = i - 79;
			
			int y = 0;
			if (i < 64) y = 8 - i / 4 % 8;
			else if (i < 72) y = i - 63;
			else if (i < 80) y = i - 71;
			else y = 9;
			
			p = new Point(x, y);
		}
		
		return p;
	}

	private Color toColor(int col) {
		return colorPalette[col];
	}
	
	private void readColorPalette() {
		String[] colorPaletteStr = LaunchpadEmulator.readOptionFile("palette_mk2_pro.txt");
		colorPalette = new Color[colorPaletteStr.length];
		for (int i = 0; i < colorPaletteStr.length; i++) {
			String[] args = colorPaletteStr[i].replaceAll(" ", "").split(",");
			int r = Integer.parseInt(args[0]);
			int g = Integer.parseInt(args[1]);
			int b = Integer.parseInt(args[2]);
			colorPalette[i] = Color.rgb(r, g, b);
		}
	}
}
