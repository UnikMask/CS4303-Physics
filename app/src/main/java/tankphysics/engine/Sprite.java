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
		PVector semiSize = PVector.div(object.size, 2.0f);
		System.out.println(semiSize.x);
		sketch.image(sprite, object.position.x - semiSize.x, object.position.y - semiSize.y,
				object.position.x + semiSize.x, object.position.y + semiSize.y);
	}

	/**
	 * Constructor for a sprite.
	 */
	public Sprite(String fp, PVector anchor, PApplet sketch) {
		this.anchor = anchor;
		this.fp = fp;
		sprite = sketch.loadImage(fp);
	}

}
