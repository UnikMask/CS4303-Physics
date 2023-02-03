package tankphysics.engine;

import java.util.HashSet;
import java.util.stream.Stream;

import processing.core.PVector;

public class RigidBody implements Component {
	private GameObject object;
	private HashSet<CollisionMesh> hitbox;
	private PVector anchor = new PVector();

	// Mass and linear force variables.
	private PVector velocity = new PVector();
	private float mass;
	private float inverseMass;

	// Torque related variables.
	private float rotationalVelocity;

	// Inertia related variables.
	private float roughness;

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

	public Iterable<CollisionMesh> getHitbox() {
		return hitbox;
	}

	public GameObject getObject() {
		return object;
	}

	public PVector getAnchor() {
		return anchor;
	}

	public float getInverseMass() {
		return inverseMass;
	}

	public float getRoughness() {
		return roughness;
	}

	/////////////////////////
	// Component Interface //
	/////////////////////////

	public void attach(GameObject object) {
		this.object = object;
	}

	/////////////////////////////////
	// RigidBody Component Methods //
	/////////////////////////////////

	/**
	 * Apply rigid body to rigid body collision check and bounce to the 2 given
	 * RigidBody components.
	 *
	 * @param bodyA The 1st rigid body to check for collisions.
	 * @param bodyB The 2nd rigid body to check for collisions.
	 */
	public static void applyCollisionAndBounce(RigidBody bodyA, RigidBody bodyB) {
		for (CollisionMesh mA : bodyA.getHitbox()) {
			for (CollisionMesh mB : bodyB.getHitbox()) {
				// Calculate Minkowski difference to check for collision.
				MinkowskiDifference dist = CollisionMesh.queryFaceDist(mA, mB);
				MinkowskiDifference distB = CollisionMesh.queryFaceDist(mB, mA);
				if (distB.minkowskiDistance < dist.minkowskiDistance) {
					dist = distB;
				}

				// On collision
				if (dist.minkowskiDistance < 0.0f) {
					PVector plane = PVector.sub(dist.v2, dist.v1);
					PVector normal = new PVector(dist.reverseNormal ? plane.x : -plane.x,
							dist.reverseNormal ? -plane.y : plane.y);

					// Get affected points on A and B
					PVector ptA = dist.affectPoint;
					PVector ptB = PVector.add(dist.affectPoint, PVector.mult(normal, dist.minkowskiDistance));
					if (dist.parent != mA) {
						PVector t = ptA;
						ptA = ptB;
						ptB = t;
					}

					// Get radius from center of mass.
					// PVector radiusA = PVector.sub(ptA, bodyA.anchor);
					// PVector radiusB = PVector.sub(ptB, bodyB.getAnchor());

					// Move 1st rigid body out of collision.
					RigidBody body = (dist.parent == mA ? bodyB : bodyA);
					body.getObject().move(PVector.mult(PVector.div(body.getVelocity(), body.getVelocity().mag()),
							PVector.dot(body.getVelocity(), normal)));

					// Calculate impulse resolution
					float waste = bodyA.roughness * bodyB.roughness;
					PVector vDiff = PVector.sub(bodyB.getVelocity(), bodyA.getVelocity());

					PVector impulse = PVector.mult(normal,
							(-(1 + waste) * PVector.dot(vDiff, normal)) / (bodyA.inverseMass + bodyB.inverseMass));
					bodyA.applyImpulse(dist.parent == mB ? impulse : PVector.sub(new PVector(), impulse), ptA);
					bodyB.applyImpulse(dist.parent == mA ? impulse : PVector.sub(new PVector(), impulse), ptB);
				}
			}
		}
	}

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
