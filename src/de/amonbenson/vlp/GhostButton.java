package de.amonbenson.vlp;

import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class GhostButton extends Button {

	public static final int SIZE = 24;

	public GhostButton(String iconPath) {
		getStyleClass().add("ghost-button");

		ImageView icon = new ImageView(
				new Image(getClass().getClassLoader().getResource(iconPath).toExternalForm(), SIZE, SIZE, true, true));

		ColorAdjust adj = new ColorAdjust();
		adj.setBrightness(1.0);
		adj.setSaturation(-1.0);
		icon.setEffect(adj);

		setGraphic(icon);
	}
}
