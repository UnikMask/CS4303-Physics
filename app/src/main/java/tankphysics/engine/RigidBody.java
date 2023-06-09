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
	private float inverseInertia;

	// Torque related variables.
	private float rotationalVelocity = 0f;
	private float torque = 0f;
	private float impulseMultipler = 1f;

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

	public float getMultiplier() {
		return impulseMultipler;
	}

	public void setMultiplier(float multiplier) {
		impulseMultipler = multiplier;
	}

	public GameObject getObject() {
		return object;
	}

	public PVector getCOM() {
		return anchor;
	}

	public void setRotation(float angle) {
		for (CollisionMesh m : hitbox) {
			m.setOrientation(angle, this);
		}
		this.size = Polygons.getRotatedBoxSize(this.object.getSize(), angle);
	}

	public float getInverseInertia() {
		return inverseInertia;
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

	public boolean addForCollisions() {
		return true;
	}

	/////////////////////////////////
	// RigidBody Component Methods //
	/////////////////////////////////

	public void applyImpulse(PVector impulse, PVector contactPt, PhysicalObject ref, boolean checkRotationalVelocity) {
		// Call event listeners that obey on impulse
		for (EngineEventListener l : getObject().getListeners("impulse")) {
			l.call(ref.getObject(), impulse);
		}
		velocity = PVector.add(velocity, PVector.mult(impulse, getInverseMass()));
		if (checkRotationalVelocity) {
			rotationalVelocity += inverseInertia * contactPt.cross(impulse).z;
		}
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
			if (m.getMeshType() == CollisionMesh.MeshType.POLYGON) {
				for (PVector v : m.getVertices()) {
					inertia += (float) Math.pow(PVector.sub(v, anchor).mag(), 2)
							* (getMass() / (hitbox.size() * m.getNumVertices()));
				}
			}
		}
		inverseInertia = inertia != 0 ? 1 / inertia : 0;
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
	public void apply(Stream<Force> forces, float deltaT) {
		forces.forEach((f) -> {
			PVector acc = f.getVectorForce();
			if (!f.isMassProportional()) {
				acc.mult(inverseMass);
			}
			velocity.add(PVector.mult(acc, deltaT));
		});
		// Update new velocity to the game object's position.
		object.setPosition(PVector.add(object.getPosition(), PVector.mult(velocity, deltaT)));
		setRotationalVelocity(getRotationalVelocity() + torque * deltaT);
		setOrientation(getOrientation() + getRotationalVelocity() * deltaT);
	}

	/**
	 * Constructor for a RigidBody component.
	 *
	 * @param mass      The object's overall mass.
	 * @param roughness The object's roughness - i.e. How sticky the object is on
	 *                  surfaces without inertia applied.
	 */
	public RigidBody(float mass, CollisionMesh... hitbox) {
		this.mass = mass;
		this.inverseMass = 1 / mass;
		this.hitbox = new HashSet<>();
		attachToHitbox(hitbox);
	}
}
