package tankphysics.engine;

import processing.core.PApplet;

public abstract class VisualModel implements Component {
	protected GameObject object;

	public void attach(GameObject object) {
		this.object = object;
	}

	public abstract void draw(PApplet sketch);

	public void draw(GameObject camera, PApplet sketch) {
		// sketch.pushMatrix();
		// sketch.translate(object.position.x - camera.position.x, object.position.y -
		// camera.position.y);
		// sketch.scale(((float) sketch.displayWidth) / camera.size.x, ((float)
		// sketch.displayHeight) / camera.size.y);
		draw(sketch);
		// sketch.popMatrix();
	}
}
