package tankphysics;

import java.util.Map;

import processing.core.PApplet;
import processing.core.PVector;
import tankphysics.engine.*;

public class Game extends PApplet {
	Director engineDirector;
	GameObject camera;
	Tank tank;

	public void setup() {
		frameRate(144);
		engineDirector = new Director(this);
		camera = engineDirector.getCamera();

		// Make a plane for collision checks.
		GameObject plane = new GameObject(new PVector(28.125f, 0.5f), new PVector(15, 16.625f), false,
				new VisualPolygon(new PVector(), Polygons.makeSquare(new PVector(28.125f, 0.5f)), color(255)),
				new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(28.125f, 0.5f)),
						Map.ofEntries(Map.entry("staticFriction", 0.6f), Map.entry("dynamicFriction", 0.5f),
								Map.entry("bounciness", 0.3f))));
		tank = new Tank(new PVector(3, 14), color(255, 0, 0));
		GameObject[] boxGrid = new GameObject[9];
		for (int i = 0; i < 9; i++) {
			boxGrid[i] = new Box(new PVector(15 + i / 3, 15 - i % 3), this);
		}

		// Attach all components to director.
		engineDirector.attach(tank, plane);
		engineDirector.attach(boxGrid);

		// Set initial camera position and zoom
		camera.setPosition(tank.getPosition());
		camera.setSize(new PVector(32, 18));
	}

	public void settings() {
		size(1920, 1080, PApplet.P2D);
		fullScreen();
	}

	public void keyReleased() {
		if (key == 'w') {
			engineDirector.togglePause();
		}
	}

	public void keyPressed() {
		if (key == 'd') {
			tank.getRigidBody().setVelocity(PVector.add(tank.getRigidBody().getVelocity(), new PVector(1, 0)));
		}
		if (key == 'a') {
			tank.getRigidBody().setVelocity(PVector.add(tank.getRigidBody().getVelocity(), new PVector(-1, 0)));
		}
	}

	public void draw() {
		background(0);

		camera.setPosition(new PVector(lerp(camera.getPosition().x, tank.getPosition().x, 0.02f),
				lerp(camera.getPosition().y, tank.getPosition().y, 0.02f)));
		engineDirector.nextFrame();
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "tankphysics.Game" });
	}
}
