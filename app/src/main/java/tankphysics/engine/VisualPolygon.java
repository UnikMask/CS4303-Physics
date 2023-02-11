package tankphysics.engine;

import java.util.List;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;

public class VisualPolygon extends VisualModel {
	PShape polygonShape;
	List<PVector> vertices;
	PVector anchor;
	FillType fillType;

	// colour/texture for polygon
	Integer colour;
	PImage texture;
	List<PVector> uv;

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
		sketch.pushMatrix();
		sketch.translate(object.getPosition().x - anchor.x, object.getPosition().y - anchor.y);
		sketch.rotate(object.getRotation());
		sketch.textureWrap(PApplet.REPEAT);
		sketch.shape(polygonShape, 0, 0);
		sketch.popMatrix();
	}

	/**
	 * Reload the saved shape.
	 */
	public void reloadPShape(PApplet sketch) {
		// Setup shape creation.
		polygonShape = sketch.createShape();
		polygonShape.beginShape();
		polygonShape.noStroke();

		// Fill polygon with given fill.
		if (fillType == FillType.Colour) {
			polygonShape.fill(colour);
			for (PVector v : vertices) {
				polygonShape.vertex(v.x, v.y);
			}
		} else if (fillType == FillType.Texture) {
			polygonShape.textureMode(PApplet.IMAGE);
			polygonShape.texture(texture);
			for (int i = 0; i < uv.size(); i++) {
				polygonShape.vertex(vertices.get(i).x, vertices.get(i).y, uv.get(i).x, uv.get(i).y);
			}
		}

		// Load all vertices into shape and close shape.
		polygonShape.endShape();
	}

	//////////////////
	// Constructors //
	//////////////////

	VisualPolygon(PVector anchor, List<PVector> vertices) {
		this.vertices = vertices;
		this.anchor = anchor;
	}

	public VisualPolygon(PVector anchor, List<PVector> vertices, Integer colour) {
		this(anchor, vertices);
		fillType = FillType.Colour;
		this.colour = colour;
	}

	public VisualPolygon(PVector anchor, List<PVector> vertices, PImage texture, List<PVector> uv) {
		this(anchor, vertices);
		fillType = FillType.Texture;
		this.texture = texture;
		this.uv = uv;
	}
}
