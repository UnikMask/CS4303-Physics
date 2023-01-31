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
	private ArrayList<PVector> vertices;
	private float roughness;
	private PVector size;
	private PVector anchor;

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public void attach(GameObject obj) {
		object = obj;
	}

	////////////////////////////
	// Collision Mesh methods //
	////////////////////////////

	public Force applyCollisionAndBounce(RigidBody body) {
		return null;
	}

	public boolean touches(CollisionMesh mesh) {
		return false;
	}

	/////////////////
	// Constructor //
	/////////////////

	/**
	 * Constructor for a CollisionMesh object.
	 *
	 * @param size      The mesh's bounds. Recalculated with object rotation.
	 * @param vertices  The mesh's vertices defining the edge of the mesh/polygon.
	 * @param roughness How rough the terrain is - i.e. How much force is required
	 *                  for the object to move without inertia.
	 */
	public CollisionMesh(PVector size, PVector anchor, ArrayList<PVector> vertices, float roughness) {
		this.size = size;
		this.vertices = vertices;
		this.roughness = roughness;
	}
}
