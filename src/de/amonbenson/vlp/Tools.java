package de.amonbenson.vlp;

import java.awt.Robot;
import java.awt.event.KeyEvent;

public class Tools {
	
	private static Robot robot;
	
	public static void doAltTab() {
		if (!initRobot()) return;

		// Do the alt tab
		robot.keyPress(KeyEvent.VK_ALT);
		robot.keyPress(KeyEvent.VK_TAB);
		robot.keyRelease(KeyEvent.VK_TAB);
		robot.keyRelease(KeyEvent.VK_ALT);
	}
	
	private static boolean initRobot() {
		if (robot != null) return true;
		try {
			robot = new Robot();
			return true;
		} catch (Exception ex) {
			System.err.println("Failed to create robot (ALT+TAB) won't work automatically");
		}
		
		return false;
	}
}
