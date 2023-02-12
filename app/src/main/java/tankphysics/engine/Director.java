package tankphysics.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;

import processing.core.PApplet;
import processing.core.PVector;

public class Director {
	// World
	private PApplet sketch;
	private HashSet<GameObject> world = new HashSet<>();
	private float targetSecondsPerFrame = 1f / 144;

	// Visuals
	private GameObject camera;
	private HashSet<VisualModel> visuals = new HashSet<>();

	// Forces
	private long lastTimeStamp = new Date().getTime();
	private float deltaT = 0;
	private final Force GRAVITY = new Force(new PVector(0, 9.18f), false, true);
	private HashMap<RigidBody, HashSet<Force>> bodies = new HashMap<>();
	private HashSet<PhysicalObject> colliders = new HashSet<>();
	public boolean pause = false;

	// Force handling
	private HashMap<PhysicalObject, HashSet<Pair>> objectMap = new HashMap<>();
	private HashSet<Pair> activePairs = new HashSet<>();

	// Event listeners

	class Pair {
		PhysicalObject obj1;
		PhysicalObject obj2;

		@Override
		public int hashCode() {
			return obj1.hashCode() + obj2.hashCode();
		}

		public boolean equals(Object obj) {
			if (obj instanceof Pair) {
				Pair p = (Pair) obj;
				return (this.obj1 == p.obj1 && this.obj2 == p.obj2) || (this.obj1 == p.obj2 && this.obj2 == p.obj1);
			}
			return false;
		}

		public Pair(PhysicalObject obj1, PhysicalObject obj2) {
			this.obj1 = obj1;
			this.obj2 = obj2;
		}
	}

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

	public void togglePause() {
		deltaT = 0;
		pause = !pause;
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
				}
				if (c instanceof PhysicalObject && (bodies.containsKey(c) || colliders.contains(c))) {
					PhysicalObject cPhys = (PhysicalObject) c;
					if (c instanceof RigidBody) {
						bodies.remove(c);
					} else if (c instanceof CollisionMesh) {
						colliders.remove(c);
					}
					HashSet<Pair> objPairs = objectMap.get(c);
					Iterator<Pair> setIterator = activePairs.iterator();
					while (setIterator.hasNext()) {
						Pair next = setIterator.next();
						if (objPairs.contains(next)) {
							setIterator.remove();
						}
					}
					for (Pair next : objPairs) {
						PhysicalObject rm = next.obj1 == c ? next.obj2 : next.obj1;
						objectMap.get(rm).remove(next);
					}
					objectMap.remove(c);
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
				} else if (c instanceof RigidBody || c instanceof CollisionMesh) {
					PhysicalObject cPhys = (PhysicalObject) c;
					objectMap.put(cPhys, new HashSet<>());

					// Add all new rigid body interactions to list of pairs
					for (RigidBody b : bodies.keySet()) {
						Pair newPair = new Pair(cPhys, b);
						activePairs.add(newPair);
						objectMap.get(cPhys).add(newPair);
						objectMap.get(b).add(newPair);
					}
					if (c instanceof CollisionMesh) {
						colliders.add(cPhys);
					} else {
						// Add all collision mesh interactions to list of pairs.
						for (PhysicalObject mesh : colliders) {
							Pair newPair = new Pair(cPhys, mesh);
							activePairs.add(newPair);
							objectMap.get(cPhys).add(newPair);
							objectMap.get(mesh).add(newPair);
						}
						bodies.put((RigidBody) c, new HashSet<>(Arrays.asList(GRAVITY)));
					}
				}
			}
		}
	}

	/**
	 * Calculate updates and draw for the next game frame.
	 */
	public void nextFrame() {
		// Get time taken since last frame and current seconds per frame.
		long currentTime = new Date().getTime();
		deltaT += ((float) (currentTime - lastTimeStamp)) / 1000f;
		lastTimeStamp = currentTime;

		// Update loop
		while (!pause && deltaT > targetSecondsPerFrame) {
			for (GameObject o : world) {
				o.update();
			}
			update();
			deltaT -= targetSecondsPerFrame;
		}
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
			b.apply(bodies.get(b).stream(), targetSecondsPerFrame);
		}

		// Apply collision check for inert mesh to rigid body
		ArrayDeque<Pair> queue = new ArrayDeque<>(activePairs);
		while (!queue.isEmpty()) {
			Pair next = queue.pop();
			if (PhysicalObject.requiresCollisionCheck(next.obj1, next.obj2)) {
				boolean moved = PhysicalObject.applyCollisionAndBounce(next.obj1, next.obj2);
				if (moved) {
					HashSet<Pair> nextElements = new HashSet<>(objectMap.get(next.obj1));
					nextElements.addAll(objectMap.get(next.obj1));
					nextElements.remove(next);
					queue.addAll(nextElements);
				}
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
		camera = new GameObject(new PVector(sketch.displayWidth, sketch.displayHeight),
				new PVector(sketch.displayWidth / 2, sketch.displayHeight / 2));
		attach(camera);
		this.sketch = sketch;
	}
}
