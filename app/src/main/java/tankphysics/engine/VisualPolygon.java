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
		// sketch.pushMatrix();
		// sketch.scale(scale.x, scale.y);
		reloadPShape(sketch);
		// sketch.shape(polygonShape, object.getPosition().x - scale.x * anchor.x,
		// object.getPosition().y - scale.y * anchor.y);
		// sketch.popMatrix();
	}

	/**
	 * Reload the saved shape.
	 */
	public void reloadPShape(PApplet sketch) {
		// Setup shape creation.
		sketch.beginShape();

		// Fill polygon with given fill.
		if (fillType == FillType.Colour) {
			sketch.fill(colour);
		} else if (fillType == FillType.Texture) {
			sketch.texture(sketch.loadImage("dirt_block.png"));
		}

		// Load all vertices into shape and close shape.
		for (PVector v : vertices) {
			sketch.vertex(object.getPosition().x + v.x * object.getSize().x - anchor.x,
					object.getPosition().y + v.y * object.getSize().y - anchor.y);
		}
		sketch.endShape(PApplet.CLOSE);
	}

	public void paintShape(PApplet sketch, PVector scale) {

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
