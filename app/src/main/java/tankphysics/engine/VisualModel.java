package tankphysics.engine;

import processing.core.PApplet;
import processing.core.PVector;

public abstract class VisualModel implements Component {
	protected GameObject object;

	public void attach(GameObject object) {
		this.object = object;
	}

	public GameObject getObject() {
		return object;
	}

	/**
	 * Draw the visual object onto the screen.
	 *
	 * @param sketch The graphical object to draw with.
	 */
	public abstract void draw(PApplet sketch);

	/**
	 * Draw the visual model based on the given camera.
	 *
	 * @param camera The camera object to translate visual coordinates with.
	 * @param sketch The graphical object to draw with.
	 */
	public void draw(GameObject camera, PApplet sketch) {
		sketch.pushMatrix();
		PVector scale = new PVector(((float) sketch.width) / camera.getSize().x,
				((float) sketch.height) / camera.getSize().y);
		PVector anchoredPos = PVector.sub(camera.getPosition(), PVector.div(camera.getSize(), 2));
		sketch.scale(scale.x, scale.y);
		sketch.translate(-anchoredPos.x, -anchoredPos.y);
		draw(sketch);
		sketch.popMatrix();
	}
}
