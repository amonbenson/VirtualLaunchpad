package de.amonbenson.vlp;

import de.amonbenson.vlp.emulator.Mode;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

public class LaunchpadPane extends AnchorPane {

	private LaunchpadCanvas canvas;

	public LaunchpadPane() {
		setId("padded-pane");
		
		canvas = new LaunchpadCanvas(1);
		getChildren().add(canvas);
	}

	public LaunchpadCanvas getCanvas() {
		return canvas;
	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();
		double x = snappedLeftInset();
		double y = snappedTopInset();
		double w = snapSize(getWidth()) - x - snappedRightInset();
		double h = snapSize(getHeight()) - y - snappedBottomInset();

		// Make a square
		w = h = Math.min(w, h);
		x = x + (getWidth() - w) / 2;
		y = y + (getHeight() - h) / 2;

		canvas.setLayoutX(x);
		canvas.setLayoutY(y);
		canvas.setWidth(w);
		canvas.setHeight(h);
	}
}
