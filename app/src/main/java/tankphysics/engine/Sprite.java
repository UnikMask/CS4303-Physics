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

	public void draw(PApplet sketch) {
		if (sprite == null) {
			sprite = sketch.loadImage(fp);
		}
		sketch.image(sprite, object.getPosition().x - anchor.x, object.getPosition().y - anchor.y, object.getSize().x,
				object.getSize().y);
	}

	/**
	 * Constructor for a sprite.
	 */
	public Sprite(String fp, PVector anchor) {
		this.anchor = anchor;
		this.fp = fp;
	}

}
