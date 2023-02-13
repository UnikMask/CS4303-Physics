package tankphysics.engine;

import java.util.List;

import processing.core.PVector;

/**
 * Class representing collision details between 2 objects. The normal belongs to
 * object A, as the affect point belongs to object B.
 */
public class CollisionDetails {
	PVector normal;
	List<PVector> affectPoints;
	float penetration;
	PhysicalObject objA;
	PhysicalObject objB;
	CollisionMesh meshA;
	CollisionMesh meshB;

	@Override
	public String toString() {
		return "Collision details: {\n\tobject A: " + objA + ", \n\tobject B: " + objB + ",\n\tnormal: [" + normal.x
				+ "," + normal.y + "],\n\tpenetration: " + penetration + ",\n}";
	}

	public CollisionDetails(float dist, PhysicalObject objA, PhysicalObject objB, CollisionMesh meshA,
			CollisionMesh meshB, PVector normal, List<PVector> affectPoints) {
		this.penetration = dist;
		this.objA = objA;
		this.objB = objB;
		this.meshA = meshA;
		this.meshB = meshB;
		this.normal = normal;
		this.affectPoints = affectPoints;
	}
}
