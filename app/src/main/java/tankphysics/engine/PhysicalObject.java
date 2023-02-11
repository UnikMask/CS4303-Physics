package tankphysics.engine;

import processing.core.PVector;

/**
 * Interface for physical objects, used in collision calculations.
 */
public interface PhysicalObject {
	public static final float CORRECTION_THRESHOLD = 0.01f;
	public static final float CORRECTION_PERCENTAGE = 0.2f;

	///////////////////////
	// Interface Methods //
	///////////////////////

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

	public PVector getCOM();

	public float getInverseInertia();

	public Iterable<CollisionMesh> getMeshes();

	public void applyImpulse(PVector impulse, PVector affectPt);

	////////////////////
	// Static Methods //
	////////////////////

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

		// Get radius of the affected point on A and B
		PVector radiusA = PVector.sub(details.affectPoint, objA.getCOM()).sub(objA.getPosition());
		PVector radiusB = PVector.sub(details.affectPoint, objB.getCOM()).sub(objB.getPosition());

		// Check if objects are moving into each other from the normal's point of view.
		PVector relativeVelocity = PVector.sub(objB.getVelocity(), objA.getVelocity())
				.sub(new PVector(-radiusA.y, radiusA.x).mult(objA.getRotationalVelocity()))
				.add(new PVector(-radiusB.y, radiusB.x).mult(objB.getRotationalVelocity()));
		float velocityProjectionOnNormal = PVector.dot(relativeVelocity, details.normal);
		if (velocityProjectionOnNormal > 0) {
			return false;
		}

		// Calculate impulse resolution
		float totalBounce = details.meshA.getBounciness() * details.meshB.getBounciness();
		float radACrossNormal = (float) Math.pow(radiusA.cross(details.normal).z, 2);
		float radBCrossNormal = (float) Math.pow(radiusB.cross(details.normal).z, 2);
		float factorDiv = objA.getInverseMass() + objB.getInverseMass() + radACrossNormal * objA.getInverseInertia()
				+ radBCrossNormal * objB.getInverseInertia();
		float impulseFactor = -(1 + totalBounce) * velocityProjectionOnNormal / factorDiv;
		PVector impulse = PVector.mult(details.normal, impulseFactor);

		objA.applyImpulse(PVector.mult(impulse, -1), radiusA);
		objB.applyImpulse(impulse, radiusB);

		applyFriction(details, impulseFactor, factorDiv);

		return applySinkingCorrection(details);
	}

	public static void applyFriction(CollisionDetails details, float normalFactor, float factorDiv) {
		PhysicalObject objA = details.objA;
		PhysicalObject objB = details.objB;

		// Get radius of the affected point on A and B
		PVector radiusA = PVector.sub(details.affectPoint, objA.getCOM()).sub(objA.getPosition());
		PVector radiusB = PVector.sub(details.affectPoint, objB.getCOM()).sub(objB.getPosition());

		// Recalculate relative velocity and velocity along normal for friction
		// calculation.
		PVector relativeVelocity = PVector.sub(objB.getVelocity(), objA.getVelocity());
		PVector tan = PVector
				.sub(relativeVelocity, PVector.mult(details.normal, PVector.dot(relativeVelocity, details.normal)))
				.normalize();
		float velocityProjectionOnTan = PVector.dot(relativeVelocity, tan);

		// Factor on friction - breaks object speed on contact by roughness of material.
		float staticFactor = details.meshA.getStaticFriction() * details.meshB.getStaticFriction();
		float dynamicFactor = details.meshA.getDynamicFriction() * details.meshB.getDynamicFriction();
		float frictionFactor = -velocityProjectionOnTan / factorDiv;

		PVector frictionImpulse = PVector.mult(tan, frictionFactor);
		if (Math.abs(frictionFactor) > Math.abs(staticFactor * normalFactor)) {
			frictionImpulse = PVector.mult(tan, -normalFactor * dynamicFactor);
		}
		objA.applyImpulse(PVector.mult(frictionImpulse, -1), radiusA);
		objB.applyImpulse(frictionImpulse, radiusB);
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
