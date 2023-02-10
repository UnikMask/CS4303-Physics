package tankphysics.engine;

import java.util.Arrays;
import java.util.List;

import processing.core.PVector;

/**
 * Class representing meshes for collision detection. CAUTION: Collision meshes
 * MUST be convex shapes. Accurate non-convex shape collision detection is not
 * yet supported.
 */
public class CollisionMesh implements Component, PhysicalObject {
	protected GameObject object;
	private MeshType meshType;
	private List<PVector> vertices;
	private float radius;
	private float friction;
	private float bounciness;
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

	public float getBounciness() {
		return bounciness;
	}

	///////////////////////
	// Interface Methods //
	///////////////////////

	public Iterable<CollisionMesh> getMeshes() {
		return Arrays.asList(this);
	}

	public GameObject getObject() {
		return this.object;
	}

	public float getInverseMass() {
		return 0;
	}

	public PVector getVelocity() {
		return new PVector();
	}

	public void setPosition(PVector position) {
	}

	public PVector getPosition() {
		return object.getPosition();
	}

	public void setVelocity(PVector velocity) {
	}

	////////////////////////////
	// Collision Mesh methods //
	////////////////////////////

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
	public static CollisionDetails queryFaceDist(CollisionMesh polygonA, CollisionMesh polygonB, PhysicalObject objA,
			PhysicalObject objB) {
		if (polygonA.meshType == MeshType.CIRCLE) {
			return circleQueryFaceDist(polygonA, polygonB, objA, objB);
		}
		// Get distance to polygon - Apply SAT.
		CollisionDetails ret = new CollisionDetails(-Float.MAX_VALUE, objA, objB, polygonA, polygonB, null, null);
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
			if (i == 0 && dist > 0.0f) {
				reverseFactor = -1.0f;
				i -= 1;
			} else if (dist > ret.penetration) {
				ret.penetration = dist;
				ret.affectPoint = support;
				ret.normal = planeNormal;

				if (ret.penetration > 0.0f) {
					return null;
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
	public static CollisionDetails circleQueryFaceDist(CollisionMesh sphere, CollisionMesh mesh, PhysicalObject objA,
			PhysicalObject objB) {
		if (sphere.meshType != MeshType.CIRCLE) {
			return null;
		}

		float minDist = Float.MAX_VALUE;
		PVector affectPoint = null;
		if (mesh.meshType == MeshType.POLYGON) {
			for (PVector v : mesh.vertices) {
				v = PVector.add(mesh.getObject().getPosition(), v);
				float dist = PVector.dist(sphere.anchor, v);
				if (dist < minDist) {
					minDist = PVector.dist(sphere.anchor, v);
					affectPoint = v;
				}
			}
		} else {
			minDist = PVector.sub(sphere.getObject().getPosition(), mesh.getObject().getPosition()).mag()
					- sphere.radius - mesh.radius;
		}
		PVector normal = PVector.sub(affectPoint, sphere.anchor);
		return new CollisionDetails(minDist, objA, objB, sphere, mesh, normal, affectPoint);
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
	public CollisionMesh(PVector anchor, List<PVector> vertices, float roughness, float bounciness) {
		this.anchor = anchor;
		this.meshType = MeshType.POLYGON;
		this.vertices = vertices;
		this.friction = roughness;
		this.bounciness = bounciness;
		this.bounciness = 1 - roughness;
	}

	/**
	 * Constructor for a circle-type CollisionMesh object.
	 *
	 * @param size     The mesh's bounds. Recalculated with object rotation.
	 * @param vertices The mesh's vertices defining the edge of the mesh/polygon.
	 * @param friction How rough the terrain is - i.e. How much force is required
	 *                 for the object to move without inertia.
	 */
	public CollisionMesh(PVector anchor, float radius, float friction, float bounciness) {
		this.anchor = anchor;
		this.meshType = MeshType.CIRCLE;
		this.radius = radius;
		this.size = new PVector(radius * 2, radius * 2);
		this.friction = friction;
		this.bounciness = bounciness;
		this.bounciness = 1 - friction;
	}
}
