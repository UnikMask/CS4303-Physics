package tankphysics.engine;

import java.util.HashSet;
import java.util.stream.Stream;

import processing.core.PVector;

public class RigidBody implements Component, PhysicalObject {
	private GameObject object;
	private HashSet<CollisionMesh> hitbox;
	private PVector anchor = new PVector();
	private PVector size;

	// Mass and linear force variables.
	private PVector velocity = new PVector();
	private float mass;
	private float inverseMass;
	private float inverseInertiaSquared;

	// Torque related variables.
	private float rotationalVelocity = 0f;
	private float torque = 0f;

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public float getMass() {
		return mass;
	}

	public void setMass(float mass) {
		this.mass = mass;
		this.inverseMass = 1 / mass;
	}

	public PVector getVelocity() {
		return velocity.copy();
	}

	public void setVelocity(PVector velocity) {
		this.velocity = velocity;
	}

	public float getRotationalVelocity() {
		return rotationalVelocity;
	}

	public void setRotationalVelocity(float rotationalVelocity) {
		this.rotationalVelocity = rotationalVelocity;
	}

	public Iterable<CollisionMesh> getHitbox() {
		return hitbox;
	}

	public GameObject getObject() {
		return object;
	}

	public PVector getAnchor() {
		return anchor;
	}

	public void setRotation(float angle) {
		for (CollisionMesh m : hitbox) {
			m.setOrientation(angle, this);
		}
		this.size = Polygons.getRotatedBoxSize(this.object.getSize(), angle);
	}

	public float getInverseInertiaSquared() {
		return inverseInertiaSquared;
	}

	////////////////////////
	// Interface Methods //
	////////////////////////

	public void attach(GameObject object) {
		this.object = object;
		for (CollisionMesh m : hitbox) {
			m.object = object;
		}
		this.size = this.object.getSize().copy();
	}

	public Iterable<CollisionMesh> getMeshes() {
		return getHitbox();
	}

	public void setPosition(PVector position) {
		object.setPosition(position);
	}

	public PVector getPosition() {
		return object.getPosition().copy();
	}

	public PVector getSize() {
		return size;
	}

	public float getTorque() {
		return torque;
	}

	public float getInverseMass() {
		return inverseMass;
	}

	public float getOrientation() {
		return object.getRotation();
	}

	public void setOrientation(float angle) {
		// Call the game object to propagate rotation to all components of the game
		// object.
		object.setRotation(angle);
	}

	/////////////////////////////////
	// RigidBody Component Methods //
	/////////////////////////////////

	public void applyImpulse(PVector impulse, PVector contactPt) {
		velocity = PVector.add(velocity, PVector.mult(impulse, inverseMass));
	}

	/**
	 * Attach some CollisionMesh components to the RigidBody component as part of
	 * its hitbox. The CollisionMesh component must belong to the same GameObject.
	 *
	 * @param meshes The meshes to attach to the RigidBody component.
	 */
	public void attachToHitbox(CollisionMesh... meshes) {
		for (CollisionMesh m : meshes) {
			if (m.object == null) {
				hitbox.add(m);
				m.object = this.object;
			}
		}
		calculateAndStoreInertia();
	}

	/* Recalculate the inertia of the object from */
	private void calculateAndStoreInertia() {
		float inertia = 0;

		for (CollisionMesh m : hitbox) {
			for (PVector v : m.getVertices()) {
				inertia += (float) Math
						.pow(PVector.sub(v, anchor).mag() * getMass() / (hitbox.size() * m.getNumVertices()), 2);
			}
		}
		inverseInertiaSquared = 1 / (float) Math.pow(inertia, 2);
	}

	/**
	 * Disattach a given collision mesh from the rigid body's hitbox. The mesh must
	 * already be in the hitbox.
	 *
	 * @param meshes The CollisionMesh components belonging to the object to remove
	 *               from the RigidBody component's hitbox.
	 */
	public void disattachFromHitbox(CollisionMesh... meshes) {
		for (CollisionMesh m : meshes) {
			if (hitbox.contains(m)) {
				hitbox.remove(m);
			}
		}
	}

	/**
	 * Apply a list of forces to a rigid body.
	 *
	 * @param forces The list of forces to apply to the rigid body.
	 * @param deltaT The amount of time
	 */
	public void apply(Stream<Force> forces, float deltaT, float pixelsPerUnit) {
		forces.forEach((f) -> {
			PVector acc = f.getVectorForce();
			if (!f.isMassProportional()) {
				acc.mult(inverseMass);
			}
			velocity.add(PVector.mult(acc, deltaT));
		});
		// Update new velocity to the game object's position.
		setRotationalVelocity(getRotationalVelocity() + torque * deltaT);
		object.setPosition(PVector.add(object.getPosition(), PVector.mult(velocity, pixelsPerUnit * deltaT)));
		setOrientation(getOrientation() + getRotationalVelocity() * deltaT);
	}

	/**
	 * Constructor for a RigidBody component.
	 *
	 * @param mass      The object's overall mass.
	 * @param roughness The object's roughness - i.e. How sticky the object is on
	 *                  surfaces without inertia applied.
	 */
	public RigidBody(float mass) {
		this.mass = mass;
		this.inverseMass = 1 / mass;
		this.hitbox = new HashSet<>();
	}
}
