package de.amonbenson.vlp.emulator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;

import javafx.scene.paint.Color;

public abstract class LaunchpadEmulator {

	public static final long MIDI_MESSAGE_QUEUE_TIMEOUT_SECONDS = 30;
	
	public static final int CMD_NOTE_OFF = 128;
	public static final int CMD_NOTE_ON = 144;
	public static final int CMD_CONTROL_CHANGE = 176;
	
	Color[][] grid;
	private boolean doUpdate;
	
	private LaunchpadEmulatorMidiListener midiListener;
	private LinkedBlockingQueue<MidiMessage> midiMessageQueue;

	Mode[] modes;
	int selectedMode;
	
	public LaunchpadEmulator(Mode[] modes) {
		this.modes = modes;
		
		resetGrid();
		doUpdate = true;
		
		midiListener = null;
		midiMessageQueue = new LinkedBlockingQueue<MidiMessage>();
	}

	void update() {
		doUpdate = true;
	}
	
	void sendMidi(MidiMessage message) {
		midiMessageQueue.offer(message);
	}

	public boolean shouldDoUpdate() {
		if (doUpdate) {
			doUpdate = false;
			return true;
		}
		
		return false;
	}

	public void resetGrid() {
		grid = new Color[10][10];
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				grid[x][y] = Color.BLACK;
			}
		}
	}

	public Color[][] getGrid() {
		return grid;
	}

	protected void setPad(int x, int y, Color color) {
		grid[x][y] = color;
	}

	protected void setGrid(Color color) {
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				grid[x][y] = color;
			}
		}
	}
	
	public MidiMessage takeMidiMessage() {
		try {
			return midiMessageQueue.take();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public int getSelectedModeIndex() {
		return selectedMode;
	}

	public Mode getSelectedMode() {
		return modes[selectedMode];
	}

	public void setSelectedModeIndex(int index) {
		if (index < 0 || index > modes.length) throw new IndexOutOfBoundsException("Unknown mode: " + index + ".");
		this.selectedMode = index;
		
		resetGrid();
		update();
	}

	public boolean setSelectedMode(Mode mode) {
		for (int i = 0; i < modes.length; i++) {
			if (modes[i].equals(mode)) {
				setSelectedModeIndex(i);
				return true;
			}
		}
		
		return false;
	}
	
	public int getModeCount() {
		if (modes == null) return 0;
		return modes.length;
	}
	
	public Mode[] getModes() {
		return modes;
	}

	public abstract void processIncomingMessage(MidiMessage message);
	public abstract void processOutgoingButtonPress(int x, int y, boolean pressed) throws InvalidMidiDataException;
	
	public static String[] readOptionFile(String filename) {
		try {
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(LaunchpadEmulator.class.getResourceAsStream(filename)));
			
			String line;
			ArrayList<String> lines = new ArrayList<String>();
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) continue;
				if (line.startsWith("//")) continue;
				
				lines.add(line);
			}
			
			return lines.toArray(new String[lines.size()]);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
