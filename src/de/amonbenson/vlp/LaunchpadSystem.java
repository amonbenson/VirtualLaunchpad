package de.amonbenson.vlp;

import java.util.ArrayList;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

public class LaunchpadSystem implements Receiver {
	
	ArrayList<MidiDevice> inputDevices, outputDevices;
	
	Transmitter transmitter;
	Receiver receiver;
	
	private MidiInputListener midiInputListener;
	
	private boolean debugEnabled;

	public LaunchpadSystem() {
		transmitter = null;
		receiver = null;
		
		debugEnabled = false;
	}
	
	public void sendMidi(MidiMessage message, long tick) {
		if (receiver != null) {
			if (debugEnabled) {
				System.out.print("[Send]");
				for (byte b : message.getMessage()) System.out.print(" " + (b & 0xFF));
				System.out.println();
			}
			receiver.send(message, tick);
		}
	}
	
	public ArrayList<MidiDevice> getInputDevices() {
		return inputDevices;
	}
	
	public ArrayList<MidiDevice> getOutputDevices() {
		return outputDevices;
	}
	
	public ArrayList<String> getInputDevicesAsString() {
		if (inputDevices == null) return null;
		
		ArrayList<String> str = new ArrayList<String>();
		for (MidiDevice device : inputDevices) {
			str.add(device.getDeviceInfo().getName());
		}
		
		return str;
	}
	
	public ArrayList<String> getOutputDevicesAsString() {
		if (outputDevices == null) return null;
		
		ArrayList<String> str = new ArrayList<String>();
		for (MidiDevice device : outputDevices) {
			str.add(device.getDeviceInfo().getName());
		}
		
		return str;
	}
	
	public void openInput(int index) {
		if (inputDevices == null) throw new NullPointerException("Scan devices first.");
		openInput(inputDevices.get(index));
	}
	
	public void openOutput(int index) {
		if (outputDevices == null) throw new NullPointerException("Scan devices first.");
		openOutput(outputDevices.get(index));
	}
	
	public void openInput(MidiDevice device) {
		if (inputDevices == null) throw new NullPointerException("Scan devices first.");
		try {
			transmitter = device.getTransmitter();
			transmitter.setReceiver(this);
		} catch (Exception ex) {
			System.err.println("Could not open device.");
			ex.printStackTrace();
		}
	}
	
	public void openOutput(MidiDevice device) {
		if (outputDevices == null) throw new NullPointerException("Scan devices first.");
		try {
			receiver = device.getReceiver();
		} catch (Exception ex) {
			System.err.println("Could not open device.");
			ex.printStackTrace();
		}
	}
	
	public void scan() {
		inputDevices = new ArrayList<MidiDevice>();
		outputDevices = new ArrayList<MidiDevice>();
		
		// Scan all devices so we can 
		Info[] info = MidiSystem.getMidiDeviceInfo();
		for (Info i : info) {
			try {
				// Get a midi device
				MidiDevice device = MidiSystem.getMidiDevice(i);
				
				// Try to open it, so we can check how many ports it has
				if (!device.isOpen()) device.open();
				
				// Try to get a transmitter
				try {
					Transmitter transmitter = device.getTransmitter();
					inputDevices.add(device);
					
					continue;
				} catch (MidiUnavailableException ex) {
				}
				
				// Try to get a receiver
				try {
					Receiver receiver = device.getReceiver();
					outputDevices.add(device);
					
					continue;
				} catch (MidiUnavailableException ex) {
				}
				
				// Close it up again
				device.close();
			} catch (Exception ex) {
				System.err.println("Could not check device: " + i.getName());
			}
		}
	}
	
	public void shutdown() {
		// Closes all midi devices
		for (MidiDevice d : inputDevices) d.close();
		for (MidiDevice d : outputDevices) d.close();
	}

	@Override
	public void close() {
		
	}

	@Override
	public void send(MidiMessage message, long tick) {
		if (debugEnabled) {
			System.out.print("[Recv]");
			for (byte b : message.getMessage()) System.out.print(" " + (b & 0xFF));
			System.out.println();
		}
		if (midiInputListener != null) midiInputListener.reveiceMidi(message, tick);
	}
	
	public MidiInputListener getMidiInputListener() {
		return midiInputListener;
	}

	public void setMidiInputListener(MidiInputListener midiInputListener) {
		this.midiInputListener = midiInputListener;
	}
}
