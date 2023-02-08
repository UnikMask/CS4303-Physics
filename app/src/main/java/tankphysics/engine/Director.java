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
	private float secondsPerFrame;

	// Visuals
	private GameObject camera;
	private HashSet<VisualModel> visuals;

	// Forces
	private long lastTimeStamp = new Date().getTime();
	private final Force GRAVITY = new Force(new PVector(0, 9.18f), false, true);
	private final float PIXELS_PER_UNIT = 64.0f;
	private HashMap<RigidBody, HashSet<Force>> bodies;
	private HashSet<CollisionMesh> collisions;

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public GameObject getCamera() {
		return camera;
	}

	public void setCamera(GameObject camera) {
		if (world.contains(camera)) {
			this.camera = camera;
		} else {
			System.err.println("Can't set a game object not belonging to world as camera.");
		}
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

	//////////////////////
	// Gameloop methods //
	//////////////////////

	/**
	 * Disattaches a game object from the world. Game object must be in the world to
	 * begin with. Removes all of the object's components, and its children, from
	 * the world.
	 *
	 * @param obj The GameObject to distach.
	 */
	public void disattach(GameObject obj) {
		if (world.contains(obj)) {
			world.remove(obj);
			for (Component c : obj.getComponents()) {
				if (c instanceof VisualModel && visuals.contains(c)) {
					visuals.remove(c);
				} else if (c instanceof RigidBody && bodies.containsKey(c)) {
					bodies.remove(c);
				} else if (c instanceof CollisionMesh && collisions.contains(c)) {
					collisions.remove(c);
				}
			}
			for (GameObject o : obj.getChildren()) {
				disattach(o);
			}
		}
	}

	/**
	 * Attach a game object to the director, to interact with its world and get
	 * displayed in relation to its camera.
	 *
	 * @param object The object to attach.
	 */
	public void attach(GameObject... objects) {
		for (GameObject object : objects) {
			world.add(object);
			for (Component c : object.getComponents()) {
				if (c instanceof VisualModel) {
					visuals.add((VisualModel) c);
				} else if (c instanceof RigidBody) {
					bodies.put((RigidBody) c, new HashSet<>(Arrays.asList(GRAVITY)));
				} else if (c instanceof CollisionMesh) {
					collisions.add((CollisionMesh) c);
				}
			}
		}
	}

	/**
	 * Calculate updates and draw for the next game frame.
	 */
	public void nextFrame() {
		// Get time taken since last frame and current seconds per frame.
		secondsPerFrame = (1f / sketch.frameRate);
		long currentTime = new Date().getTime();
		float deltaT = ((float) (currentTime - lastTimeStamp)) / 1000f;
		lastTimeStamp = currentTime;

		// Update loop
		// while (deltaT > 1.1 * secondsPerFrame) {
		// for (GameObject o : world) {
		// o.update();
		// }
		// update();
		// deltaT -= secondsPerFrame;
		// }
		for (GameObject o : world) {
			o.update();
		}
		update();
		// Draw the image after all updates have been made.
		draw();

	}

	/**
	 * Initiate draw on all visuals in the world.
	 */
	public void draw() {
		// Draw visuals
		for (VisualModel v : visuals) {
			v.draw(camera, sketch);
		}
	}

	/**
	 * Update the engine logic.
	 */
	public void update() {
		// Apply forces to rigid bodies
		for (RigidBody b : bodies.keySet()) {
			b.apply(bodies.get(b).stream(), secondsPerFrame, PIXELS_PER_UNIT);
		}
		// Apply collision check for inert mesh to rigid body
		for (RigidBody b : bodies.keySet()) {
			for (CollisionMesh c : collisions) {
				if (b.getObject() != c.getObject()) {
					if (c.requiresCollisionCheck(b)) {
						c.applyCollisionAndBounce(b);
					}
				}
			}
		}
		/*
		 * Apply rigid body to rigid body collision check for (RigidBody b :
		 * bodies.keySet()) { for (RigidBody bA : bodies.keySet()) { if (b != bA) {
		 * RigidBody.applyCollisionAndBounce(b, bA); } } }
		 */

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
		bodies = new HashMap<>();
		collisions = new HashSet<>();
		camera = new GameObject(new PVector(sketch.displayWidth, sketch.displayHeight),
				new PVector(sketch.displayWidth / 2, sketch.displayHeight / 2));
		attach(camera);
		this.sketch = sketch;
	}
}
