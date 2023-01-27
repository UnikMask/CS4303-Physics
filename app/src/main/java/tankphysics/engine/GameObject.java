package tankphysics.engine;

import java.util.ArrayList;

import processing.core.PVector;

public class GameObject {
	PVector position;
	PVector size;
	boolean followsParent;

	ArrayList<Component> components;
	ArrayList<GameObject> children;

	public PVector getPosition() {
		return position;
	}

	public PVector getSize() {
		return size;
	}

	public ArrayList<Component> getComponents() {
		return components;
	}

	/**
	 * Move the game object by a given increment.
	 */
	public void move(PVector increment) {
		children.forEach((child) -> {
			if (followsParent)
				child.move(increment);
		});
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

	public GameObject(PVector size, PVector position, boolean followsParent) {
		this.position = position;
		this.size = size;
		this.followsParent = followsParent;
	}
}
