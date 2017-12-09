package de.amonbenson.vlp;

import java.awt.Desktop;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;

import de.amonbenson.vlp.LaunchpadCanvas.CaseType;
import de.amonbenson.vlp.emulator.LaunchpadEmulator;
import de.amonbenson.vlp.emulator.LaunchpadMK2Emulator;
import de.amonbenson.vlp.emulator.LaunchpadMiniEmulator;
import de.amonbenson.vlp.emulator.LaunchpadProEmulator;
import de.amonbenson.vlp.emulator.Mode;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

	public static final String VERSION = "1.0";
	public static final String RESOURCE_FOLDER = "de/amonbenson/res/";
	public static final String TITLE = "Virtual Launchpad " + VERSION;

	public static final long UPDATE_TICK_DELAY = 10;

	public static final int STATE_RETRACTED = 0, STATE_EXTENDED = 1;

	public static final String[] LAUNCHPAD_TYPES = new String[] { "Launchpad Mini", "Launchpad MK2", "Launchpad Pro" };

	private Stage primaryStage;
	private BorderPane root;

	private VerticalExtensionTransition dropdownMenuTransition;
	private LaunchpadCanvasRotationTransition launchpadCanvasRotationTransition;

	private GridPane dropdownMenu;
	private int dropdownMenuState;

	private ComboBox launchpadSelectorBox, inputSelectorBox, outputSelectorBox;
	private Button rescan, dropdownTrigger, about, rotate;

	private HBox modeSelectorBox;
	private ToggleGroup modeSelectorGroup;

	private LaunchpadPane launchpadPane;

	private LaunchpadSystem lpSystem;
	private LaunchpadEmulator lpEmulator;

	private boolean wasLPCanvasFocused = false;

	@Override
	public void start(Stage primaryStage) {
		try {
			this.primaryStage = primaryStage;

			primaryStage.setTitle(TITLE);
			primaryStage.setOnCloseRequest((WindowEvent event) -> {
				// Close the launchpad system and stop application
				System.exit(0);
			});
			root = new BorderPane();

			// CREATE LAUNCHPAD SYSTEM
			lpSystem = new LaunchpadSystem();

			// DROP DOWN MIDI SELCTION MENU
			dropdownMenu = new GridPane();
			ColumnConstraints col = new ColumnConstraints();
			col.setPercentWidth(100 / 3.0);
			dropdownMenu.getColumnConstraints().addAll(col, col, col);

			rescan = new Button("Rescan Midi");
			rescan.setOnAction((ActionEvent event) -> rescanMidi());
			rescan.setTooltip(new Tooltip("Refresh the number of connected midi devices."));
			rescan.setPrefWidth(Double.MAX_VALUE);
			dropdownMenu.add(rescan, 0, 0);

			launchpadSelectorBox = new ComboBox();
			launchpadSelectorBox.setOnAction((Event event) -> setSelectedLaunchpadEmulator());
			launchpadSelectorBox.setPrefWidth(Double.MAX_VALUE);
			launchpadSelectorBox.getItems().addAll(LAUNCHPAD_TYPES);
			//launchpadSelectorBox.getSelectionModel().selectFirst();
			launchpadSelectorBox.getSelectionModel().select(2);
			launchpadSelectorBox.setTooltip(new Tooltip("Select the type of Launchpad to be emulated."));
			dropdownMenu.add(launchpadSelectorBox, 0, 1);

			inputSelectorBox = new ComboBox();
			inputSelectorBox.setOnAction((Event event) -> openSelectedInput());
			inputSelectorBox.setPrefWidth(Double.MAX_VALUE);
			inputSelectorBox.getSelectionModel().selectFirst();
			inputSelectorBox.setTooltip(new Tooltip("Virtual Input Port"));
			dropdownMenu.add(inputSelectorBox, 1, 0);

			outputSelectorBox = new ComboBox();
			outputSelectorBox.setOnAction((Event event) -> openSelectedOutput());
			outputSelectorBox.setPrefWidth(Double.MAX_VALUE);
			outputSelectorBox.getSelectionModel().selectFirst();
			outputSelectorBox.setTooltip(new Tooltip("Virtual Output Port"));
			dropdownMenu.add(outputSelectorBox, 2, 0);

			modeSelectorBox = new HBox();
			modeSelectorBox.setAlignment(Pos.CENTER_LEFT);
			modeSelectorGroup = new ToggleGroup();
			dropdownMenu.add(modeSelectorBox, 1, 1, 2, 1);

			root.setTop(dropdownMenu);

			// MAIN LAUNCHPAD PANE
			launchpadPane = new LaunchpadPane();
			root.setCenter(launchpadPane);

			// SEMI-TRANSPARENT CONTROL BUTTONS
			dropdownTrigger = new GhostButton(RESOURCE_FOLDER + "icons/dropdown.png");
			dropdownTrigger.setTooltip(new Tooltip("show / hide Menu"));
			dropdownTrigger.setRotate(180);
			dropdownTrigger.setOnAction((event) -> {
				if (dropdownMenuState == STATE_EXTENDED)
					retractMenu();
				if (dropdownMenuState == STATE_RETRACTED)
					extendMenu();
			});

			about = new GhostButton(RESOURCE_FOLDER + "icons/about.png");
			about.setOnAction((event) -> showAboutWindow());
			about.setTooltip(new Tooltip("About"));

			rotate = new GhostButton(RESOURCE_FOLDER + "icons/rotate.png");
			rotate.setOnAction((event) -> launchpadCanvasRotationTransition.rotateClockwise());
			rotate.setTooltip(new Tooltip("Rotate Launchpad clockwise"));

			launchpadPane.getChildren().addAll(dropdownTrigger, about, rotate);
			AnchorPane.setLeftAnchor(dropdownTrigger, 0.0);
			AnchorPane.setRightAnchor(about, 0.0);
			AnchorPane.setBottomAnchor(rotate, 0.0);

			// Scan for all the midi devices
			rescanMidi();

			// Link the launchpad system to the emulated launchpad
			lpSystem.setMidiInputListener((MidiMessage message, long tick) -> {
				try {
					if (lpEmulator != null)
						lpEmulator.processIncomingMessage(message);
				} catch (ArrayIndexOutOfBoundsException ex) {
					System.err.println("Unknown button position, trying to continue.");
					ex.printStackTrace();
				}
			});

			// Link the launchpad canvas to the emulated launchpad
			launchpadPane.getCanvas().setLaunchpadCanvasListener((int x, int y, boolean pressed) -> {
				if (lpEmulator != null)
					try {
						lpEmulator.processOutgoingButtonPress(x, y, pressed);
					} catch (InvalidMidiDataException e) {
						e.printStackTrace();
					}
			});

			// Link the emulated launchpad to the launchpad system
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						try {
							if (lpEmulator != null) {
								MidiMessage message = lpEmulator.takeMidiMessage();
								lpSystem.sendMidi(message, 0);
							}
						} catch (Exception ex) {
							System.err.println("Error while updating.");
						}
					}
				}
			}).start();

			// Link the emulated launchpad to the launchpad canvas
			AnimationTimer lpCanvasTimer = new AnimationTimer() {
				@Override
				public void handle(long timestamp) {
					try {
						if (lpEmulator != null) {
							if (lpEmulator.shouldDoUpdate()) {
								launchpadPane.getCanvas().setPads(lpEmulator.getGrid());
								launchpadPane.getCanvas().render();
							}
						}

						Thread.sleep(UPDATE_TICK_DELAY);
					} catch (Exception ex) {
						System.err.println("Error while updating.");
					}
				}
			};

			// Init all the midi io stuff
			setSelectedLaunchpadEmulator();
			openSelectedInput();
			openSelectedOutput();

			// These things (like selecting list values, starting animations)
			// have to be triggered after javafx created the window
			Platform.runLater(() -> {
				// Select a dummy value
				selectDummyValue();

				// Start the dropdown menu extension transition
				List<Node> fadeNodes = new ArrayList<Node>();
				fadeNodes.add(dropdownTrigger);
				fadeNodes.add(about);
				fadeNodes.add(rotate);
				dropdownMenuState = STATE_EXTENDED;
				dropdownMenuTransition = new VerticalExtensionTransition(dropdownMenu, dropdownTrigger, fadeNodes,
						VerticalExtensionTransition.DIRECTION_RETRACT, dropdownMenu.getHeight());

				// Start the launchpad pane rotation transition
				launchpadCanvasRotationTransition = new LaunchpadCanvasRotationTransition(launchpadPane.getCanvas());

				// Create launchpad mode selector buttons
				setLPModes(lpEmulator.getModes(), lpEmulator.getSelectedModeIndex());

				// Let the launchpad canvas request focus
				launchpadPane.getCanvas().requestFocus();

				// Start the animation timer
				lpCanvasTimer.start();
			});

			// Create a scene
			Scene scene = new Scene(root, 500, 500);
			scene.getStylesheets().add(RESOURCE_FOLDER + "tanger/tanger.css");

			// Global key events
			scene.addEventFilter(KeyEvent.KEY_PRESSED, (event) -> {
				if (event.getCode() == KeyCode.F11) {
					primaryStage.setFullScreen(!primaryStage.isFullScreen());
					primaryStage.setAlwaysOnTop(false); // Maybe a bug? we have to re set always on
					primaryStage.setAlwaysOnTop(true); // top after toggeling fullscreen
					event.consume();
				}
			});

			/*
			 * ---- not really working ---- // Defocus application when mouse
			 * position is not on launchpad // canvas, focus when it is
			 * scene.setOnMouseMoved((e) -> { boolean isLPCanvasFocused =
			 * e.getTarget().getClass() == LaunchpadCanvas.class; if
			 * (isLPCanvasFocused && !wasLPCanvasFocused) { // Focus gained
			 * primaryStage.requestFocus(); } else if (!isLPCanvasFocused &&
			 * wasLPCanvasFocused) { // Focus lost primaryStage.toBack(); }
			 * wasLPCanvasFocused = isLPCanvasFocused; });
			 */

			// Show the stage
			primaryStage.setScene(scene);
			primaryStage.getIcons().add(new Image(
					getClass().getClassLoader().getResource("de/amonbenson/res/icons/favicon.png").toExternalForm()));
			primaryStage.setAlwaysOnTop(true);
			primaryStage.setMinWidth(100);
			primaryStage.setMinHeight(150);
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setLPModes(Mode[] modes, int selectedIndex) {
		modeSelectorBox.getChildren().clear();
		modeSelectorGroup = new ToggleGroup();

		Label label = new Label("Layout: ");
		label.setTooltip(new Tooltip("Select a button layout."));
		modeSelectorBox.getChildren().add(label);

		for (int i = 0; i < modes.length; i++) {
			ToggleButton b = new ToggleButton(modes[i].getName());
			b.setUserData(i);
			if (i == selectedIndex)
				b.setSelected(true);

			b.setToggleGroup(modeSelectorGroup);
			modeSelectorBox.getChildren().add(b);
		}

		modeSelectorGroup.selectedToggleProperty()
				.addListener((ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle) -> {
					if (modeSelectorGroup.getSelectedToggle() == null)
						modeSelectorGroup.selectToggle(oldToggle);

					int modeIndex = (int) modeSelectorGroup.getSelectedToggle().getUserData();
					lpEmulator.setSelectedModeIndex(modeIndex);
				});
	}

	private void setSelectedLaunchpadEmulator() {
		int index = launchpadSelectorBox.getSelectionModel().getSelectedIndex();
		if (index == -1)
			return;

		// Set the launchpad emulator type TODO: Add launchpad mk2 and pro
		// support
		if (index == 0) {
			launchpadPane.getCanvas().setCaseType(CaseType.TWO_ROWS);
			launchpadPane.getCanvas().setRenderSideLED(false);
			lpEmulator = new LaunchpadMiniEmulator();
		}
		if (index == 1) {
			launchpadPane.getCanvas().setCaseType(CaseType.TWO_ROWS);
			launchpadPane.getCanvas().setRenderSideLED(false);
			lpEmulator = new LaunchpadMK2Emulator();
		}
		if (index == 2) {
			launchpadPane.getCanvas().setCaseType(CaseType.FOUR_ROWS);
			launchpadPane.getCanvas().setRenderSideLED(true);
			lpEmulator = new LaunchpadProEmulator();
		}

		setLPModes(lpEmulator.getModes(), lpEmulator.getSelectedModeIndex());
		launchpadPane.getCanvas().render();
	}

	private void openSelectedInput() {
		int index = inputSelectorBox.getSelectionModel().getSelectedIndex();
		if (index == -1)
			return;

		// Open the selected midi device
		lpSystem.openInput(index);
	}

	private void openSelectedOutput() {
		int index = outputSelectorBox.getSelectionModel().getSelectedIndex();
		if (index == -1)
			return;

		// Open the selected midi device
		lpSystem.openOutput(index);

		// Update the title to match the output port
		Object item = outputSelectorBox.getSelectionModel().getSelectedItem();
		if (item != null)
			primaryStage.setTitle(TITLE + " -> " + item.toString());
	}

	public void rescanMidi() {
		// Rescan the launchpad system
		lpSystem.scan();

		// Update the input and output list
		inputSelectorBox.getItems().clear();
		inputSelectorBox.getItems().addAll(lpSystem.getInputDevicesAsString());

		outputSelectorBox.getItems().clear();
		outputSelectorBox.getItems().addAll(lpSystem.getOutputDevicesAsString());

		// Select dummy info value
		selectDummyValue();

		// Reset title
		primaryStage.setTitle(TITLE);
	}

	public void selectDummyValue() {
		// Selects a dummy value for info
		inputSelectorBox.getSelectionModel().select("MOSI1");//"Input");
		outputSelectorBox.getSelectionModel().select("MISO1");//"Output");
	}

	private void showAboutWindow() {
		Stage about = new Stage();
		about.setTitle("About");
		VBox root = new VBox();

		root.getChildren()
				.add(new Label("Virtual Launchpad\n"
						+ "This application emulates any type of Novation Launchpad (LP). It was intended\n"
						+ "to be used as a replacement for a second real LP when working on multi-LP-projects.\n"
						+ "The program is still in development. For further information and tutorials visit:\n"));
		Hyperlink hyp = new Hyperlink("amonbenson.de");
		hyp.setOnAction((ActionEvent event) -> {
			openWebsite("http://www.amonbenson.de");
		});
		root.getChildren().add(hyp);

		root.getChildren().add(new Separator());

		root.getChildren().add(
				new Label("Version: " + VERSION + "\n" + "Created and designed by: Amon Benson (SchlegelFlegel)\n"));

		Button ok = new Button("      OK      ");
		ok.setOnAction((ActionEvent event) -> about.close());
		root.getChildren().add(new StackPane(ok));

		Scene scene = new Scene(root);
		scene.getStylesheets().add(RESOURCE_FOLDER + "tanger/tanger.css");

		about.setResizable(false);
		about.setScene(scene);
		about.sizeToScene();
		about.initOwner(primaryStage);
		about.initModality(Modality.APPLICATION_MODAL);
		about.showAndWait();
	}

	private void openWebsite(String site) {
		try {
			Desktop.getDesktop().browse(new URL(site).toURI());
		} catch (Exception ex) {
			System.err.println("Couldn't open webpage.");
			ex.printStackTrace();
		}
	}

	private void extendMenu() {
		// Re-add the dropdown menu at the beginning
		dropdownMenuState = STATE_EXTENDED;
		if (!root.getChildren().contains(dropdownMenu))
			root.setTop(dropdownMenu);

		// Remove any listeners from the transition
		dropdownMenuTransition.setOnFinished(null);

		// Play the extend transition
		dropdownMenuTransition.setDirection(VerticalExtensionTransition.DIRECTION_EXTEND);
		dropdownMenuTransition.playFromStart();
	}

	private void retractMenu() {
		// Play the retract transition
		dropdownMenuTransition.setDirection(VerticalExtensionTransition.DIRECTION_RETRACT);
		dropdownMenuTransition.playFromStart();

		// Remove the dropdown menu when the transition has finished
		dropdownMenuTransition.setOnFinished((ActionEvent event) -> {
			root.getChildren().remove(dropdownMenu);
			dropdownMenuState = STATE_RETRACTED;
		});
	}

	public static void main(String[] args) {
		launch(args);
	}
}
