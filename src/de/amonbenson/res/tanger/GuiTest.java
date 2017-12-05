package de.amonbenson.res.tanger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraintsBuilder;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class GuiTest extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Gui Test");
		BorderPane root = new BorderPane();
		Scene scene = new Scene(root, 600, 400);
		
		GridPane grid = new GridPane();
        grid.getColumnConstraints().setAll(
                ColumnConstraintsBuilder.create().percentWidth(100/3.0).build(),
                ColumnConstraintsBuilder.create().percentWidth(100/3.0).build(),
                ColumnConstraintsBuilder.create().percentWidth(100/3.0).build()
        );

		grid.add(new Label("Label"), 0, 0);
		grid.add(new Button("Button"), 1, 0);
		grid.add(new ToggleButton("Toggle Button"), 2, 0);
		
		grid.add(new RadioButton("Radio Button"), 0, 1);
		grid.add(new CheckBox("Check Box"), 1, 1);
		grid.add(new ChoiceBox(FXCollections.observableArrayList("First", "Second", "Third")), 2, 1);

		grid.add(new TextField("Text Field"), 0, 2);
		grid.add(new PasswordField(), 1, 2);
		grid.add(new ComboBox(FXCollections.observableArrayList("First", "Second", "Third")), 2, 2);

		grid.add(new TextArea("I'm far to long to be displayed without the need of a scroll pane.\nlol\na\nb\nc\nd\ne\nf\na\nb\nc\nd\ne\nf"), 0, 3);
		ListView<String> lw = new ListView<String>(FXCollections.observableArrayList("First", "Second", "Third"));
		lw.setPrefHeight(100);
		lw.setTooltip(new Tooltip("I am a Tooltip."));
		grid.add(lw, 1, 3);
		grid.add(new ScrollBar(), 2, 3);

		grid.add(new Separator(), 0, 4);
		grid.add(new Slider(), 1, 4);
		grid.add(new Hyperlink("Hyperlink"), 2, 4);

		grid.add(new ProgressBar(0.6), 0, 5);
		grid.add(new ProgressIndicator(0.6), 1, 5);
		ColorPicker cp = new ColorPicker();
		cp.setOnAction((ActionEvent event) -> {
			root.setStyle("-tanger-accent: #" + Integer.toHexString(cp.getValue().hashCode()) + ";");
		});
		grid.add(cp, 2, 5);
		
		Button fcb = new Button("File Chooser");
		fcb.setOnAction((ActionEvent event) -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open Some File");
			fileChooser.showOpenDialog(primaryStage);
		});
		grid.add(fcb, 0, 6);
		
		root.setCenter(grid);
		scene.getStylesheets().add("de/amonbenson/res/tanger/tanger.css");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
