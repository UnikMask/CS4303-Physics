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
		sketch.textureWrap(PApplet.REPEAT);
		sketch.shape(polygonShape, object.getPosition().x - anchor.x, object.getPosition().y - anchor.y);
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
				polygonShape.vertex(v.x * object.getSize().x, v.y * object.getSize().y);
			}
		} else if (fillType == FillType.Texture) {
			polygonShape.textureMode(PApplet.IMAGE);
			polygonShape.texture(texture);
			for (int i = 0; i < uv.size(); i++) {
				polygonShape.vertex(vertices.get(i).x * object.getSize().x, vertices.get(i).y * object.getSize().y,
						uv.get(i).x, uv.get(i).y);
			}
		}

		// Load all vertices into shape and close shape.
		polygonShape.endShape();
	}

	//////////////////
	// Constructors //
	//////////////////

	VisualPolygon(List<PVector> vertices, PVector anchor) {
		this.vertices = vertices;
		this.anchor = anchor;
	}

	public VisualPolygon(List<PVector> vertices, PVector anchor, Integer colour) {
		this(vertices, anchor);
		fillType = FillType.Colour;
		this.colour = colour;
	}

	public VisualPolygon(List<PVector> vertices, PVector anchor, PImage texture, List<PVector> uv) {
		this(vertices, anchor);
		fillType = FillType.Texture;
		this.texture = texture;
		this.uv = uv;
	}
}
