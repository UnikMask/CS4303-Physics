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
	private float friction;
	private PVector size;
	private PVector anchor;

	private static enum MeshType {
		CIRCLE, POLYGON
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

	public GameObject getObject() {
		return this.object;
	}

	////////////////////////////
	// Collision Mesh methods //
	////////////////////////////

	/**
	 * Apply inert object to rigid body collision check and bounce to the given
	 * RigidBody component.
	 *
	 * @param body The RigidBody component to check against.
	 */
	public void applyCollisionAndBounce(RigidBody body) {
		for (CollisionMesh m : body.getHitbox()) {
			MinkowskiDifference dist = queryFaceDist(this, m);

			// If there is collision - move object and return kinetic force
			if (dist.minkowskiDistance < -0.0f) {
				System.out.println("Mesh to Rigid Body collision success!");
				PVector plane = PVector.sub(dist.v2, dist.v1);
				PVector normal = new PVector(dist.reverseNormal ? plane.x : -plane.x,
						dist.reverseNormal ? -plane.y : plane.y).normalize();

				// Get points of contact on affected polygons.
				PVector ptA = dist.affectPoint;
				PVector ptB = PVector.add(dist.affectPoint, PVector.mult(normal, dist.minkowskiDistance));
				if (dist.parent != m) {
					PVector t = ptA;
					ptA = ptB;
					ptB = t;
				}
				// Move rigid body out of the mesh it overlaps
				PVector mvt = PVector.mult(PVector.div(body.getVelocity(), body.getVelocity().mag()),
						(dist.parent == this ? -1 : 1) * PVector.dot(body.getVelocity(), normal));
				System.out.println("Velocity: " + body.getVelocity() + ", Upforce [" + mvt.x + "," + mvt.y + "]");
				body.getObject().move(mvt);

				// Calculate impulse resolution
				float waste = body.getRoughness();
				PVector impulse = PVector.mult(normal,
						((1 + waste) * PVector.dot(body.getVelocity(), normal)) / (body.getInverseMass()));
				body.applyImpulse(dist.parent == m ? impulse : PVector.sub(new PVector(), impulse), ptA);
				body.getObject().move(PVector.mult(normal, 0.2f * dist.minkowskiDistance));
			}
		}
	}

	/**
	 * Check if the component collide with a given collision mesh.
	 *
	 * @param mesh The mesh to check for collision.
	 */
	public boolean collides(CollisionMesh mesh) {
		MinkowskiDifference rA = queryFaceDist(this, mesh);
		MinkowskiDifference rB = queryFaceDist(mesh, this);

		return Math.max(rA.minkowskiDistance, rB.minkowskiDistance) < 0;
	}

	/**
	 * Check if a polygon is colliding with the point of another polygon. Used the
	 * Minkowski Difference-based Separating Axis Theorem algorithm. Based on Dirk
	 * Gregorius's slides during GDC 2013. cf.
	 * https://gdcvault.com/play/1017646/Physics-for-Game-Programmers-The
	 *
	 * @param polygonA The polygon to check sides from.
	 * @param polygonB the polygon to check points from
	 *
	 * @return The vertices for which the edge has the closest distance, and the
	 *         minkowski distance of polygon B to the edge.
	 */
	public static MinkowskiDifference queryFaceDist(CollisionMesh polygonA, CollisionMesh polygonB) {
		if (polygonA.meshType == MeshType.CIRCLE) {
			// Polygon is a circle - get closest circle, and return distance from closest of
			// B vertices, to tangent on direction.
			float minDist = Float.MAX_VALUE;
			PVector pt = PVector.add(polygonB.vertices.get(0), polygonB.getObject().getPosition());
			for (PVector v : polygonB.vertices) {
				v = PVector.add(polygonB.getObject().getPosition(), v);
				float dist = PVector.dist(polygonA.anchor, v);
				if (dist < minDist) {
					minDist = PVector.dist(polygonA.anchor, v);
					pt = v;
				}
			}
			PVector normal = PVector.sub(pt, polygonA.anchor);
			return new MinkowskiDifference(minDist, polygonA, PVector.add(normal, new PVector(normal.y, -normal.x)),
					PVector.add(normal, new PVector(-normal.y, normal.x)));
		} else {
			// Get distance to polygon - Apply SAT.
			MinkowskiDifference ret = new MinkowskiDifference(Float.MIN_VALUE, polygonA, polygonA.vertices.get(0),
					polygonA.vertices.get(1));
			float reverseFactor = 1.0f;
			for (int i = 0; i < polygonA.vertices.size(); i++) {
				PVector v1 = PVector.add(polygonA.getObject().getPosition(), polygonA.vertices.get(i));
				PVector v2 = PVector.add(polygonA.getObject().getPosition(),
						polygonA.vertices.get((i + 1) % polygonA.vertices.size()));

				// Get normal of plane, get support from inverse of normal's direction, and
				// compare distances.
				PVector normal = new PVector(PVector.sub(v2, v1).x, -PVector.sub(v2, v1).y).mult(reverseFactor)
						.normalize();
				PVector support = PVector.add(polygonB.getObject().getPosition(),
						polygonB.getSupportPoint(new PVector(-normal.x, -normal.y)));
				float dist = PVector.dot(PVector.sub(support, v1), normal);
				if (dist > ret.minkowskiDistance) {
					ret.minkowskiDistance = dist;
					ret.affectPoint = support;
					ret.v1 = v1;
					ret.v2 = v2;
				}
				// Assume correct side on 1st vertices' plane, reverse normals if vertex is on
				// outside of 1st vertices' plane.
				if (i == 0 && ret.minkowskiDistance > 0.0f) {
					ret.minkowskiDistance = -ret.minkowskiDistance;
					reverseFactor = -1.0f;
					ret.reverseNormal = true;
				} else if (ret.minkowskiDistance > 0.0f) {
					return ret;
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
		this.friction = roughness;
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
		this.friction = roughness;
	}
}
