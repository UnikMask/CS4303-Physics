package tankphysics.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
	private List<PVector> storageVertices;
	private float radius;

	private PVector size;
	private PVector anchor;
	private PVector savedAnchor;

	// Material Properties
	private float staticFriction = 1f;
	private float dynamicFriction = 1f;
	private float bounciness = 1f;

	// Optimisation features on rotation
	private float ROTATION_CALC_THRESHOLD = 0.01f;
	private float savedAngle;

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

	public float getStaticFriction() {
		return staticFriction;
	}

	public float getDynamicFriction() {
		return dynamicFriction;
	}

	public void setOrientation(float angle, RigidBody body) {
		if (Math.abs(angle - this.savedAngle) > ROTATION_CALC_THRESHOLD) {
			vertices = Polygons.getRotatedVertices(storageVertices, anchor, angle);
			anchor = Polygons.getRotatedVector(savedAnchor, angle);
			this.savedAngle = angle;
		}
	}

	public int getNumVertices() {
		return vertices.size();
	}

	public Iterable<PVector> getVertices() {
		return vertices;
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
		return PVector.add(object.getPosition(), anchor);
	}

	public void setVelocity(PVector velocity) {
	}

	public float getOrientation() {
		return 0;
	}

	public void setOrientation(float angle) {
	}

	public float getRotationalVelocity() {
		return 0;
	}

	public void setRotationalVelocity(float velocity) {
		return;
	}

	public float getInverseInertia() {
		return 0;
	}

	public PVector getCOM() {
		return anchor;
	}

	public void applyImpulse(PVector impulse, PVector contactPt) {
		return;
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
		CollisionDetails ret = new CollisionDetails(-Float.MAX_VALUE, objA, objB, polygonA, polygonB, null,
				new ArrayList<>());
		float reverseFactor = 1.0f;
		PVector p1 = new PVector(), p2 = new PVector();

		for (int i = 0; i < polygonA.vertices.size(); i++) {
			PVector vertex1 = PVector.add(polygonA.getObject().getPosition(), polygonA.vertices.get(i));
			PVector vertex2 = PVector.add(polygonA.getObject().getPosition(),
					polygonA.vertices.get((i + 1) % polygonA.vertices.size()));

			// Get normal of plane, get support from inverse of normal's direction, and
			// compare distances.
			PVector planeNormal = new PVector(PVector.sub(vertex2, vertex1).y, -PVector.sub(vertex2, vertex1).x)
					.mult(reverseFactor).normalize();

			List<PVector> supports = new ArrayList<>(
					polygonB.getSupportPoint(new PVector(-planeNormal.x, -planeNormal.y)).stream()
							.map((v) -> PVector.add(polygonB.getObject().getPosition(), v)).toList());
			float dist = PVector.dot(PVector.sub(supports.get(0), vertex1), planeNormal);

			// Assume 1st vertex negative - reverse normals if not.
			if (i == 0 && dist > 0.0f) {
				reverseFactor = -1.0f;
				i -= 1;
			} else if (dist > ret.penetration) {
				p1 = vertex1;
				p2 = vertex2;
				ret.penetration = dist;
				ret.affectPoints = supports;
				ret.normal = planeNormal;

				if (ret.penetration > 0.0f) {
					return null;
				}
			}
		}
		ret.affectPoints = cleanFromVoronoiRegions(ret.affectPoints, p1, p2, objB.getPosition());
		return ret;
	}


	/**
	 * Query face distances with a particle as second object.
	 * 
	 * @param obj The object that owns the CollisionMesh components.
	 * @param polygon The polygon to check for collisions against the particle.
	 * @param particle The particle object to check for collisions against.
	 * 
	 * @return The collision details between the particle and the object, if any exist.
	 */
	public static CollisionDetails particleQueryFaceDist(PhysicalObject obj, CollisionMesh polygon, Particle particle) {
		if (polygon.meshType == MeshType.CIRCLE) {
			PVector dir = PVector.sub(polygon.getPosition(), particle.getPosition());
			if (dir.mag() <= polygon.radius) {
				PVector normal = dir.copy().normalize();
				return new CollisionDetails(-dir.mag(), obj, particle, polygon, null, normal, Arrays.asList(particle.getPosition()));
			}
		}

		CollisionDetails ret = new CollisionDetails(-Float.MAX_VALUE, obj, particle, polygon, null, null, Arrays.asList(particle.getPosition()));
		float reverseFactor = 1.0f;
		for (int i = 0; i < polygon.vertices.size(); i++) {
			PVector vertex1 = PVector.add(polygon.getPosition(), polygon.vertices.get(i));
			PVector vertex2 = PVector.add(polygon.getPosition(), polygon.vertices.get((i + 1) % polygon.vertices.size()));

			PVector planeNormal = new PVector(PVector.sub(vertex2, vertex1).y, -PVector.sub(vertex2, vertex1).x)
					.mult(reverseFactor).normalize();
			float dist = PVector.dot(PVector.sub(particle.getPosition(), vertex1), planeNormal);

			if (i == 0 && dist > 0.0f) {
				reverseFactor = -1.0f;
				i -= 1;
			} else if (dist > ret.penetration) {
				ret.penetration = dist;
				ret.normal = planeNormal;

				if (ret.penetration > 0.0f) {
					return null;
				}
			}
		}
		return ret;
	}

	// Remove points from the list that are not on the edge Voronoi region of the
	// segment.
	private static List<PVector> cleanFromVoronoiRegions(List<PVector> points, PVector p1, PVector p2,
			PVector positionB) {
		PVector tan1 = PVector.sub(p2, p1);
		PVector tan2 = PVector.sub(p1, p2);
		Iterator<PVector> iterator = points.iterator();
		while (iterator.hasNext()) {
			PVector v = iterator.next();
			if (PVector.dot(PVector.sub(v, p1), tan1) < 0 || PVector.dot(PVector.sub(v, p2), tan2) < 0) {
				iterator.remove();
			}
		}
		return points;
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
		return new CollisionDetails(minDist, objA, objB, sphere, mesh, normal, Arrays.asList(affectPoint));
	}

	/**
	 * Get the support point of the mesh over a direction.
	 *
	 * @param direction The direction for which the support point is for.
	 */
	public List<PVector> getSupportPoint(PVector direction) {
		if (meshType == MeshType.POLYGON) {
			List<PVector> maxes = new ArrayList<>(Arrays.asList(vertices.get(0)));
			float maxDot = PVector.dot(maxes.get(0), direction);
			for (PVector v : vertices) {
				float dot = v.dot(direction);
				if (dot > maxDot + PhysicalObject.SAME_EDGE_THRESHOLD) {
					maxes = new ArrayList<>(Arrays.asList(v));
					maxDot = dot;
				} else if (Math.abs(dot - maxDot) < PhysicalObject.SAME_EDGE_THRESHOLD) {
					maxes.add(v);
				}
			}
			return maxes;
		} else {
			return Arrays.asList(PVector.mult(PVector.fromAngle(direction.heading()), radius));
		}
	}

	/**
	 * Set properties of the rigid body from a key-value map.
	 */
	public void setProperties(Map<String, Float> properties) {
		if (properties == null) {
			return;
		}
		for (String property : properties.keySet()) {
			float val = properties.get(property);
			if (property.equals("dynamicFriction")) {
				dynamicFriction = val;
			} else if (property.equals("staticFriction")) {
				staticFriction = val;
			} else if (property.equals("bounciness")) {
				bounciness = val;
			} else if (property.equals("calc_treshold")) {
				ROTATION_CALC_THRESHOLD = val;
			}
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
	public CollisionMesh(PVector anchor, List<PVector> vertices, Map<String, Float> properties) {
		this.savedAnchor = anchor.copy();
		this.anchor = anchor.copy();
		this.meshType = MeshType.POLYGON;
		this.storageVertices = vertices;
		this.vertices = vertices;
		setProperties(properties);
	}

	/**
	 * Constructor for a circle-type CollisionMesh object.
	 *
	 * @param size     The mesh's bounds. Recalculated with object rotation.
	 * @param vertices The mesh's vertices defining the edge of the mesh/polygon.
	 * @param friction How rough the terrain is - i.e. How much force is required
	 *                 for the object to move without inertia.
	 */
	public CollisionMesh(PVector anchor, float radius, Map<String, Float> properties) {
		this.savedAnchor = anchor.copy();
		this.anchor = anchor.copy();
		this.meshType = MeshType.CIRCLE;
		this.radius = radius;
		this.size = new PVector(radius * 2, radius * 2);
		setProperties(properties);
	}
}
