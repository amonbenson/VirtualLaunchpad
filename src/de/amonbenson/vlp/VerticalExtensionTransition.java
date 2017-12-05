package de.amonbenson.vlp;

import javafx.animation.Transition;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class VerticalExtensionTransition extends Transition {

	public static final double DURATION = 300;
	public static final int DIRECTION_RETRACT = 0, DIRECTION_EXTEND = 1;

	private Region region, buttonRegion;

	private int direction;

	private double maxExtension;

	public VerticalExtensionTransition(Region region, Region buttonRegion, int direction, double maxExtension) {
		if (region == null)
			throw new NullPointerException("Region shouldn't be null.");

		this.region = region;
		this.buttonRegion = buttonRegion;
		this.maxExtension = maxExtension;

		setCycleDuration(Duration.millis(DURATION));
		setDirection(direction);
	}

	@Override
	protected void interpolate(double fraction) {
		// Set the scale
		if (direction == DIRECTION_RETRACT)
			region.setScaleY(1 - fraction);
		if (direction == DIRECTION_EXTEND)
			region.setScaleY(fraction);

		// Translate to make the scale happen on the top edge
		region.setTranslateY(region.getHeight() * (region.getScaleY() / 2 - 0.5));

		// Set the actual size to resize all the other panes
		region.setMinHeight(maxExtension * region.getScaleY());
		region.setMaxHeight(maxExtension * region.getScaleY());

		if (buttonRegion != null) {
			buttonRegion.setRotate(180 * region.getScaleY());
		}
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		if (direction < 0 || direction > 1)
			throw new IllegalArgumentException("Unknown direction (" + direction + ").");

		this.direction = direction;
	}

	public double getMaxExtension() {
		return maxExtension;
	}

	public void setMaxExtension(double maxExtension) {
		this.maxExtension = maxExtension;
	}
}