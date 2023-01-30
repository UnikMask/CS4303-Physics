package tankphysics.engine;

import processing.core.PApplet;
import processing.core.PVector;

public abstract class VisualModel implements Component {
	protected GameObject object;

	public void attach(GameObject object) {
		this.object = object;
	}

	public abstract void draw(PApplet sketch, PVector scale);

	public void draw(GameObject camera, PApplet sketch) {
		sketch.pushMatrix();
		PVector scale = new PVector(((float) sketch.displayWidth) / camera.size.x,
				((float) sketch.displayHeight) / camera.size.y);
		PVector semiSize = PVector.div(new PVector(sketch.displayWidth, sketch.displayHeight), 2);
		sketch.translate(scale.x * (semiSize.x - camera.getPosition().x),
				scale.y * (semiSize.y - camera.getPosition().y));
		draw(sketch, scale);
		sketch.popMatrix();
	}
}
