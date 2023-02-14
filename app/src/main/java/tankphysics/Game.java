package tankphysics;

import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import processing.core.PVector;
import tankphysics.engine.*;

public class Game extends PApplet {
	Director engineDirector;
	GameObject camera;
	Tank redTank;
	Tank blueTank;

	HashMap<Character, Integer> heldKeys = new HashMap<>();

	public void setup() {
		frameRate(60);
		engineDirector = new Director(this);
		camera = engineDirector.getCamera();

		// Make a plane for collision checks.
		GameObject floor = new GameObject(new PVector(30, 5), new PVector(0, 4), false,
				new VisualPolygon(new PVector(), Polygons.makeSquare(new PVector(30, 5)), color(128)),
				new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(30, 5)),
						Map.ofEntries(Map.entry("staticFriction", 0.6f), Map.entry("dynamicFriction", 0.5f),
								Map.entry("bounciness", 0.3f))));

		GameObject ceiling = new GameObject(new PVector(30, 5), new PVector(0, -13), false,
				new VisualPolygon(new PVector(), Polygons.makeSquare(new PVector(30, 5)), color(128)),
				new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(30, 5)),
						Map.ofEntries(Map.entry("staticFriction", 0.6f), Map.entry("dynamicFriction", 0.5f),
								Map.entry("bounciness", 0.3f))));

		redTank = new Tank(new PVector(-13, -2), color(255, 0, 0));
		blueTank = new Tank(new PVector(13, -2), color(0, 0, 255));
		GameObject[] boxGrid = new GameObject[25];
		for (int i = 0; i < 25; i++) {
			boxGrid[i] = new Box(new PVector(-6.5f + 3.2f * (i / 5), -2 - 2.1f * (i % 5)), this);
		}

		// Attach all components to director.
		engineDirector.attach(floor, ceiling, redTank, blueTank);
		engineDirector.attach(boxGrid);
	}

	public void settings() {
		size(1920, 1080, PApplet.P2D);
		fullScreen();
	}

	public void keyReleased() {
		if (!heldKeys.containsKey(key)) {
			return;
		}
		heldKeys.remove(key);
		if (key == 'w') {
			engineDirector.togglePause();
		}
		if (key == 'd') {
			if (!heldKeys.containsKey('a')) {
				redTank.setIdleFriction();
			}
		}
		if (key == 'a') {
			if (!heldKeys.containsKey('d')) {
				redTank.setIdleFriction();
			}
		}
	}

	public void keyPressed() {
		heldKeys.put(key, 0);
		if (key == 'd') {
			if (!heldKeys.containsKey('a')) {
				redTank.setMovingFriction();
			} else {
				heldKeys.remove('d');
			}
		}
		if (key == 'a') {
			if (!heldKeys.containsKey('d')) {
				redTank.setMovingFriction();
			} else {
				heldKeys.remove('a');
			}
		}
	}

	public void keyHeld(char key) {
		if (key == 'd') {
			redTank.drive(true);
		}
		if (key == 'a') {
			redTank.drive(false);
		}
	}

	public void draw() {
		background(0);

		float scale = max(16, Math.abs(redTank.getPosition().x - blueTank.getPosition().x)) + 4;

		camera.setPosition(PVector.add(PVector.div(PVector.add(redTank.getPosition(), blueTank.getPosition()), 2),
				new PVector(0, -5)));
		camera.setSize(new PVector(scale, (9f / 16f) * scale));
		redTank.getNozzle().setExtraAngle(
				PVector.sub(new PVector(mouseX, mouseY), engineDirector.getSetVector(redTank.getNozzle().getPosition()))
						.heading());
		for (char key : heldKeys.keySet()) {
			keyHeld(key);
		}
		engineDirector.nextFrame();
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "tankphysics.TestEnvironment" });
	}
}
