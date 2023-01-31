package tankphysics.engine;

import java.util.ArrayList;

import processing.core.PVector;

/**
 * Class representing meshes for collision detection. CAUTION: Collision meshes
 * MUST be convex shapes. Accurate non-convex shape collision detection is not
 * yet supported.
 */
public class CollisionMesh implements Component {
	protected GameObject object;
	private MeshType meshType;
	private ArrayList<PVector> vertices;
	private float radius;
	private float roughness;
	private PVector size;
	private PVector anchor;

	private static enum MeshType {
		CIRCLE, POLYGON
	}

	private static class MinkowskiDistance {
		PVector v1, v2;
		float minkowskiDistance;

		MinkowskiDistance(float dist, PVector v1, PVector v2) {
			this.v1 = v1;
			this.v2 = v2;
			this.minkowskiDistance = dist;
		}
	}

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public void attach(GameObject obj) {
		object = obj;
	}

	public PVector getSize() {
		return meshType == MeshType.POLYGON ? size : new PVector(radius * 2, radius * 2);
	}

	////////////////////////////
	// Collision Mesh methods //
	////////////////////////////

	/**
	 * Apply collision check and bounce to the given RigidBody component.
	 *
	 * @param body The RigidBody component to check against.
	 */
	public Force applyCollisionAndBounce(RigidBody body) {
		for (CollisionMesh m : body.getHitbox()) {
			if (collides(m)) {
				return new Force(PVector.mult(body.getVelocity(), 0.5f * body.getMass()), false, false);
			}
		}
		return null;
	}

	/**
	 * Check if the component collide with a given collision mesh.
	 *
	 * @param mesh The mesh to check for collision.
	 */
	public boolean collides(CollisionMesh mesh) {
		MinkowskiDistance rA = collides(this, mesh);
		MinkowskiDistance rB = collides(mesh, this);

		return Math.max(rA.minkowskiDistance, rB.minkowskiDistance) > 0;
	}

	/**
	 * Check if a polygon is colliding with the point of another polygon.
	 *
	 * @param polygonA The polygon to check sides from.
	 * @param polygonB the polygon to check points from
	 *
	 * @return The vertices for which the edge has the closest distance, and the
	 *         minkowski distance of polygon B to the edge.
	 */
	public static MinkowskiDistance collides(CollisionMesh polygonA, CollisionMesh polygonB) {
		if (polygonA.meshType == MeshType.CIRCLE) {
			return new MinkowskiDistance(Float.MIN_VALUE, new PVector(), new PVector());
		} else {
			MinkowskiDistance ret = new MinkowskiDistance(Float.MIN_VALUE, polygonA.vertices.get(0),
					polygonA.vertices.get(1));
			for (int i = 0; i < polygonA.vertices.size(); i++) {
				PVector v1 = polygonA.vertices.get(i);
				PVector v2 = polygonA.vertices.get((i + 1) % polygonA.vertices.size());

				PVector negNorm = new PVector(PVector.sub(v2, v1).x, -PVector.sub(v2, v1).y);
				PVector support = polygonB.getSupportPoint(negNorm);
				if (PVector.dot(support, negNorm) > ret.minkowskiDistance) {
					ret.minkowskiDistance = PVector.dot(support, negNorm);
					ret.v1 = v1;
					ret.v2 = v2;
				}
			}
			return ret;
		}
	}

	/**
	 * Get the support point of the mesh over a direction.
	 *
	 * @param direction The direction for which the support point is for.
	 */
	public PVector getSupportPoint(PVector direction) {
		if (meshType == MeshType.POLYGON) {
			PVector max = vertices.get(0);
			float maxDot = PVector.dot(max, direction);
			for (PVector v : vertices) {
				if (v.dot(direction) > maxDot) {
					max = v;
					maxDot = v.dot(direction);
				}
			}
			return max;
		} else {
			return PVector.mult(PVector.fromAngle(direction.heading()), radius);
		}
	}

	/////////////////
	// Constructor //
	/////////////////

	/**
	 * Constructor for a polygon-type CollisionMesh object.
	 *
	 * @param size      The mesh's bounds. Recalculated with object rotation.
	 * @param anchor    The anchor for the mesh's rotations.
	 * @param vertices  The mesh's vertices defining the edge of the mesh/polygon.
	 * @param roughness How rough the terrain is - i.e. How much force is required
	 *                  for the object to move without inertia.
	 */
	public CollisionMesh(PVector size, PVector anchor, ArrayList<PVector> vertices, float roughness) {
		this.size = size;
		this.meshType = MeshType.POLYGON;
		this.vertices = vertices;
		this.roughness = roughness;
	}

	/**
	 * Constructor for a circle-type CollisionMesh object.
	 *
	 * @param size      The mesh's bounds. Recalculated with object rotation.
	 * @param vertices  The mesh's vertices defining the edge of the mesh/polygon.
	 * @param roughness How rough the terrain is - i.e. How much force is required
	 *                  for the object to move without inertia.
	 */
	public CollisionMesh(PVector size, PVector anchor, float radius, float roughness) {
		this.size = size;
		this.meshType = MeshType.POLYGON;
		this.radius = radius;
		this.roughness = roughness;
	}
}
