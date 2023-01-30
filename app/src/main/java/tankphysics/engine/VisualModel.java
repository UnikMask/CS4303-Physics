package tankphysics.engine;

import processing.core.PApplet;

public abstract class VisualModel implements Component {
	protected GameObject object;

	public void attach(GameObject object) {
		this.object = object;
	}

	public abstract void draw(PApplet sketch);

	public void draw(GameObject camera, PApplet sketch) {
		sketch.pushMatrix();
		//sketch.translate((camera.size.x / 2) - camera.position.x, (camera.size.y/2) - camera.position.y);
		System.out.println((camera.size.x / 2) - camera.position.x);
		System.out.println(-sketch.displayWidth/2);
		sketch.translate(-sketch.displayWidth/2, -sketch.displayHeight/2);
		//sketch.translate(0, 0);
		sketch.scale(((float) sketch.displayWidth) / camera.size.x, ((float) sketch.displayHeight) / camera.size.y);
		draw(sketch);
		sketch.popMatrix();
	}
}
