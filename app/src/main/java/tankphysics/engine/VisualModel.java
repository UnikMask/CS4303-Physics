package tankphysics.engine;

import processing.core.PApplet;
import processing.core.PVector;

public abstract class VisualModel extends PApplet implements Component {
	protected GameObject object;
	protected PVector position;

	public void attach(GameObject object) {
		this.object = object;
		this.position = object.getPosition();
	}

	/**
	 * Draw the visual model.
	 *
	 * @param camera The camera to interpolate positions from.
	 */
	public abstract void draw(GameObject camera);

	/**
	 * Draw with no camera attached.
	 */
	public void draw() {
		draw(new GameObject(new PVector(displayWidth, displayHeight)));
	}
}
