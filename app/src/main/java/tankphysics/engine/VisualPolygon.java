package tankphysics.engine;

import java.util.ArrayList;

import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;

public class VisualPolygon extends VisualModel {
	PShape polygonShape;
	ArrayList<PVector> vertices;
	PVector anchor;
	FillType fillType;

	// colour/texture for polygon
	Integer colour;
	PImage texture;

	/**
	 * Enum determining whether a texture or a colour will fill a polygon.
	 */
	public static enum FillType {
		Colour, Texture
	}

	public void draw() {
		shape(polygonShape, object.getPosition().x, object.getPosition().y);
	}

	/**
	 * Reload the saved shape.
	 */
	public void reloadPShape() {
		// Setup shape creation.
		polygonShape = createShape();
		polygonShape.beginShape();
		polygonShape.translate(anchor.x, anchor.y);

		// Fill polygon with given fill.
		if (fillType == FillType.Colour) {
			polygonShape.fill(colour);
		} else if (fillType == FillType.Texture) {
			polygonShape.texture(texture);
		}

		// Load all vertices into shape and close shape.
		for (PVector v : vertices) {
			polygonShape.vertex(v.x, v.y);
		}
		polygonShape.endShape(CLOSE);
	}

	//////////////////
	// Constructors //
	//////////////////

	VisualPolygon(ArrayList<PVector> vertices, PVector anchor) {
		this.vertices = vertices;
		this.anchor = anchor;
	}

	public VisualPolygon(ArrayList<PVector> vertices, PVector anchor, Integer colour) {
		this(vertices, anchor);
		fillType = FillType.Colour;
		this.colour = colour;
		reloadPShape();
	}

	public VisualPolygon(ArrayList<PVector> vertices, PVector anchor, PImage texture) {
		this(vertices, anchor);
		fillType = FillType.Texture;
		this.texture = texture;
		reloadPShape();
	}
}
