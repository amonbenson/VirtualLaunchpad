package de.amonbenson.vlp;

import java.util.List;

import javafx.animation.Transition;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class VerticalExtensionTransition extends Transition {

	public static final double DURATION = 300;
	public static final int DIRECTION_RETRACT = 0, DIRECTION_EXTEND = 1;

	private Region region, buttonRegion;
	private List<Node> fadeNodes;

	private int direction;

	private double maxExtension;

	public VerticalExtensionTransition(Region region, Region buttonRegion, List<Node> fadeNodes, int direction, double maxExtension) {
		if (region == null)
			throw new NullPointerException("Region shouldn't be null.");

		this.region = region;
		this.buttonRegion = buttonRegion;
		this.fadeNodes = fadeNodes;
		
		this.maxExtension = maxExtension;

		setCycleDuration(Duration.millis(DURATION));
		setDirection(direction);
	}

	@Override
	protected void interpolate(double fraction) {
		// Set the scale
		double scale = 0;
		if (direction == DIRECTION_RETRACT)
			scale = 1 - fraction;
		if (direction == DIRECTION_EXTEND)
			scale = fraction;
		region.setScaleY(scale);

		// Translate to make the scale happen on the top edge
		region.setTranslateY(region.getHeight() * (scale / 2 - 0.5));

		// Set the actual size to resize all the other panes
		region.setMinHeight(maxExtension * scale);
		region.setMaxHeight(maxExtension * scale);

		// Rotate the trigger button
		if (buttonRegion != null) {
			buttonRegion.setRotate(180 * scale);
		}
		
		// Hide the other ghost buttons
		for (Node node : fadeNodes) {
			if (node != null) {
				double maxOpacity = 1;
				if (node instanceof GhostButton) maxOpacity = ((GhostButton) node).getMaxOpacity();
				node.setOpacity(scale * maxOpacity);
			}
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