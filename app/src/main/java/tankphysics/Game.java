package tankphysics;

import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PApplet;
import processing.core.PVector;
import tankphysics.engine.Director;
import tankphysics.engine.GameObject;
import tankphysics.engine.Sprite;
import tankphysics.engine.VisualModel;
import tankphysics.engine.VisualPolygon;

public class Game extends PApplet {
	Director engineDirector;
	GameObject camera;

	public void setup() {
		engineDirector = new Director(this);
		camera = engineDirector.getCamera();

		ArrayList<PVector> vertices = new ArrayList<>(
				Arrays.asList(new PVector(0, 1), new PVector(1, 1), new PVector(1, 0), new PVector(0, 0)));
		GameObject dirtBlock = new GameObject(new PVector(512, 512),
				new PVector(displayWidth - 960, displayHeight - 540));
		VisualModel blockViz = new VisualPolygon(vertices, new PVector(256, 256), color(255, 0, 0));
		// Sprite blockViz = new Sprite("dirt_block.png", new PVector(256, 256));
		dirtBlock.attach(blockViz);
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
