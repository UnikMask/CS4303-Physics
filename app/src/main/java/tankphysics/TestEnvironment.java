package tankphysics;

import processing.core.PApplet;
import processing.core.PVector;
import tankphysics.engine.Director;
import tankphysics.engine.GameObject;

public class TestEnvironment extends PApplet {
	GameObject particle;
	GameObject camera;
	Director testDirector;
	UIButton testButton = new UIButton(new PVector(0.3f, 0.3f), new PVector(0.2f, 0.2f), "test", null);

	public void setup() {
	}

	public void settings() {
		size(854, 480, PApplet.P2D);
	}

	public void draw() {
		background(0);
		// testDirector.nextFrame();
		testButton.draw(this, new PVector(mouseX, mouseY));
	}
}
