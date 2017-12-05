package de.amonbenson.vlp.emulator;

public class Mode {

	public static Mode XY_MODE = new Mode("XY");
	public static Mode NOTE_MODE = new Mode("Note");
	public static Mode DEVICE_MODE = new Mode("Device");
	public static Mode USER_MODE = new Mode("User");
	
	private String name;
	
	public Mode(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean equals(Object other) {
		if (other instanceof Mode)
			return getName().equals(((Mode) other).getName());
		return false;
	}
}
