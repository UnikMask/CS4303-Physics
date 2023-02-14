package tankphysics;

import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import processing.core.PVector;
import tankphysics.engine.*;

public class Game extends PApplet {
	// Director engine handling
	Director engineDirector;
	GameObject camera;

	// Tank handling
	Tank redTank;
	Tank blueTank;
	Tank currentTank;

	// Input handling - held keys
	HashMap<Character, Integer> heldKeys = new HashMap<>();

	// Game state
	enum GameState {

	}

	//////////////////////
	// Gameloop Methods //
	//////////////////////

	public EventListener getNormalBulletNextTurnOnHitListener(Bullet bullet) {
		Game self = this;
		return new EventListener() {
			public void call(GameObject caller, Object... parameters) {
				engineDirector.disattach(bullet);
				self.nextTurn();
			}
		};
	}

	public void nextTurn() {
		Tank lastTank = currentTank;
		currentTank = (currentTank == redTank ? blueTank : redTank);

		lastTank.setState(Tank.TankState.IDLE);
		currentTank.setState(Tank.TankState.MOVING);
	}

	////////////////////
	// Input Handling //
	////////////////////

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
				currentTank.setIdleFriction();
			}
		}
		if (key == 'a') {
			if (!heldKeys.containsKey('d')) {
				currentTank.setIdleFriction();
			}
		}
	}

	public void keyPressed() {
		heldKeys.put(key, 0);
		if (key == 'd') {
			if (!heldKeys.containsKey('a')) {
				currentTank.setMovingFriction();
			} else {
				heldKeys.remove('d');
			}
		}
		if (key == 'a') {
			if (!heldKeys.containsKey('d')) {
				currentTank.setMovingFriction();
			} else {
				heldKeys.remove('a');
			}
		}
	}

	public void keyHeld(char key) {
		if (key == 'd') {
			currentTank.drive(true);
		}
		if (key == 'a') {
			currentTank.drive(false);
		}
	}

	public void mouseClicked() {
		Bullet bullet = currentTank.spawnProjectile();
		if (bullet != null) {
			engineDirector.attach(bullet);
			bullet.attachEventListener("onHit", getNormalBulletNextTurnOnHitListener(bullet));
			engineDirector.removeCollisions(bullet.getRigidBody(), currentTank.getRigidBody());
		}
	}

	//////////////////////////
	// PApplet Main Handler //
	//////////////////////////

	// Setup of the game objects for the game loop
	public void worldSetup() {
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
			boxGrid[i] = new Box(new PVector(-6.5f + 3.2f * (i / 5), -2 - 2.1f * (i % 5)), this, engineDirector);
		}

		// Attach all components to director.
		engineDirector.attach(floor, ceiling, redTank, blueTank);
		engineDirector.attach(boxGrid);
		currentTank = redTank;
		redTank.setState(Tank.TankState.MOVING);

	}

	// PApplet and backend setup for the game.
	public void setup() {
		frameRate(60);
		engineDirector = new Director(this);
		camera = engineDirector.getCamera();
		worldSetup();
		engineDirector.attachEventListener("update", update());
	}

	public void settings() {
		size(1920, 1080, PApplet.P2D);
		fullScreen();
	}

	public void draw() {
		background(0);
		engineDirector.nextFrame();
	}

	// In world update called on every new game update
	public EventListener update() {
		return new EventListener() {
			public void call(GameObject caller, Object... parameters) {
				float scale = max(16, Math.abs(redTank.getPosition().x - blueTank.getPosition().x)) + 4;

				camera.setPosition(
						PVector.add(PVector.div(PVector.add(redTank.getPosition(), blueTank.getPosition()), 2),
								new PVector(0, -5)));
				camera.setSize(new PVector(scale, (9f / 16f) * scale));
				currentTank.setAimOptions(PVector.sub(new PVector(mouseX, mouseY),
						engineDirector.getSetVector(currentTank.getNozzle().getPosition())));
				for (char key : heldKeys.keySet()) {
					keyHeld(key);
				}
			}
		};
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "tankphysics.Game" });
	}
}
