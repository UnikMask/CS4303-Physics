package tankphysics.engine;

import processing.core.PVector;

/**
 * Interface for physical objects, used in collision calculations.
 */
public interface PhysicalObject {
	public static final float CORRECTION_THRESHOLD = 0.1f;
	public static final float CORRECTION_PERCENTAGE = 0.5f;

	public float getInverseMass();

	public PVector getVelocity();

	public PVector getPosition();

	public PVector getSize();

	public float getOrientation();

	public void setOrientation(float radians);

	public float getRotationalVelocity();

	public void setRotationalVelocity(float velocity);

	public void setPosition(PVector position);

	public void setVelocity(PVector velocity);

	public Iterable<CollisionMesh> getMeshes();

	/**
	 * Get collision details between 2 objects. Returns null if the 2 objects are
	 * not colliding.
	 *
	 * @param objA The 1st object to check.
	 * @param objB The object to check against.
	 *
	 * @return The collision details between both objects, or null if those are not
	 *         colliding.
	 */
	public static CollisionDetails getCollisionDetails(PhysicalObject objA, PhysicalObject objB) {
		for (CollisionMesh vA : objA.getMeshes()) {
			for (CollisionMesh vB : objB.getMeshes()) {
				CollisionDetails collideAtoB = CollisionMesh.queryFaceDist(vA, vB, objA, objB);
				if (collideAtoB == null) {
					continue;
				}

				CollisionDetails collideBtoA = CollisionMesh.queryFaceDist(vB, vA, objB, objA);
				if (collideBtoA == null) {
					continue;
				} else if (collideBtoA.penetration > collideAtoB.penetration) {
					return collideBtoA;
				}
				return collideAtoB;
			}
		}
		return null;
	}

	/**
	 * Apply impulse resolution from collision details given.
	 *
	 * @param details The collision details containing the objects to resolve
	 *                impulse from.
	 */
	public static boolean applyImpulseResolution(CollisionDetails details) {
		PhysicalObject objA = details.objA;
		PhysicalObject objB = details.objB;

		// Check if objects are moving into each other from the normal's point of view.
		PVector relativeVelocity = PVector.sub(objB.getVelocity(), objA.getVelocity());
		float velocityProjectionOnNormal = PVector.dot(relativeVelocity, details.normal);
		if (velocityProjectionOnNormal > 0) {
			return false;
		}

		// Calculate impulse resolution
		float totalBounce = details.meshA.getBounciness() * details.meshB.getBounciness();
		float impulseFactor = -(1 + totalBounce) * velocityProjectionOnNormal
				/ (objA.getInverseMass() + objB.getInverseMass());
		objA.setVelocity(
				PVector.sub(objA.getVelocity(), PVector.mult(details.normal, impulseFactor * objA.getInverseMass())));
		objB.setVelocity(
				PVector.add(objB.getVelocity(), PVector.mult(details.normal, impulseFactor * objB.getInverseMass())));

		applyFriction(details, impulseFactor);

		return applySinkingCorrection(details);
	}

	public static void applyFriction(CollisionDetails details, float normalFactor) {
		PhysicalObject objA = details.objA;
		PhysicalObject objB = details.objB;

		// Recalculate relative velocity and velocity along normal for friction
		// calculation.
		PVector relativeVelocity = PVector.sub(objB.getVelocity(), objA.getVelocity());
		float velocityProjectionOnNormal = PVector.dot(relativeVelocity, details.normal);
		PVector tan = PVector.sub(relativeVelocity, PVector.mult(details.normal, velocityProjectionOnNormal))
				.normalize();

		// Factor on friction - breaks object speed on contact by roughness of material.
		float staticFactor = details.meshA.getStaticFriction() * details.meshB.getStaticFriction();
		float dynamicFactor = details.meshA.getDynamicFriction() * details.meshB.getDynamicFriction();
		float frictionFactor = -PVector.dot(relativeVelocity, tan) / (objA.getInverseMass() + objB.getInverseMass());

		PVector frictionImpulse = PVector.mult(tan, frictionFactor);
		if (Math.abs(frictionFactor) > Math.abs(staticFactor * normalFactor)) {
			frictionImpulse = PVector.mult(tan, -normalFactor * dynamicFactor);
		}
		objA.setVelocity(PVector.sub(objA.getVelocity(), PVector.mult(frictionImpulse, objA.getInverseMass())));
		objB.setVelocity(PVector.add(objB.getVelocity(), PVector.mult(frictionImpulse, objB.getInverseMass())));
	}

	public static boolean applySinkingCorrection(CollisionDetails details) {
		PhysicalObject objA = details.objA;
		PhysicalObject objB = details.objB;

		// Correct positional sinking between both objects, with jitter threshold
		float inverseTotalMass = 1 / (objA.getInverseMass() + objB.getInverseMass());
		if (-details.penetration > CORRECTION_THRESHOLD) {
			objA.setPosition(PVector.add(objA.getPosition(), PVector.mult(details.normal,
					details.penetration * CORRECTION_PERCENTAGE * objA.getInverseMass() * inverseTotalMass)));
			objB.setPosition(PVector.sub(objB.getPosition(), PVector.mult(details.normal,
					details.penetration * CORRECTION_PERCENTAGE * objB.getInverseMass() * inverseTotalMass)));
		}
		System.out.println(details);
		return -details.penetration > CORRECTION_THRESHOLD;
	}

	public static boolean applyCollisionAndBounce(PhysicalObject objA, PhysicalObject objB) {
		// If there is collision - move object and return kinetic force
		CollisionDetails details = PhysicalObject.getCollisionDetails(objA, objB);

		if (details != null) {
			return PhysicalObject.applyImpulseResolution(details);
		}
		return false;
	}

	/**
	 * Do a broad O(1) complexity collision check between 2 physical objects to
	 * determine whether a more complex collision check is required.
	 *
	 * @param objA The 1st physical object to check against.
	 * @param objB The 2nd physical object to check against.
	 *
	 * @return Whether the broad collision check returns positive or not.
	 */
	public static boolean requiresCollisionCheck(PhysicalObject objA, PhysicalObject objB) {
		PVector dist = PVector.sub(objA.getPosition(), objB.getPosition());
		PVector minDist = PVector.div(PVector.add(objA.getSize(), objB.getSize()), 2);
		if (Math.abs(dist.x) < minDist.x && Math.abs(dist.y) < minDist.y) {
			return true;
		}
		return false;
	}
}
