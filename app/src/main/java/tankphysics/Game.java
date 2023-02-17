package tankphysics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import processing.core.PVector;
import tankphysics.WindIndicator.WindDirection;
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

public class Game {
	private final int NUM_BOXES = 39;
	private final int BOXES_PER_COL = 3;
	private final float BOX_SPACING = 1.6f;
	private final float MAX_WIND_INTENSITY = 20;
	private final int NUM_FRAMES_WIN_STATE = 600;
	private final float PLAYER_INDICATOR_Y_OFFSET = -2;

	// PApplet handling
	PApplet sketch;

	// Director engine handling
	Director engineDirector;
	GameObject camera;

	// Game scene handling
	Tank redTank;
	Tank blueTank;
	Tank currentTank;
	Bullet currentBullet;
	GameObject floor;
	GameObject[] boundaries;
	PVector windIntensity = new PVector();
	GameObject currentPlayerIndicator;
	boolean initiateNextTurn = false;
	boolean player1;
	boolean player2;

	// Notification text handling
	String notificationText = "";
	int notificationFrames = 0;

	// UI objects
	HealthBar redHealthBar;
	HealthBar blueHealthBar;
	WindIndicator windSpeed = new WindIndicator(new PVector(22.5f, 2));

	GameState state = GameState.RESTART;

	// Input handling - held keys
	HashMap<Character, Integer> heldKeys = new HashMap<>();

	// Game state
	enum GameState {
		ONGOING, WON, RESTART, END, PAUSE
	}

	public GameState getState() {
		return state;
	}

	public void setState(GameState state) {
		this.state = state;
	}

	//////////////////////
	// Gameloop Methods //
	//////////////////////

	public EngineEventListener getBulletOnHitListener(Bullet bullet) {
		return new EngineEventListener() {
			public void call(GameObject caller, Object... parameters) {
				engineDirector.disattach(bullet);
				currentBullet = null;
				initiateNextTurn = true;
			}
		};
	}

	public EngineEventListener getBulletUpdateListener(Bullet bullet) {
		return new EngineEventListener() {
			Force f;
			RigidBody body = bullet.getRigidBody();

			public String toString() {
				return "Bullet update";
			}

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
			public String toString() {
				return "Tank on boundary hit";
			}

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

		int rand = (int) (Math.random() * 3.0f);

		WindDirection dir = WindDirection.NEUTRAL;

		if (rand % 3 == 0) {
			windIntensity = new PVector(-MAX_WIND_INTENSITY, 0);
			dir = WindDirection.LEFT;
		} else if (rand % 3 == 2) {
			windIntensity = new PVector(MAX_WIND_INTENSITY, 0);
			dir = WindDirection.RIGHT;
		} else {
			windIntensity = new PVector(0, 0);
		}
		windSpeed.setDirection(dir, engineDirector);
		initiateNextTurn = false;
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

	public void addNotification(String text) {
		notificationText = text;
		notificationFrames = 300;
	}

	// In world update called on every new game update
	public EngineEventListener update() {
		return new EngineEventListener() {
			int winnerScreenTimer = NUM_FRAMES_WIN_STATE;

			public String toString() {
				return "Game update";
			}

			public void call(GameObject caller, Object... parameters) {
				if (state == GameState.ONGOING) {
					notificationFrames--;
					if (notificationFrames <= 0) {
						notificationText = "";
					}

					// Set camera position
					float scale = Math.max(16, Math.abs(redTank.getPosition().x - blueTank.getPosition().x)) + 4;
					float tanksXPos = (redTank.getPosition().x + blueTank.getPosition().x) / 2;
					PVector nextCameraPosition = new PVector(tanksXPos, -5);
					if (currentBullet != null) {
						nextCameraPosition = new PVector(tanksXPos,
								-5 - currentBullet.getStartPosition().y + currentBullet.getPosition().y);
					}
					camera.setPosition(PVector.lerp(camera.getPosition(), nextCameraPosition, 0.1f));
					camera.setSize(new PVector(scale, (9f / 16f) * scale));
				} else if (state == GameState.WON) {
					// Set camera position
					camera.setPosition(PVector.lerp(camera.getPosition(), currentTank.getPosition(), 0.1f));
					camera.setSize(PVector.lerp(camera.getSize(), new PVector(10, (9f / 16f) * 10f), 0.1f));

					// Check for when to restart
					winnerScreenTimer--;
					if (winnerScreenTimer <= 0) {
						state = GameState.RESTART;
						engineDirector.disattachEventListener(this);
					}
				}

				// Update player indicator and current tank aim
				currentPlayerIndicator.setPosition(PVector.lerp(currentPlayerIndicator.getPosition(),
						PVector.add(currentTank.getPosition(), new PVector(0, PLAYER_INDICATOR_Y_OFFSET)), 0.1f));
				currentTank.getController()
						.update(PVector.sub(new PVector(sketch.mouseX, sketch.mouseY),
								engineDirector.getSetVector(currentTank.getNozzle().getPosition())),
								currentTank == redTank ? blueTank : redTank);
				for (char key : heldKeys.keySet()) {
					keyHeld(key);
				}
			}
		};
	}

	//////////////////////////
	// Game Loading Methods //
	//////////////////////////

	// Setup of the game objects for the game loop
	public void worldSetup() {
		engineDirector = new Director(sketch);
		camera = engineDirector.getCamera();

		// Make a plane for collision checks.
		floor = new GameObject(new PVector(40, 20), new PVector(20, 10), false,
				new VisualPolygon(new PVector(), Polygons.makeSquare(new PVector(40, 20)), sketch.color(128)),
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

		// Build the tank indicator
		currentPlayerIndicator = new GameObject(new PVector(0.5f, 0.5f), new PVector(0, 0), false,
				new VisualPolygon(new PVector(),
						Arrays.asList(new PVector(-0.25f, -0.25f), new PVector(0.25f, -0.25f), new PVector(0, 0.25f)),
						sketch.color(255)));

		// Set up the tanks
		redHealthBar.setPercentage(0);
		blueHealthBar.setPercentage(0);
		redTank = new Tank(new PVector(5, -2), sketch.color(255, 0, 0), redHealthBar);
		redTank.setController(player1 ? new PlayerController(redTank, this) : new ComputerController(redTank, this));
		redTank.attachEventListener("onHit", getTankOnBoundaryHitListener(redTank));
		blueTank = new Tank(new PVector(35, -2), sketch.color(0, 0, 255), blueHealthBar);
		blueTank.setController(player2 ? new PlayerController(blueTank, this) : new ComputerController(blueTank, this));
		blueTank.attachEventListener("onHit", getTankOnBoundaryHitListener(blueTank));

		// Set up the box grid
		GameObject[] boxGrid = new GameObject[NUM_BOXES];
		for (int i = 0; i < NUM_BOXES; i++) {
			boxGrid[i] = new Box(
					new PVector(10 + BOX_SPACING * (i / BOXES_PER_COL), -1.5f - 1.6f * (i % BOXES_PER_COL)), sketch,
					engineDirector);
		}

		// Attach all components to director.
		engineDirector.attach(floor, redTank, blueTank, windSpeed, currentPlayerIndicator);
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
		currentPlayerIndicator
				.setPosition(PVector.add(currentTank.getPosition(), new PVector(0, PLAYER_INDICATOR_Y_OFFSET)));
		redTank.setState(Tank.TankState.MOVING);
		engineDirector.attachEventListener("update", update());
		engineDirector.setReady();
	}

	public void resetWorld() {
		engineDirector = null;
		redHealthBar.setPercentage(0);
		blueHealthBar.setPercentage(0);
	}

	////////////////////
	// Input Handling //
	////////////////////

	public void keyReleased(char key) {
		if (!heldKeys.containsKey(key)) {
			return;
		}
		heldKeys.remove(key);
		if (key == 'w') {
			// engineDirector.togglePause();
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

	public void keyPressed(char key) {
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
		currentTank.getController().keyHeld(key);
	}

	public void mouseClicked() {
		if (state != GameState.ONGOING) {
			return;
		}

		currentTank.getController().onClick(engineDirector.getSetVector(new PVector(sketch.mouseX, sketch.mouseY)));
	}

	//////////////////////////
	// PApplet Main Handler //
	//////////////////////////

	// PApplet and backend setup for the game.
	public void draw() {
		if (state == GameState.RESTART) {
			resetWorld();
			worldSetup();
		} else {
			if (state == GameState.PAUSE && !engineDirector.isPaused()) {
				engineDirector.setPause(true);
			} else if (state != GameState.PAUSE && engineDirector.isPaused()) {
				engineDirector.setPause(false);
			}
			engineDirector.nextFrame();

			// Draw ongoing game UI
			if (state == GameState.ONGOING) {
				blueHealthBar.draw(sketch);
				redHealthBar.draw(sketch);
			}
			if (!notificationText.equals("")) {
				sketch.pushStyle();
				sketch.textSize(128);
				sketch.fill(255);
				sketch.textAlign(PApplet.CENTER, PApplet.BOTTOM);
				sketch.text(notificationText, sketch.width / 2, 8);
				sketch.popStyle();
			}

			// After all is drawn - if a next turn should be initialized, initialize it.
			if (initiateNextTurn) {
				nextTurn();
			}
		}
	}

	public Game(PApplet sketch, boolean player1, boolean player2) {
		this.sketch = sketch;
		this.player1 = player1;
		this.player2 = player2;
		redHealthBar = new HealthBar(new PVector(0.05f, 0.85f), sketch.color(255, 50, 50, 128));
		blueHealthBar = new HealthBar(new PVector(0.75f, 0.85f), sketch.color(50, 50, 255, 128));
	}
}
