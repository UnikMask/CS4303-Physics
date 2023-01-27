package tankphysics.engine;

import processing.core.PImage;
import processing.core.PVector;

/**
 * Visual component to display an image. Takes the size of the visual component.
 */
public class Sprite extends VisualModel {
	PImage sprite;

	public void draw(GameObject camera) {
		PVector pos_min = new PVector(position.x - object.size.x / 2, position.y - object.size.y / 2).add(position)
				.sub(camera.position);
		PVector pos_max = new PVector(position.x + object.size.x / 2, position.y + object.size.y / 2).add(position)
				.sub(camera.position);
		pos_min.x *= ((float) displayWidth) / camera.size.x;
		pos_min.y *= ((float) displayHeight) / camera.size.x;
		pos_max.x *= ((float) displayWidth) / camera.size.x;
		pos_max.y *= ((float) displayHeight) / camera.size.x;

		if (pos_max.x >= 0 && pos_max.y >= 0 && pos_min.x <= displayWidth && pos_min.y <= displayHeight) {
			image(sprite, pos_min.x, pos_min.y, pos_max.x - pos_min.x, pos_max.y - pos_min.y);
		}
	}

	public Sprite(String fp) {
		sprite = loadImage(fp);
	}

}
