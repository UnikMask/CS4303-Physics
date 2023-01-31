package tankphysics;

import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PApplet;
import processing.core.PVector;
import tankphysics.engine.Director;
import tankphysics.engine.GameObject;
import tankphysics.engine.RigidBody;
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
		ArrayList<PVector> blockUv = new ArrayList<>(Arrays.asList(new PVector(0, 128 * 8),
				new PVector(128 * 8, 128 * 8), new PVector(128 * 8, 0), new PVector(0, 0)));

		// Make dirt block for show.
		GameObject dirtBlock = new GameObject(new PVector(512, 512),
				new PVector(displayWidth - 960, displayHeight - 540));
		VisualModel blockViz = new VisualPolygon(vertices, new PVector(256, 256), loadImage("dirt_block.png"), blockUv);

		// Make gravity-bound object.
		GameObject bullet = new GameObject(new PVector(64, 64), new PVector(128, displayHeight - 128));
		RigidBody bulletCPU = new RigidBody(18.7f, 0.2f);
		VisualModel bulletVisual = new Sprite("dirt_block.png", new PVector(32, 32));

		// Attach all components to director.
		dirtBlock.attach(blockViz);
		bullet.attach(bulletCPU);
		bullet.attach(bulletVisual);
		engineDirector.attach(dirtBlock);
		engineDirector.attach(bullet);

		// Give initial velocity to bullet.
		bulletCPU.setVelocity(new PVector(10, -10));
	}

	public void settings() {
		size(1920, 1080, PApplet.P2D);
		fullScreen();
	}

	public void draw() {
		background(0);

		if (camera.getSize().x < displayWidth) {
			camera.getSize().add(new PVector(displayWidth / 180, displayHeight / 180));
		}

		engineDirector.draw();
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "tankphysics.Game" });
	}
}
