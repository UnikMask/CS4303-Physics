package tankphysics.engine;

import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PVector;

public class GameObject {
	private PVector position;
	private PVector size;
	private boolean followsParent;

	private ArrayList<Component> components;
	private ArrayList<GameObject> children;

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public PVector getPosition() {
		return position;
	}

	public PVector getSize() {
		return size;
	}

	public ArrayList<Component> getComponents() {
		return components;
	}

	public ArrayList<GameObject> getChildren() {
		return children;
	}

	public void setPosition(PVector position) {
		this.position = position;
	}

	public void attach(Component component) {
		components.add(component);
		component.attach(this);
	}

	////////////////////////
	// GameObject methods //
	////////////////////////

	/**
	 * Method called on every director update.
	 */
	public void update() {
		for (GameObject c : children) {
			c.update();
		}
	}

	/**
	 * Move the game object by a given increment.
	 *
	 * @param increment The vector to increment to the postion.
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
		this.components = new ArrayList<>(Arrays.asList(components));
		for (Component c : this.components) {
			c.attach(this);
		}
		children = new ArrayList<>();
	}
}
