package tankphysics;

import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PApplet;
import processing.core.PVector;
import tankphysics.engine.Director;
import tankphysics.engine.GameObject;
import tankphysics.engine.VisualPolygon;

public class Game extends PApplet {
	Director engineDirector;
	GameObject camera;

	public void setup() {
		frameRate(60);
		engineDirector = new Director(this);
		camera = engineDirector.getCamera();

		GameObject dirtBlock = new GameObject(new PVector(512, 512),
				new PVector(displayWidth - 960, displayHeight - 540));
		ArrayList<PVector> vertices = new ArrayList<>(
				Arrays.asList(new PVector(0, 1), new PVector(1, 1), new PVector(1, 0), new PVector(0, 0)));

		VisualPolygon blockTex = new VisualPolygon(vertices, PVector.div(dirtBlock.getSize(), 2),
				loadImage("dirt_block.png"));
		dirtBlock.attach(blockTex);
		engineDirector.attach(dirtBlock);
	}

	public void settings() {
		size(1920, 1080, PApplet.P2D);
	}

	public void draw() {
		background(0);
		engineDirector.draw();
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "tankphysics.Game" });
	}
}
