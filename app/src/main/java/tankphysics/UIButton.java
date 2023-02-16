package tankphysics;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;
import tankphysics.engine.EngineEventListener;
import tankphysics.engine.Polygons;

/** Represents a button on the game menus. */
public class UIButton {
	// Constants
	private static final int normalBgGray = 32;
	private static final int hoveredBgGray = 200;
	private static final int normalFgGray = 255;
	private static final int hoveredFgGray = 28;
	private static final float ANIM_LERP_COEFF = 0.2f;

	private List<PVector> vertices = new ArrayList<>();
	private String text;
	private PVector position;
	private PVector size;
	private EngineEventListener onClickEvent;
	private int currentBgGray = normalBgGray;
	private int currentFgGray = normalFgGray;

	public void draw(PApplet sketch, PVector realMousePosition) {

		PVector scaledMousePosition = new PVector(realMousePosition.x / sketch.width,
				realMousePosition.y / sketch.height);

		if (isHovered(scaledMousePosition)) {
			currentBgGray = (int) PApplet.lerp(currentBgGray, hoveredBgGray, ANIM_LERP_COEFF);
			currentFgGray = (int) PApplet.lerp(currentFgGray, hoveredFgGray, ANIM_LERP_COEFF);
		} else {
			currentBgGray = (int) PApplet.lerp(currentBgGray, normalBgGray, ANIM_LERP_COEFF);
			currentFgGray = (int) PApplet.lerp(currentFgGray, normalFgGray, ANIM_LERP_COEFF);
		}

		// Draw button

		PShape shape = sketch.createShape();
		shape.beginShape();
		shape.fill(currentBgGray);
		for (PVector v : vertices) {
			shape.vertex((v.x + position.x) * sketch.width, (v.y + position.y) * sketch.height);
		}
		shape.endShape(PApplet.CLOSE);

		sketch.pushMatrix();
		// sketch.scale((float) sketch.width, (float) sketch.height);
		// sketch.translate(position.x, position.y);
		sketch.shape(shape, 0, 0);
		sketch.popMatrix();

		// Draw button text

		sketch.pushStyle();
		float textSize = (size.y * sketch.height / 2);
		sketch.fill(sketch.color(currentFgGray));
		sketch.textSize(textSize);
		sketch.textAlign(PApplet.CENTER, PApplet.CENTER);
		sketch.text(text, (position.x + size.x / 2) * sketch.width,
				-textSize / 4 + (position.y + size.y / 2) * sketch.height);
		sketch.popStyle();

	}

	/**
	 * Check if a button is being hovered.
	 */
	public boolean isHovered(PVector adaptedMousePosition) {
		PVector dist = PVector.sub(adaptedMousePosition, position);
		return dist.x > 0 && dist.x < size.x && dist.y > 0 && dist.y < size.y;
	}

	public void generateButtonVertices() {
		vertices = Polygons.makeSquare(size, new PVector());
	}

	/**
	 * Call the onClick event of the button.
	 */
	public void callOnClickEvent(PVector adaptedMousePosition) {
		if (onClickEvent != null && isHovered(adaptedMousePosition)) {
			onClickEvent.call(null);
		}
	}

	/**
	 * Constructor for a UIButton object.
	 *
	 * @param position     The position of the button. Anchor is up-left corner of
	 *                     the button.
	 * @param size         The size of the button. Determinines it's vertices, the
	 *                     text size, and the range at which it'll be hovered.
	 * @param text         The text in the button.
	 * @param onClickEvent The event that happens if the button is clicked.
	 */
	public UIButton(PVector position, PVector size, String text, EngineEventListener onClickEvent) {
		this.position = position;
		this.size = size;
		this.text = text;
		this.onClickEvent = onClickEvent;
		generateButtonVertices();
	}
}
