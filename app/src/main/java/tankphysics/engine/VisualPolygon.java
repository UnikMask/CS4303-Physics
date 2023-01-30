package tankphysics.engine;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PGraphics;
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

	public void draw(PApplet sketch) {
		if (polygonShape == null) {
			reloadPShape(sketch);
		}
		sketch.shape(polygonShape, object.getPosition().x, object.getPosition().y);
	}

	/**
	 * Reload the saved shape.
	 */
	public void reloadPShape(PApplet sketch) {
		// Setup shape creation.
		polygonShape = sketch.createShape();
		polygonShape.beginShape();

		// Fill polygon with given fill.
		if (fillType == FillType.Colour) {
			polygonShape.fill(colour);
		} else if (fillType == FillType.Texture) {
			polygonShape.texture(sketch.loadImage("dirt_block.png"));
		}

		// Load all vertices into shape and close shape.
		polygonShape.translate(-anchor.x, -anchor.y);
		for (PVector v : vertices) {
			polygonShape.vertex(v.x * object.getSize().x, v.y * object.getSize().y);
		}
		polygonShape.endShape(PApplet.CLOSE);
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
	}

	public VisualPolygon(ArrayList<PVector> vertices, PVector anchor, PImage texture) {
		this(vertices, anchor);
		fillType = FillType.Texture;
		this.texture = texture;
	}
}
