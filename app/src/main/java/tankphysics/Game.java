package tankphysics;

import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import processing.core.PVector;
import tankphysics.engine.CollisionMesh;
import tankphysics.engine.Component;
import tankphysics.engine.Director;
import tankphysics.engine.EngineEventListener;
import tankphysics.engine.Force;
import tankphysics.engine.GameObject;
import tankphysics.engine.PhysicalObject;
import tankphysics.engine.Polygons;
import tankphysics.engine.RigidBody;
import tankphysics.engine.Surfaces;
import tankphysics.engine.VisualPolygon;

public class Game extends PApplet {
	private final int NUM_BOXES = 65;
	private final int BOXES_PER_COL = 5;
	private final float BOX_SPACING = 1.6f;
	private final float MAX_WIND_INTENSITY = 50;
	private final int NUM_FRAMES_WIN_STATE = 420;

	// Director engine handling
	Director engineDirector;
	GameObject camera;

	// Game scene handling
	Tank redTank;
	Tank blueTank;
	Tank currentTank;
	Bullet currentBullet;
	GameObject[] boundaries;
	PVector windIntensity = new PVector();
	GameState state = GameState.ONGOING;

	// Input handling - held keys
	HashMap<Character, Integer> heldKeys = new HashMap<>();

	// Game state
	enum GameState {
		ONGOING, WON, RESTART
	}

	//////////////////////
	// Gameloop Methods //
	//////////////////////

	public EngineEventListener getBulletOnHitListener(Bullet bullet) {
		Game self = this;
		return new EngineEventListener() {
			public void call(GameObject caller, Object... parameters) {
				engineDirector.disattach(bullet);
				currentBullet = null;
				self.nextTurn();
			}
		};
	}

	public EngineEventListener getBulletUpdateListener(Bullet bullet) {
		return new EngineEventListener() {
			Force f;
			RigidBody body = bullet.getRigidBody();

			public void call(GameObject caller, Object... parameters) {
				PVector windNormal = windIntensity.copy().normalize();
				float relativeVelocity = Math
						.abs(PVector.dot(PVector.sub(body.getVelocity(), windIntensity), windNormal));

				// Clean last force from body and new one
				engineDirector.removeForce(body, f);
				f = new Force(PVector.mult(windNormal, relativeVelocity), false, false);
				engineDirector.addForce(body, f);
			}
		};
	}

	public EngineEventListener getTankOnBoundaryHitListener(Tank tank) {
		return new EngineEventListener() {
			public void call(GameObject caller, Object... parameters) {
				for (GameObject boundary : boundaries) {
					if (caller == boundary) {
						initiateGameEnd(tank);
					}
				}
			}
		};
	}

	public void nextTurn() {
		Tank lastTank = currentTank;
		currentTank = (currentTank == redTank ? blueTank : redTank);

		lastTank.setState(Tank.TankState.IDLE);
		currentTank.setState(Tank.TankState.MOVING);
		windIntensity = new PVector((float) (Math.random() % (2 * MAX_WIND_INTENSITY)) - MAX_WIND_INTENSITY, 0);
	}

	public void initiateGameEnd(Tank tank) {
		if (tank == redTank) {
			System.out.println("Blue tank wins!");
			currentTank = blueTank;
		} else {
			System.out.println("Red tank wins!");
			currentTank = redTank;
		}
		currentTank.setState(Tank.TankState.MOVING);
		engineDirector.disattach(tank);
		for (GameObject o : boundaries) {
			engineDirector.disattach(o);
		}
		state = GameState.WON;
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
		if (key == 'q') {
			currentTank.getRigidBody().setRotationalVelocity(currentTank.getRigidBody().getRotationalVelocity() - 0.1f);
		}
		if (key == 'e') {
			currentTank.getRigidBody().setRotationalVelocity(currentTank.getRigidBody().getRotationalVelocity() + 0.1f);
		}
	}

	public void mouseClicked() {
		if (state != GameState.ONGOING) {
			return;
		}

		Bullet bullet = currentTank.spawnProjectile();
		if (bullet != null) {
			engineDirector.attach(bullet);
			bullet.attachEventListener("onHit", getBulletOnHitListener(bullet));
			currentBullet = bullet;
			engineDirector.attachEventListener("update", getBulletUpdateListener(bullet));
			engineDirector.removeCollisions(bullet.getRigidBody(), currentTank.getRigidBody());
		}
	}

	//////////////////////////
	// PApplet Main Handler //
	//////////////////////////

	// Setup of the game objects for the game loop
	public void worldSetup() {
		engineDirector = new Director(this);
		camera = engineDirector.getCamera();

		// Make a plane for collision checks.
		GameObject floor = new GameObject(new PVector(40, 20), new PVector(20, 10), false,
				new VisualPolygon(new PVector(), Polygons.makeSquare(new PVector(40, 20)), color(128)),
				new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(40, 20)),
						Map.ofEntries(Map.entry("staticFriction", 0.6f), Map.entry("dynamicFriction", 0.5f),
								Map.entry("bounciness", 0.3f))));

		// Set game boundaries
		boundaries = new GameObject[] {
				new GameObject(new PVector(800, 5), new PVector(20, 12.5f), false,
						new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(800, 5)),
								Surfaces.getBoundarySurface())),
				new GameObject(new PVector(5, 400), new PVector(-20, 0), false,
						new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(5, 400)),
								Surfaces.getBoundarySurface())),
				new GameObject(new PVector(5, 400), new PVector(77.5f, 0), false, new CollisionMesh(new PVector(),
						Polygons.makeSquare(new PVector(5, 400)), Surfaces.getBoundarySurface())) };
		GameObject[] tankBoundaries = new GameObject[] {
				new GameObject(new PVector(2, 10), new PVector(9, -5), false,
						new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(5, 30)),
								Surfaces.getBoundarySurface())),
				new GameObject(new PVector(2, 10), new PVector(29, -5), false, new CollisionMesh(new PVector(),
						Polygons.makeSquare(new PVector(5, 30)), Surfaces.getBoundarySurface())) };

		redTank = new Tank(new PVector(5, -2), color(255, 0, 0));
		redTank.attachEventListener("onHit", getTankOnBoundaryHitListener(redTank));
		blueTank = new Tank(new PVector(35, -2), color(0, 0, 255));
		blueTank.attachEventListener("onHit", getTankOnBoundaryHitListener(blueTank));
		GameObject[] boxGrid = new GameObject[NUM_BOXES];
		for (int i = 0; i < NUM_BOXES; i++) {
			boxGrid[i] = new Box(
					new PVector(10 + BOX_SPACING * (i / BOXES_PER_COL), -1.5f - 1.6f * (i % BOXES_PER_COL)), this,
					engineDirector);
		}

		// Attach all components to director.
		engineDirector.attach(floor, redTank, blueTank);
		engineDirector.attach(boxGrid);
		engineDirector.attach(boundaries);
		engineDirector.attach(tankBoundaries);

		// Remove collisions for tank boundaries
		for (GameObject t : tankBoundaries) {
			for (Component c : t.getComponents()) {
				if (c instanceof CollisionMesh) {
					engineDirector.removeCollisionsForAllBut((PhysicalObject) c, redTank.getRigidBody(),
							blueTank.getRigidBody());
					((CollisionMesh) c).setProperties(Map.ofEntries(Map.entry("add_for_collisions", 0.0f)));
				}
			}
		}

		// Set up game start state.
		state = GameState.ONGOING;
		currentTank = redTank;
		redTank.setState(Tank.TankState.MOVING);
		engineDirector.attachEventListener("update", update());
		engineDirector.setReady();
	}

	// PApplet and backend setup for the game.
	public void setup() {
		frameRate(60);
		worldSetup();
	}

	public void settings() {
		size(1920, 1080, PApplet.P2D);
		fullScreen();
	}

	public void draw() {
		background(0);

		if (state == GameState.RESTART) {
			worldSetup();
		} else {
			engineDirector.nextFrame();
		}
	}

	// In world update called on every new game update
	public EngineEventListener update() {
		return new EngineEventListener() {
			int winnerScreenTimer = NUM_FRAMES_WIN_STATE;

			public void call(GameObject caller, Object... parameters) {
				if (state == GameState.ONGOING) {
					float scale = max(16, Math.abs(redTank.getPosition().x - blueTank.getPosition().x)) + 4;

					float tanksXPos = (redTank.getPosition().x + blueTank.getPosition().x) / 2;
					PVector nextCameraPosition = new PVector(tanksXPos, -5);
					if (currentBullet != null) {
						nextCameraPosition = new PVector(tanksXPos,
								-5 - currentBullet.getStartPosition().y + currentBullet.getPosition().y);
					}
					camera.setPosition(PVector.lerp(camera.getPosition(), nextCameraPosition, 0.1f));
					camera.setSize(new PVector(scale, (9f / 16f) * scale));
				} else if (state == GameState.WON) {
					camera.setPosition(PVector.lerp(camera.getPosition(), currentTank.getPosition(), 0.1f));
					camera.setSize(PVector.lerp(camera.getSize(), new PVector(10, (9f / 16f) * 10f), 0.1f));

					winnerScreenTimer--;
					if (winnerScreenTimer <= 0) {
						state = GameState.RESTART;
						engineDirector.disattachEventListener(this);
					}
				}
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
