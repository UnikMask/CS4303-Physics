package tankphysics.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

import processing.core.PApplet;
import processing.core.PMatrix2D;
import processing.core.PVector;

public class Director {
	// World
	private PApplet sketch;
	private HashSet<GameObject> world = new HashSet<>();
	private float targetSecondsPerFrame = 1f / 144;
	private static final int COLLISION_CHECK_PER_FRAME_LIMIT = 2;

	// Visuals
	private GameObject camera;
	private HashSet<VisualModel> visuals = new HashSet<>();
	private HashSet<VisualModel> topVisuals = new HashSet<>();

	// Forces
	private long lastTimeStamp = new Date().getTime();
	private float deltaT = 0;
	private final Force GRAVITY = new Force(new PVector(0, 9.18f), false, true);
	private HashMap<RigidBody, HashSet<Force>> bodies = new HashMap<>();
	private HashSet<PhysicalObject> colliders = new HashSet<>();
	public boolean pause = false;
	public boolean ready = false;

	// Force handling
	private HashMap<PhysicalObject, HashSet<Pair>> objectMap = new HashMap<>();
	private HashSet<Pair> activePairs = new HashSet<>();

	// Director's event listener list
	private HashMap<String, HashSet<EngineEventListener>> listeners = new HashMap<>(
			Map.ofEntries(Map.entry("update", new HashSet<>())));
	private HashMap<EngineEventListener, String> listenerToId = new HashMap<>();
	private ArrayList<EngineEventListener> listenersSetForDestruction = new ArrayList<>();

	// Simulation
	private static final float SIMULATION_TIMEOUT_SECONDS = 15;

	// Class representing a pair of physical objects interacting together in
	// collisions.
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

	public boolean isPaused() {
		return pause;
	}

	public void setPause(boolean pause) {
		deltaT = 0;
		this.pause = pause;
		System.out.println("set to " + pause);
	}

	public void setReady() {
		ready = true;
	}

	////////////////////////////
	// World Handling Methods //
	////////////////////////////

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
				disattachComponent(c);
			}
			for (GameObject o : obj.getChildren()) {
				disattach(o);
			}
			for (EngineEventListener l : obj.listenerToId.keySet()) {
				if (listenerToId.containsKey(l)) {
					listenersSetForDestruction.add(l);
				}
			}
		}
	}

	public void disattachComponent(Component c) {
		if (c instanceof VisualModel) {
			if (visuals.contains(c)) {
				visuals.remove(c);
			} else if (topVisuals.contains(c)) {
				topVisuals.remove(c);
			}
		}
		if (c instanceof PhysicalObject
				&& (bodies.containsKey((PhysicalObject) c) || colliders.contains((PhysicalObject) c))) {
			PhysicalObject cPhys = (PhysicalObject) c;
			if (c instanceof RigidBody) {
				bodies.remove(c);
			} else {
				colliders.remove(cPhys);
			}
			HashSet<Pair> objPairs = objectMap.get(cPhys);
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
			objectMap.remove(cPhys);
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
				attachComponent(c);
			}
			for (GameObject child : object.getChildren()) {
				attach(child);
			}
		}
	}

	/**
	 * Attach a component from the given GameObject to the Director.
	 *
	 * @param c The component to attach to the director.
	 */
	public void attachComponent(Component c) {
		if (c instanceof VisualModel) {
			if (c.getObject().isOnTop()) {
				topVisuals.add((VisualModel) c);
			} else {
				visuals.add((VisualModel) c);
			}
		} else if (c instanceof PhysicalObject) {
			PhysicalObject cPhys = (PhysicalObject) c;
			objectMap.put(cPhys, new HashSet<>());

			// Add all new rigid body interactions to list of pairs
			for (RigidBody b : bodies.keySet()) {
				if (b.addForCollisions()) {
					Pair newPair = new Pair(cPhys, b);
					activePairs.add(newPair);
					objectMap.get(cPhys).add(newPair);
					objectMap.get(b).add(newPair);
				}
			}
			if (c instanceof RigidBody) {
				// Add all collision mesh interactions to list of pairs.
				for (PhysicalObject mesh : colliders) {
					if (mesh.addForCollisions()) {
						Pair newPair = new Pair(cPhys, mesh);
						activePairs.add(newPair);
						objectMap.get(cPhys).add(newPair);
						objectMap.get(mesh).add(newPair);
					}
				}
				bodies.put((RigidBody) c, new HashSet<>(Arrays.asList(GRAVITY)));
			} else {
				colliders.add(cPhys);
			}
		}
	}

	/**
	 * Add a force to the stream of active forces for a rigid body.
	 *
	 * @param b The affected rigid body.
	 * @param f The force affecting the rigid body.
	 */
	public void addForce(RigidBody b, Force f) {
		if (bodies.containsKey(b) && !bodies.get(b).contains(f)) {
			bodies.get(b).add(f);
		}
	}

	/**
	 * Remove from the RigidBody the given force.
	 *
	 * @param b The affected rigid body.
	 * @param f The force to remove from the rigid body.
	 */
	public void removeForce(RigidBody b, Force f) {
		if (bodies.containsKey(b) && bodies.get(b).contains(f)) {
			bodies.get(b).remove(f);
		}
	}

	/**
	 * Remove collisions on the director between one object and another.
	 *
	 * @param objA 1st object of pair to remove.
	 * @param objB 2nd object of pair to remove.
	 */
	public void removeCollisions(PhysicalObject objA, PhysicalObject objB) {
		if (world.contains(objA.getObject()) && world.contains(objB.getObject())) {
			Pair currentPair = new Pair(objA, objB);
			if (activePairs.contains(currentPair)) {
				activePairs.remove(currentPair);
				objectMap.get(objA).remove(currentPair);
				objectMap.get(objB).remove(currentPair);
			}
		}
	}

	/**
	 * Remove all collisions for a physical object but the ones with a given set of
	 * physical objects.
	 *
	 * @param target The physical object to remove collisions for
	 * @param keep   The list of physical objects to keep collisions for.
	 */
	public void removeCollisionsForAllBut(PhysicalObject target, PhysicalObject... keep) {
		HashSet<PhysicalObject> keepSet = new HashSet<>(Arrays.asList(keep));
		for (PhysicalObject o : colliders) {
			if (o != target && !keepSet.contains(o)) {
				removeCollisions(target, o);
			}
		}
		for (PhysicalObject o : bodies.keySet()) {
			if (o != target && !keepSet.contains(o)) {
				removeCollisions(target, o);
			}
		}
	}

	//////////////////////
	// Gameloop methods //
	//////////////////////

	/**
	 * Calculate updates and draw for the next game frame.
	 */
	public void nextFrame() {
		// If the director is not ready, return without doing anything.
		if (!ready) {
			return;
		}

		// Get time taken since last frame and current seconds per frame.
		long currentTime = new Date().getTime();
		deltaT += ((float) (currentTime - lastTimeStamp)) / 1000f;
		lastTimeStamp = currentTime;

		// Update loop
		while (!pause && deltaT > targetSecondsPerFrame) {
			for (EngineEventListener l : listeners.get("update")) {
				l.call(null);
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

		// Top visuals are on top - draw after normal visuals.
		for (VisualModel v : topVisuals) {
			v.draw(camera, sketch);
		}
	}

	/**
	 * Update the engine logic.
	 */
	public void update() {
		cleanListeners();

		// Apply forces to rigid bodies
		for (RigidBody b : bodies.keySet()) {
			b.apply(bodies.get(b).stream(), targetSecondsPerFrame);
		}

		// Apply collision check for inert mesh to rigid body
		ArrayDeque<Pair> queue = new ArrayDeque<>(activePairs);
		HashMap<Pair, Integer> donePairs = new HashMap<>();
		HashSet<Pair> collidedPairs = new HashSet<>();
		while (!queue.isEmpty()) {
			Pair next = queue.pop();
			if (donePairs.containsKey(next)) {
				if (donePairs.get(next) >= COLLISION_CHECK_PER_FRAME_LIMIT) {
					continue;
				}
				donePairs.replace(next, donePairs.get(next) + 1);
			} else {
				donePairs.put(next, 1);
			}

			if (PhysicalObject.requiresCollisionCheck(next.obj1, next.obj2)) {
				boolean collided = PhysicalObject.applyCollisionAndBounce(next.obj1, next.obj2);

				// Add previous object linked pairs to queue for collision recalculation.
				if (collided) {
					HashSet<Pair> nextElements = new HashSet<>(objectMap.get(next.obj1));
					nextElements.addAll(objectMap.get(next.obj2));
					nextElements.remove(next);
					queue.addAll(nextElements);
					collidedPairs.add(next);
				}
			}
		}
		for (Pair next : collidedPairs) {
			// Call on hit events on both GameObjects.
			if (world.contains(next.obj1.getObject()) && world.contains(next.obj2.getObject())) {
				for (EngineEventListener l : next.obj1.getObject().getListeners("onHit")) {
					l.call(next.obj2.getObject(), next.obj2);
				}
				for (EngineEventListener l : next.obj2.getObject().getListeners("onHit")) {
					l.call(next.obj1.getObject(), next.obj1);
				}
			}
		}
	}

	/**
	 * Perform a local update with a given game object, ignoring all physics but
	 * physics for the given game object, and all collisions aside from collision
	 * handlers from the game object
	 *
	 * @param obj The rigid body to simulate
	 * @return The list of physical objects this object has interacted with this
	 *         frame.
	 */
	public List<PhysicalObject> localPhysicalUpdate(RigidBody obj) {
		if (!bodies.containsKey(obj)) {
			return null;
		}

		cleanListeners();
		List<PhysicalObject> interactions = new ArrayList<>();
		for (EngineEventListener l : obj.getObject().getListeners("update")) {
			l.call(null);
		}
		obj.apply(bodies.get(obj).stream(), targetSecondsPerFrame);
		ArrayDeque<Pair> queue = new ArrayDeque<>(objectMap.get(obj));
		while (!queue.isEmpty()) {
			Pair next = queue.pop();

			if (PhysicalObject.requiresCollisionCheck(next.obj1, next.obj2)
					&& !PhysicalObject.getCollisionDetails(next.obj1, next.obj2).isEmpty()) {
				for (EngineEventListener l : obj.getObject().getListeners("onHit")) {
					l.call(obj == next.obj1 ? next.obj2.getObject() : next.obj1.getObject());
				}
				interactions.add(obj == next.obj1 ? next.obj2 : next.obj1);
			}
		}
		return interactions;
	}

	/**
	 * Simulate a game object in the world until it destroys itself or timeout
	 * occurs.
	 *
	 * @param obj The game object to simulate
	 * @param rb  The rigid body to attach to the game object and simulate.
	 *
	 * @return A set of all the physical objects the object interacted with.
	 */
	public HashSet<PhysicalObject> startSimulationAndReturnInteractions(GameObject obj, RigidBody rb) {
		if (world.contains(obj) || bodies.containsKey(rb)) {
			return null;
		}

		if (!obj.getComponents().contains(rb)) {
			obj.attach(rb);
		}
		attach(obj);
		HashSet<PhysicalObject> interactions = new HashSet<>();
		for (float time = 0; world.contains(obj) && time < SIMULATION_TIMEOUT_SECONDS; time += targetSecondsPerFrame) {
			interactions.addAll(localPhysicalUpdate(rb));
		}
		if (world.contains(obj)) {
			disattach(obj);
		}
		return interactions;
	}

	/**
	 * Get the projection of the given vector with the camera to get a position on
	 * the game window.
	 *
	 * @param originalVector on base scale to check against.
	 *
	 * @return The vector's projection on the game window coordinates.
	 */
	public PVector getSetVector(PVector originalVector) {
		PMatrix2D mat = new PMatrix2D();
		PVector scale = new PVector(((float) sketch.width) / camera.getSize().x,
				((float) sketch.height) / camera.getSize().y);
		PVector anchoredPos = PVector.sub(camera.getPosition(), PVector.div(camera.getSize(), 2));
		mat.scale(scale.x, scale.y);
		mat.translate(-anchoredPos.x, -anchoredPos.y);
		PVector ret = new PVector();
		mat.mult(originalVector, ret);
		return ret;
	}

	////////////////////////////
	// Event Listener Methods //
	////////////////////////////

	/**
	 * Attach an event listener to the given event.
	 *
	 * @param id       The ID of the event listener. The ID must exist for the
	 *                 director or the listener object is rejected.
	 * @param listener The listener to attach to the Director.
	 *
	 * @return Whether the listener was successfully attached or not.
	 */
	public boolean attachEventListener(String id, EngineEventListener listener) {
		if (!listeners.containsKey(id) || listenerToId.containsKey(listener)) {
			return false;
		}
		listeners.get(id).add(listener);
		listenerToId.put(listener, id);
		return true;
	}

	/**
	 * Disattach an event listener from the given event.
	 *
	 * @param listener The listener to disattach from the Director. The listener
	 *                 must actually be attached to the Director.
	 *
	 * @return Whether the listener was successfully disattached or not.
	 */
	public boolean disattachEventListener(EngineEventListener listener) {
		if (!listenerToId.containsKey(listener)) {
			return false;
		}
		listenersSetForDestruction.add(listener);
		return true;
	}

	/**
	 * Remove all listeners set for destruction.
	 */
	public void cleanListeners() {
		if (!listenersSetForDestruction.isEmpty())
			for (EngineEventListener l : listenersSetForDestruction) {
				if (listenerToId.containsKey(l)) {
					listeners.get(listenerToId.get(l)).remove(l);
					listenerToId.remove(l);
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
		float scale = (float) sketch.height / (float) sketch.width;
		camera = new GameObject(new PVector(20, 20 * scale), new PVector());
		attach(camera);
		this.sketch = sketch;
	}
}
