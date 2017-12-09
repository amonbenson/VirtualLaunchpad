package de.amonbenson.vlp;

import java.awt.Point;
import java.util.ArrayList;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;

public class LaunchpadCanvas extends Canvas {

	public enum CaseType {
		TWO_ROWS, FOUR_ROWS
	};

	public static final double DEFAULT_SIZE = 400, CASE_ARC = 0.27, PAD_ARC = 0.08, PAD_FACT = 0.83, ROUND_FACT = 0.65;
	public static final Color CASE_COLOR = Color.rgb(30, 30, 30);
	public static final Color PAD_COLOR = Color.rgb(90, 90, 90);

	private Color[][] pads;

	private boolean multiSelectionEnabled;
	private ArrayList<Point> multiSelections;
	
	private LaunchpadCanvasListener listener;
	private int lastX, lastY;
	
	private CaseType caseType;

	public LaunchpadCanvas() {
		this(DEFAULT_SIZE);
	}

	public LaunchpadCanvas(double size) {
		super(size, size);

		// Detect a change in size
		widthProperty().addListener(evt -> render());
		heightProperty().addListener(evt -> render());
		
		// We need this to catch all key events
		addEventFilter(MouseEvent.ANY, (e) -> requestFocus());

		// Set mouse listeners
		setOnMousePressed((MouseEvent event) -> mousePressed(event));
		setOnMouseReleased((MouseEvent event) -> mouseReleased(event));
		setOnMouseDragged((MouseEvent event) -> mouseDragged(event));

		// Set key listeners
		setOnKeyPressed((KeyEvent event) -> keyPressed(event));
		setOnKeyReleased((KeyEvent event) -> keyReleased(event));

		// Multi Selection will be activated when holding down the shift key
		multiSelectionEnabled = false;
		multiSelections = new ArrayList<Point>();
		
		listener = null;
		lastX = -1;
		lastY = -1;
		
		caseType = CaseType.FOUR_ROWS;

		reset();
		render();
	}

	public Color getPad(int x, int y) {
		if (pads[x][y] == null)
			return Color.BLACK;
		return pads[x][y];
	}

	public void setPad(int x, int y, Color color) {
		if (color == null)
			color = Color.BLACK;
		pads[x][y] = color;
	}

	public void setPads(Color[][] pads) {
		this.pads = pads;
	}

	public void reset() {
		pads = new Color[10][10];
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				setPad(x, y, Color.BLACK);
			}
		}
	}

	public void render() {
		renderTo(getGraphicsContext2D());
	}

	public void renderTo(GraphicsContext g) {
		// Clear
		g.clearRect(0, 0, getWidth(), getHeight());

		// Render case
		g.setFill(CASE_COLOR);
		g.fillRoundRect(0, 0, getWidth(), getHeight(), CASE_ARC * getWidth() / 4, CASE_ARC * getHeight() / 4);

		// Render pads
		double padSize;
		if (caseType == CaseType.TWO_ROWS) padSize = getWidth() / 10;
		else padSize = getWidth() / 11;
		
		g.setStroke(Color.WHITE);
		g.setLineWidth(2);
		
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				// Null-pads are not allowed
				if (pads[x][y] == null)
					pads[x][y] = Color.BLACK;

				g.setFill(getPadColor(pads[x][y]));
				
				if (x == 0 || x == 9 || y == 0 || y == 9) {
					// continue if we have one of the corners or a left / bottom row in two row case type
					if (x == y || 9 - x == y || ((x == 0 || y == 9) && caseType == CaseType.TWO_ROWS))
						continue;

					// Draw round buttons
					double xOff = caseType == CaseType.TWO_ROWS ? 0 : 1;
					double posX = (xOff + x - ROUND_FACT / 2) * padSize;
					double posY = (1 + y - ROUND_FACT / 2) * padSize;

					g.fillOval(posX, posY, padSize * ROUND_FACT, padSize * ROUND_FACT);
					
					if (isMultiSelected(x, y)) {
						g.strokeOval(posX, posY, padSize * ROUND_FACT, padSize * ROUND_FACT);
					}

				} else {

					// Draw square buttons
					double xOff = caseType == CaseType.TWO_ROWS ? 0 : 1;
					double posX = (xOff + x - PAD_FACT / 2) * padSize;
					double posY = (1 + y - PAD_FACT / 2) * padSize;

					g.fillRoundRect(posX, posY, padSize * PAD_FACT, padSize * PAD_FACT, PAD_ARC * getWidth() / 4,
							PAD_ARC * getHeight() / 4);
					
					if (isMultiSelected(x, y)) {
						g.strokeRoundRect(posX, posY, padSize * PAD_FACT, padSize * PAD_FACT, PAD_ARC * getWidth() / 4,
								PAD_ARC * getHeight() / 4);
					}
				}
			}
		}

		// Render the circle in the middle
		double circleR = getWidth() * 0.05;
		double circleX, circleY;
		if (caseType == CaseType.TWO_ROWS) {
			circleX = getWidth() / 10 * 4.5 - circleR / 2;
			circleY = getHeight() / 10 * 5.5 - circleR / 2;
		} else {
			circleX = (getWidth() - circleR) / 2;
			circleY = (getHeight() - circleR) / 2;
		}
		
		g.setFill(CASE_COLOR);
		g.fillOval(circleX, circleY, circleR, circleR);
	}
	
	private void beginMultiSelection() {
		if (multiSelectionEnabled) return;
		
		multiSelections.clear();
		multiSelectionEnabled = true;
	}
	
	private void endMultiSelection() {
		multiSelectionEnabled = false;
	}
	
	private boolean isMultiSelected(int x, int y) {
		if (!multiSelectionEnabled) return false;
		
		for (Point p : multiSelections) {
			if ((int) p.getX() == x && (int) p.getY() == y) return true;
		}
		
		return false;
	}

	private Point2D getPadPosition(double x, double y) {
		if (caseType == CaseType.TWO_ROWS) {
			
			int xp = (int) (x / getWidth() * 10 - 0.5);
			int yp = (int) (y / getHeight() * 10 - 0.5);
	
			if (xp < 0 || xp > 8 || yp < 0 || yp > 8)
				return null; // Out of range
			if (xp == 8 && yp == 0)
				return null; // Top right corner
			
			return new Point2D(xp + 1, yp);
			
		} else {
			
			int xp = (int) (x / getWidth() * 11 - 0.5);
			int yp = (int) (y / getHeight() * 11 - 0.5);
	
			if (xp < 0 || xp > 9 || yp < 0 || yp > 9)
				return null; // Out of range
			if (xp == 0 && yp == 0 || xp == 9 && yp == 9 || xp == 0 && yp == 9 || xp == 9 && yp == 0)
				return null; // Corners
			
			return new Point2D(xp, yp);
			
		}
	}

	private void mousePressed(MouseEvent event) {
		// If we have no listener, we don't have to catch any mouse events
		if (listener == null)
			return;

		// Clicked on launchpad
		if (contains(event.getX(), event.getY()) && event.getButton() == MouseButton.PRIMARY) {
			Point2D p = getPadPosition(event.getX(), event.getY());
			if (p == null)
				return;

			int x = (int) p.getX(), y = (int) p.getY();

			// Keep track of the pressed buttons if multi-seleciton is enabled
			if (multiSelectionEnabled) {
				multiSelections.add(new Point(x, y));
				render();
			}
			listener.padAction(x, y, true);

			lastX = x;
			lastY = y;
		}
	}

	private void mouseReleased(MouseEvent event) {
		// If we have no listener, we don't have to catch any mouse events
		if (listener == null)
			return;
		
		// If we have a multi selection, we ignore release events
		if (multiSelectionEnabled)
			return;

		// Clicked on launchpad
		if (contains(event.getX(), event.getY()) && event.getButton() == MouseButton.PRIMARY) {
			Point2D p = getPadPosition(event.getX(), event.getY());
			if (p == null)
				return;

			int x = (int) p.getX(), y = (int) p.getY();
			listener.padAction(x, y, false);

			lastX = x;
			lastY = y;
		}
	}

	private void mouseDragged(MouseEvent event) {
		// If we have no listener, we don't have to catch any mouse events
		if (listener == null)
			return;

		// Clicked on launchpad
		if (contains(event.getX(), event.getY()) && event.getButton() == MouseButton.PRIMARY) {
			Point2D p = getPadPosition(event.getX(), event.getY());
			if (p == null)
				return;

			int x = (int) p.getX(), y = (int) p.getY();

			// We only want to update, if the mouse has moved to a new pad
			if ((lastX == x && lastY == y) || lastX == -1 || lastY == -1)
				return;

			// Release previous button (if not multi-selection)
			if (!multiSelectionEnabled) listener.padAction(lastX, lastY, false);
			
			// Keep track of the pressed buttons if multi-seleciton is enabled
			if (multiSelectionEnabled) {
				multiSelections.add(new Point(x, y));
				render();
			}
			listener.padAction(x, y, true);

			lastX = x;
			lastY = y;
		}
	}
	
	private void keyPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.SHIFT) {
			// Begin multi selection
			beginMultiSelection();
		}
	}
	
	private void keyReleased(KeyEvent event) {
		if (event.getCode() == KeyCode.SHIFT) {
			// End multi selection and "release" all keys
			endMultiSelection();
			
			if (listener != null) {
				for (Point point : multiSelections) {
					listener.padAction((int) point.getX(), (int) point.getY(), false);
				}
			}

			render();
		}
	}

	public LaunchpadCanvasListener getLaunchpadCanvasListener() {
		return listener;
	}

	public void setLaunchpadCanvasListener(LaunchpadCanvasListener listener) {
		this.listener = listener;
	}

	public CaseType getCaseType() {
		return caseType;
	}

	public void setCaseType(CaseType caseType) {
		this.caseType = caseType;
	}

	private Color getPadColor(Color col) {
		return Color.rgb((int) (PAD_COLOR.getRed() * 255 + col.getRed() * 255 * (1 - PAD_COLOR.getRed())),
				(int) (PAD_COLOR.getGreen() * 255 + col.getGreen() * 255 * (1 - PAD_COLOR.getGreen())),
				(int) (PAD_COLOR.getBlue() * 255 + col.getBlue() * 255 * (1 - PAD_COLOR.getBlue()))).saturate();
	}

	private double mix(double x, double y, double f) {
		return x + f * (y - x);
	}
}
