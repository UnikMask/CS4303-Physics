package tankphysics.engine;

import java.util.List;

import processing.core.PVector;

/**
 * Class representing meshes for collision detection. CAUTION: Collision meshes
 * MUST be convex shapes. Accurate non-convex shape collision detection is not
 * yet supported.
 */
public class CollisionMesh implements Component {
	protected GameObject object;
	private MeshType meshType;
	private List<PVector> vertices;
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
		this.size = obj.getSize();
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

	public boolean requiresCollisionCheck(CollisionMesh mesh) {
		return (Math.abs(this.getObject().getPosition().x - mesh.getObject().getPosition().y) < this.getSize().x
				+ (mesh.getSize().x / 2))
				|| (Math.abs(this.getObject().getPosition().y - mesh.getObject().getPosition().y) < this.getSize().y
						+ (mesh.getSize().y / 2));
	}

	public boolean requiresCollisionCheck(RigidBody b) {
		for (CollisionMesh mesh : b.getHitbox()) {
			if (requiresCollisionCheck(mesh)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Apply inert object to rigid body collision check and bounce to the given
	 * RigidBody component.
	 *
	 * @param body The RigidBody component to check against.
	 */
	public boolean applyCollisionAndBounce(RigidBody body) {
		for (CollisionMesh m : body.getHitbox()) {
			MinkowskiDifference dist = queryFaceDist(this, m);
			MinkowskiDifference distB = queryFaceDist(m, this);
			if (distB.minkowskiDistance < dist.minkowskiDistance) {
				dist = distB;
			}

			// If there is collision - move object and return kinetic force
			if (dist.minkowskiDistance < -0.0f) {
				PVector plane = PVector.sub(dist.v2, dist.v1);
				PVector normal = new PVector(dist.reverseNormal ? plane.y : -plane.y,
						dist.reverseNormal ? -plane.x : plane.x).normalize();

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
				body.getObject().move(mvt);

				// Calculate impulse resolution
				float waste = body.getRoughness();
				PVector impulse = PVector.mult(normal,
						(-(1 + waste) * PVector.dot(body.getVelocity(), normal)) / (body.getInverseMass()));
				System.out.println("Impulse on " + this + ": [" + impulse.x + ", " + impulse.y + "]");

				if (mvt.mag() > 0.1) {
					body.applyImpulse(impulse, ptA);
					body.getObject().move(PVector.mult(normal, 0.2f * dist.minkowskiDistance));
				}
				return true;
			}
		}
		return false;
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
			return circleQueryFaceDist(polygonA, polygonB);
		}
		// Get distance to polygon - Apply SAT.
		MinkowskiDifference ret = new MinkowskiDifference(-Float.MAX_VALUE, polygonA, polygonA.vertices.get(0),
				polygonA.vertices.get(1));
		float reverseFactor = 1.0f;
		for (int i = 0; i < polygonA.vertices.size(); i++) {
			PVector vertex1 = PVector.add(polygonA.getObject().getPosition(), polygonA.vertices.get(i));
			PVector vertex2 = PVector.add(polygonA.getObject().getPosition(),
					polygonA.vertices.get((i + 1) % polygonA.vertices.size()));

			// Get normal of plane, get support from inverse of normal's direction, and
			// compare distances.
			PVector planeNormal = new PVector(PVector.sub(vertex2, vertex1).y, -PVector.sub(vertex2, vertex1).x)
					.mult(reverseFactor).normalize();
			PVector support = PVector.add(polygonB.getObject().getPosition(),
					polygonB.getSupportPoint(new PVector(-planeNormal.x, -planeNormal.y)));
			float dist = PVector.dot(PVector.sub(support, vertex1), planeNormal);

			// Assume 1st vertex negative - reverse normals if not.
			if (i == 0 && !ret.reverseNormal && dist > 0.0f) {
				reverseFactor = -1.0f;
				ret.reverseNormal = true;
				i -= 1;
			} else if (dist > ret.minkowskiDistance) {
				ret.minkowskiDistance = dist;
				ret.affectPoint = support;
				ret.v1 = vertex1;
				ret.v2 = vertex2;

				if (ret.minkowskiDistance > 0.0f) {
					return ret;
				}
			}
		}
		return ret;
	}

	/**
	 * Check if a circle mesh is colliding with a point of another polygon.
	 *
	 * @param sphere The circle to check with. Method returns null if mesh is a
	 *               polygon.
	 * @param mesh   The mesh to check points from. Can be either a polygon or a
	 *               circle.
	 *
	 * @return The Minkowski distance with points that make a tangent on the support
	 *         point of the circle's circle. Null if polygonA is a polygon mesh.
	 */
	public static MinkowskiDifference circleQueryFaceDist(CollisionMesh sphere, CollisionMesh mesh) {
		if (sphere.meshType != MeshType.CIRCLE) {
			return null;
		}
		float minDist = Float.MAX_VALUE;
		PVector pt = PVector.add(mesh.vertices.get(0), mesh.getObject().getPosition());

		if (mesh.meshType == MeshType.POLYGON) {
			for (PVector v : mesh.vertices) {
				v = PVector.add(mesh.getObject().getPosition(), v);
				float dist = PVector.dist(sphere.anchor, v);
				if (dist < minDist) {
					minDist = PVector.dist(sphere.anchor, v);
					pt = v;
				}
			}
		} else {
			minDist = PVector.sub(sphere.getObject().getPosition(), mesh.getObject().getPosition()).mag()
					- sphere.radius - mesh.radius;
		}
		PVector normal = PVector.sub(pt, sphere.anchor);
		return new MinkowskiDifference(minDist, sphere, PVector.add(normal, new PVector(normal.y, -normal.x)),
				PVector.add(normal, new PVector(-normal.y, normal.x)));
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
				float dot = v.dot(direction);
				if (dot > maxDot) {
					max = v;
					maxDot = dot;
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
	 * @param anchor    The anchor for the mesh's rotations.
	 * @param vertices  The mesh's vertices defining the edge of the mesh/polygon.
	 * @param roughness How rough the terrain is - i.e. How much force is required
	 *                  for the object to move without inertia.
	 */
	public CollisionMesh(PVector anchor, List<PVector> vertices, float roughness) {
		this.anchor = anchor;
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
	public CollisionMesh(PVector anchor, float radius, float roughness) {
		this.anchor = anchor;
		this.meshType = MeshType.CIRCLE;
		this.radius = radius;
		this.size = new PVector(radius * 2, radius * 2);
		this.friction = roughness;
	}
}
