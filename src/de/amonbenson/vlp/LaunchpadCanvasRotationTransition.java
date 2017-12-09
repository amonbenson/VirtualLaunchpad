package de.amonbenson.vlp;

import javafx.animation.Transition;
import javafx.util.Duration;

public class LaunchpadCanvasRotationTransition extends Transition {
	
	public static final double DURATION = 200;
		
	private LaunchpadCanvas lpCanvas;
	private int oldRotation, newRotation;
	
	public LaunchpadCanvasRotationTransition(LaunchpadCanvas lpCanvas) {
		this.lpCanvas = lpCanvas;
		
		setCycleDuration(Duration.millis(DURATION));
	}
	
	@Override
	protected void interpolate(double fraction) {
		if (lpCanvas == null) return;
		
		double oldR = oldRotation * 90;
		double newR = newRotation * 90;
		
		if (newR - oldR > 180) oldR += 360;
		if (oldR - newR > 180) newR += 360;
		
		double r = oldR + (newR - oldR) * fraction;
		lpCanvas.setRotate(r);
	}
	
	public void rotateClockwise() {
		int r = getRotation();
		int newr = r + 1;
		if (newr > 3) newr = 0;
		setRotation(newr);
		playFromStart();
	}
	
	public void rotateCounterclockwise() {
		int r = getRotation();
		int newr = r - 1;
		if (newr < 0) newr = 3;
		setRotation(newr);
		playFromStart();
	}
	
	public void setRotation(int i) {
		oldRotation = newRotation;
		newRotation = i;
	}
	
	public int getRotation() {
		return newRotation;
	}
}
