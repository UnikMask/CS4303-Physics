package tankphysics.engine;

import processing.core.PApplet;

public abstract class VisualModel extends PApplet implements Component {
	protected GameObject object;

	public void attach(GameObject object) {
		this.object = object;
	}

	public abstract void draw();

	public void draw(GameObject camera) {
		pushMatrix();
		translate(object.position.x - camera.position.x, object.position.y - camera.position.y);
		scale(((float) displayWidth) / camera.size.x, ((float) displayHeight) / camera.size.y);
		draw();
		popMatrix();
	}
}
