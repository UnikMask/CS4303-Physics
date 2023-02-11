package tankphysics.engine;

import java.util.Arrays;
import java.util.HashSet;
import processing.core.PVector;

public class GameObject {
  private PVector position;
  private PVector size;
  private boolean followsParent;

  private HashSet<Component> components;
  private HashSet<GameObject> children;
  private float rotation = 0;

  /////////////////////////
  // Getters and Setters //
  /////////////////////////

  public PVector getPosition() { return position; }

  public PVector getSize() { return size; }

  public void setSize(PVector size) { this.size = size; }

  public HashSet<Component> getComponents() { return components; }

  public HashSet<GameObject> getChildren() { return children; }

  public void setPosition(PVector position) { this.position = position; }

  public void attach(Component component) {
    components.add(component);
    component.attach(this);
  }

  public void disattach(Component component) { components.remove(component); }

  public float getRotation() { return rotation; }

  public void setRotation(float angle) {
    rotation = angle;
    for (Component c : components) {
      if (c instanceof RigidBody) {
        ((RigidBody)c).setRotation(angle);
      }
    }
    for (GameObject child : children) {
      if (child.followsParent) {
        child.setRotation(angle);
      }
    }
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
    position = PVector.add(position, increment);
  }

  //////////////////
  // Constructors //
  //////////////////

  /**
   * Base constructor for a game object.
   */
  public GameObject() { this(new PVector(0, 0)); }

  public GameObject(PVector size) { this(size, new PVector(0, 0)); }

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
  public GameObject(PVector size, PVector position, boolean followsParent,
                    Component... components) {
    this.position = position;
    this.size = size;
    this.followsParent = followsParent;
    this.components = new HashSet<>(Arrays.asList(components));
    for (Component c : this.components) {
      c.attach(this);
    }
    children = new HashSet<>();
  }
}
