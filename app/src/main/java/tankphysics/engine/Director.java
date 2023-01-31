package tankphysics.engine;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class Director {
	// World
	private PApplet sketch;
	private HashSet<GameObject> world;

	// Visuals
	private GameObject camera;
	private HashSet<VisualModel> visuals;

	// Forces
	private long lastTimeStamp = new Date().getTime();
	private final Force GRAVITY = new Force(new PVector(0, 9.18f), false, true);
	private final float PIXELS_PER_UNIT = 64.0f;
	private HashMap<RigidBody, HashSet<Force>> bodies;

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public GameObject getCamera() {
		return camera;
	}

	public HashSet<VisualModel> getVisuals() {
		return visuals;
	}

	public void setVisuals(HashSet<VisualModel> visuals) {
		this.visuals = visuals;
	}

	public HashMap<RigidBody, HashSet<Force>> getBodies() {
		return bodies;
	}

	public void setBodies(HashMap<RigidBody, HashSet<Force>> bodies) {
		this.bodies = bodies;
	}

	public HashSet<GameObject> getWorld() {
		return world;
	}

	public void setWorld(HashSet<GameObject> world) {
		this.world = world;
	}

	public void setCamera(GameObject camera) {
		this.camera = camera;
	}

	//////////////////////
	// Gameloop methods //
	//////////////////////

	/**
	 * Initiate draw on all visuals in the world.
	 */
	public void draw() {
		long currentTime = new Date().getTime();
		float deltaT = ((float) (currentTime - lastTimeStamp)) / 1000;
		lastTimeStamp = currentTime;

		// Apply forces to rigid bodies
		for (RigidBody b : bodies.keySet()) {
			b.apply(bodies.get(b).stream(), deltaT, PIXELS_PER_UNIT);
			System.out.println(deltaT + ", " + b.getVelocity().y);
		}

		// Draw visuals
		for (VisualModel v : visuals) {
			v.draw(camera, sketch);
		}
	}

	/**
	 * Update the engine logic.
	 */
	public void update() {

	}

	/**
	 * Attach a game object to the director, to interact with its world and get
	 * displayed in relation to its camera.
	 *
	 * @param object The object to attach.
	 */
	public void attach(GameObject object) {
		world.add(object);
		for (Component c : object.getComponents()) {
			if (c instanceof VisualModel) {
				visuals.add((VisualModel) c);
			}
			if (c instanceof RigidBody) {
				bodies.put((RigidBody) c, new HashSet<>(Arrays.asList(GRAVITY)));
			}
		}
	}

	//////////////////
	// Constructors //
	//////////////////

	/**
	 * Constructor for a director object.
	 */
	public Director(PApplet sketch) {
		world = new HashSet<>();
		visuals = new HashSet<>();
		camera = new GameObject(new PVector(sketch.displayWidth / 2, sketch.displayHeight / 2),
				new PVector(sketch.displayWidth / 2, sketch.displayHeight / 2));
		bodies = new HashMap<>();
		this.sketch = sketch;
	}
}
