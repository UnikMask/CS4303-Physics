package tankphysics.engine;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 * Visual component to display an image. Takes the size of the visual component.
 */
public class Sprite extends VisualModel {
	PImage sprite;
	String fp;
	PVector anchor;

	public void draw(PApplet sketch, PVector scale) {
		if (sprite == null) {
			sprite = sketch.loadImage(fp);
		}
		PVector semiSize = PVector.div(object.size, 2);
		sketch.image(sprite, object.position.x - scale.x * anchor.x, object.position.y - scale.y * anchor.y,
				object.getSize().x * scale.x, object.getSize().y * scale.y);
	}

	/**
	 * Constructor for a sprite.
	 */
	public Sprite(String fp, PVector anchor) {
		this.anchor = anchor;
		this.fp = fp;
	}

}
