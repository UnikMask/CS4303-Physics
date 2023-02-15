package tankphysics;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import processing.core.PApplet;
import processing.core.PVector;

public class HealthBar {
	private static final float EQ_TRESHOLD = 0.0001f;
	private static final float TEXT_SIZE = 0.1f;
	private final List<PVector> vertices = new ArrayList<>(Arrays.asList(new PVector(0, 0), new PVector(0.2f, -0.05f),
			new PVector(0.175f, 0.1f), new PVector(0, 0.1f)));
	private float percentage;
	private int bgColor;
	private int fgColor;
	private PVector position;

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public int getBgColor() {
		return bgColor;
	}

	public void setBgColor(int color) {
		this.bgColor = color;
	}

	public int getFgColor() {
		return fgColor;
	}

	public void setFgColor(int fgColor) {
		this.fgColor = fgColor;
	}

	public float getPercentage() {
		return percentage;
	}

	public void setPercentage(float percentage) {
		this.percentage = percentage;
	}
	///////////////////////
	// Interface Methods //
	///////////////////////

	public void draw(PApplet sketch) {
		// Draw the health bar background
		sketch.pushMatrix();
		sketch.fill(bgColor);
		sketch.noStroke();
		sketch.smooth();
		sketch.beginShape();
		for (PVector v : vertices) {
			sketch.vertex(sketch.width * (position.x + v.x), sketch.height * (position.y + v.y));
		}
		sketch.endShape(PApplet.CLOSE);

		// Draw the health bar text
		sketch.textSize(TEXT_SIZE * sketch.width);
		PVector textPosition = new PVector(sketch.width * (position.x + 0.01f), sketch.height * (position.y + 0.03f));
		String percentageStr = new DecimalFormat("#").format(percentage);
		sketch.text(percentageStr + " %", textPosition.x, textPosition.y);
		sketch.popMatrix();
	}

	public void setPosition(PVector position) {
		this.position = position;
	}

	public PVector getPosition() {
		return position;
	}

	public List<UIObject> getContainingObjects() {
		return new ArrayList<>();
	}

	public HealthBar(PVector position, int color) {
		this.position = position;
		this.bgColor = color;
	}
}
