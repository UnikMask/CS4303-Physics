package tankphysics.engine;

import processing.core.PImage;
import processing.core.PVector;

/**
 * Visual component to display an image. Takes the size of the visual component.
 */
public class Sprite extends VisualModel {
	PImage sprite;
	PVector anchor;

	public void draw() {
		PVector semiSize = PVector.div(object.size, 2.0f);
		image(sprite, object.position.x - semiSize.x, object.position.y - semiSize.y, object.position.x + semiSize.x,
				object.position.y + semiSize.y);
	}

	/**
	 * Constructor for a sprite.
	 */
	public Sprite(String fp, PVector anchor) {
		sprite = loadImage(fp);
		this.anchor = anchor;
	}

}
