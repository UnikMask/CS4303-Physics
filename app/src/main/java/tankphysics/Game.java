package tankphysics;

import processing.core.PApplet;
import processing.core.PVector;
import tankphysics.engine.Director;
import tankphysics.engine.GameObject;
import tankphysics.engine.Sprite;

public class Game extends PApplet {
	Director engineDirector;
	GameObject camera;

	public void setup() {
		engineDirector = new Director();
		camera = engineDirector.getCamera();

		GameObject dirtBlock = new GameObject(new PVector(512, 512),
				new PVector(displayWidth - 256, displayHeight - 256));
		// dirtBlock.attach(new Sprite("texture_dirt-1.png.png", new PVector(0, 0)));
		engineDirector.attach(dirtBlock);
	}

	public void settings() {
		size(1920, 1080);
		fullScreen();
	}

	public void draw() {
		background(0);
		engineDirector.draw();
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "tankphysics.Game" });
	}
}
