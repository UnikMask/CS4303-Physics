package tankphysics.engine;

import processing.core.PVector;

public class MinkowskiDifference {
	PVector v1, v2;
	PVector affectPoint = new PVector();
	float minkowskiDistance;
	boolean reverseNormal = false;
	CollisionMesh parent;

	MinkowskiDifference(float dist, CollisionMesh parent, PVector v1, PVector v2) {
		this.v1 = v1;
		this.v2 = v2;
		this.parent = parent;
		this.minkowskiDistance = dist;
	}
}
