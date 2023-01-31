package tankphysics.engine;

import java.util.HashSet;
import java.util.stream.Stream;

import processing.core.PVector;

public class RigidBody implements Component {
	private GameObject object;
	private HashSet<CollisionMesh> hitbox;

	// Mass and linear force variables.
	private PVector velocity;
	private float mass;
	private float inverseMass;

	// Torque related variables.
	private float rotationalVelocity;

	// Inertia related variables.
	private float roughness;

	public void attach(GameObject object) {
		this.object = object;
	}

	/**
	 * Attach some CollisionMesh components to the RigidBody component as part of
	 * its hitbox. The CollisionMesh component must belong to the same GameObject.
	 *
	 * @param meshes The meshes to attach to the RigidBody component.
	 */
	public void attachToHitbox(CollisionMesh... meshes) {
		for (CollisionMesh m : meshes) {
			if (this.object != null && m.object == this.object && !hitbox.contains(m)) {
				hitbox.add(m);
			}
		}
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

	public float getMass() {
		return mass;
	}

	public void setMass(float mass) {
		this.mass = mass;
		this.inverseMass = 1 / mass;
	}

	public PVector getVelocity() {
		return velocity;
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
		object.setPosition(PVector.add(object.getPosition(), PVector.mult(velocity, pixelsPerUnit * deltaT)));
	}

	/**
	 * Constructor for a RigidBody component.
	 *
	 * @param mass      The object's overall mass.
	 * @param roughness The object's roughness - i.e. How sticky the object is on
	 *                  surfaces without inertia applied.
	 */
	public RigidBody(float mass, float roughness) {
		this.mass = mass;
		this.inverseMass = 1 / mass;
		this.rotationalVelocity = 0.0f;
		this.roughness = roughness;
		this.hitbox = new HashSet<>();
	}
}
