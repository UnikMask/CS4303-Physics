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
		frameRate(60);
		engineDirector = new Director(this);
		camera = engineDirector.getCamera();

		// Set up vertices for blocks
		ArrayList<PVector> vertices = new ArrayList<>(
				Arrays.asList(new PVector(0, 1), new PVector(1, 1), new PVector(1, 0), new PVector(0, 0)));

		// Make gravity-bound object.
		ArrayList<PVector> bulletVertices = new ArrayList<>(
				Arrays.asList(new PVector(-32, 32), new PVector(32, 32), new PVector(32, -32), new PVector(-32, -32)));
		RigidBody bulletCPU = new RigidBody(18.7f, 0.2f);
		CollisionMesh bulletMesh = new CollisionMesh(new PVector(), bulletVertices, 0.3f);
		GameObject bullet = new GameObject(new PVector(64, 64), new PVector(120, displayHeight - 180), false,
				new Sprite("dirt_block.png", new PVector(32, 32)), bulletCPU, bulletMesh);
		bulletCPU.attachToHitbox(bulletMesh);

		// Make a plane for collision checks.
		ArrayList<PVector> planeVertices = new ArrayList<>(Arrays.asList(new PVector(-900, 16), new PVector(900, 16),
				new PVector(900, -16), new PVector(-900, -16)));
		GameObject plane = new GameObject(new PVector(1800, 32), new PVector(960, displayHeight - 16), false,
				new VisualPolygon(vertices, new PVector(900, 16), color(255)),
				new CollisionMesh(new PVector(), planeVertices, 0.3f));

		// Attach all components to director.
		engineDirector.attach(plane, bullet);

		// Give initial velocity to bullet.
		bulletCPU.setVelocity(new PVector(2, -10));
	}

	public void settings() {
		size(1920, 1080, PApplet.P2D);
		fullScreen();
	}

	public void draw() {
		background(0);

		engineDirector.nextFrame();
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "tankphysics.Game" });
	}
}
