package tankphysics.engine;

import processing.core.PApplet;
import processing.core.PVector;

public abstract class VisualModel implements Component {
	protected GameObject object;

	public void attach(GameObject object) {
		this.object = object;
	}

	public abstract void draw(PApplet sketch);

	public void draw(GameObject camera, PApplet sketch) {
		sketch.pushMatrix();
		PVector scale = new PVector(((float) sketch.displayWidth) / camera.size.x,
				((float) sketch.displayHeight) / camera.size.y);
		PVector semiSize = PVector.div(camera.getSize(), 2);
		PVector anchoredPos = PVector.sub(camera.getPosition(), semiSize);
		sketch.scale(scale.x, scale.y);
		sketch.translate(-anchoredPos.x, -anchoredPos.y);
		draw(sketch);
		sketch.popMatrix();
	}
}
