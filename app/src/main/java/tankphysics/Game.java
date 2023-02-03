package tankphysics;

import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PApplet;
import processing.core.PVector;
import tankphysics.engine.*;

public class Game extends PApplet {
	Director engineDirector;
	GameObject camera;

	public void setup() {
		engineDirector = new Director(this);
		camera = engineDirector.getCamera();

		// Set up vertices for blocks
		ArrayList<PVector> vertices = new ArrayList<>(
				Arrays.asList(new PVector(0, 1), new PVector(1, 1), new PVector(1, 0), new PVector(0, 0)));
		ArrayList<PVector> blockUv = new ArrayList<>(Arrays.asList(new PVector(0, 128 * 8),
				new PVector(128 * 8, 128 * 8), new PVector(128 * 8, 0), new PVector(0, 0)));

		// Make dirt block for show.
		GameObject dirtBlock = new GameObject(new PVector(512, 512),
				new PVector(displayWidth - 960, displayHeight - 540), false,
				new VisualPolygon(vertices, new PVector(256, 256), loadImage("dirt_block.png"), blockUv));

		// Make gravity-bound object.
		RigidBody bulletCPU = new RigidBody(18.7f, 0.2f);
		GameObject bullet = new GameObject(new PVector(64, 64), new PVector(128, displayHeight - 128), false,
				new Sprite("dirt_block.png", new PVector(32, 32)), bulletCPU);

		// Make a plane for collision checks.
		GameObject plane = new GameObject(new PVector(1800, 32), new PVector(960, displayHeight - 64), false,
				new VisualPolygon(vertices, new PVector(900, 16), color(255)),
				new CollisionMesh(new PVector(1800, 32), new PVector(900, 16), vertices, 1f));

		// Attach all components to director.
		engineDirector.attach(dirtBlock, plane, bullet);

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

		engineDirector.nextFrame();
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "tankphysics.Game" });
	}
}
