package tankphysics.engine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import processing.core.PVector;

public class GameObject {
	protected PVector position;
	protected PVector size;
	protected boolean followsParent;

	protected HashSet<Component> components;
	protected HashMap<GameObject, PVector> children;
	protected float rotation = 0;

	// Event listener list
	protected HashMap<String, HashSet<EngineEventListener>> listeners = new HashMap<>(
			Map.ofEntries(Map.entry("update", new HashSet<>()), Map.entry("onHit", new HashSet<>())));
	protected HashMap<EngineEventListener, String> listenerToId = new HashMap<>();

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public PVector getPosition() {
		return position;
	}

	public PVector getSize() {
		return size;
	}

	public void setSize(PVector size) {
		this.size = size;
	}

	public HashSet<Component> getComponents() {
		return components;
	}

	public Iterable<GameObject> getChildren() {
		return children.keySet();
	}

	public void addChild(GameObject child, PVector offset) {
		children.put(child, offset);
		child.setPosition(PVector.add(position, Polygons.getRotatedVector(offset, rotation)));
	}

	public void setPosition(PVector position) {
		this.position = position;
		for (GameObject child : children.keySet()) {
			if (child.followsParent) {
				child.position = PVector.add(position, Polygons.getRotatedVector(children.get(child), rotation));
			}
		}
	}

	public void attach(Component component) {
		components.add(component);
		component.attach(this);
	}

	public void disattach(Component component) {
		components.remove(component);
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float angle) {
		rotation = angle;
		for (Component c : components) {
			if (c instanceof RigidBody) {
				((RigidBody) c).setRotation(angle);
			}
		}
		for (GameObject child : children.keySet()) {
			if (child.followsParent) {
				child.setRotation(angle);
			}
		}
	}

	public Iterable<EngineEventListener> getListeners(String id) {
		if (!listeners.containsKey(id)) {
			return null;
		}
		return listeners.get(id);
	}

	////////////////////////
	// GameObject methods //
	////////////////////////

	/**
	 * Method called on every director update.
	 */
	public void update() {
		for (GameObject c : children.keySet()) {
			c.update();
		}
	}

	/**
	 * Move the game object by a given increment.
	 *
	 * @param increment The vector to increment to the postion.
	 */
	public void move(PVector increment) {
		children.keySet().forEach((child) -> {
			if (followsParent)
				child.move(increment);
		});
		position = PVector.add(position, increment);
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
		listeners.get(listenerToId.get(listener)).remove(listener);
		listenerToId.remove(listener);
		return true;
	}

	//////////////////
	// Constructors //
	//////////////////

	/**
	 * Base constructor for a game object.
	 */
	public GameObject() {
		this(new PVector(0, 0));
	}

	public GameObject(PVector size) {
		this(size, new PVector(0, 0));
	}

	public GameObject(PVector size, PVector position) {
		this(size, position, true);
	}

	/**
	 * Detailed constructor for a game object.
	 *
	 * @param size          The object's size.
	 * @param position      The object's position.
	 * @param followsParent Whether the object follows it's parent object or not.
	 * @param components    The list of components to attach to the game object.
	 */
	public GameObject(PVector size, PVector position, boolean followsParent, Component... components) {
		this.position = position;
		this.size = size;
		this.followsParent = followsParent;
		this.components = new HashSet<>(Arrays.asList(components));
		for (Component c : this.components) {
			c.attach(this);
		}
		children = new HashMap<>();
	}
}
