package tankphysics;

import java.util.Map;

import processing.core.PApplet;
import processing.core.PVector;
import tankphysics.engine.CollisionMesh;
import tankphysics.engine.GameObject;
import tankphysics.engine.Polygons;
import tankphysics.engine.RigidBody;
import tankphysics.engine.VisualPolygon;

public class Box extends GameObject {
	private static final Map<String, Float> boxSurface = Map.ofEntries(Map.entry("bounciness", 0.3f),
			Map.entry("staticFriction", 0.5f), Map.entry("dynamicFriction", 1f));
	private RigidBody rigidBody = new RigidBody(BOX_MASS,
			new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(1, 1)), boxSurface));
	private static final float BOX_MASS = 10;

	RigidBody getRigidBody() {
		return rigidBody;
	}

	public Box(PVector position, PApplet sketch) {
		super(new PVector(1, 1), position, false,
				new VisualPolygon(new PVector(), Polygons.makeSquare(new PVector(1, 1)),
						sketch.loadImage("box_texture.jpg"),
						Polygons.getPolygonUVMapping(Polygons.makeSquare(new PVector(1, 1)), new PVector(-0.5f, -0.5f),
								new PVector(0.5f, 0.5f), new PVector(2400, 2400))));
		attach(rigidBody);
	}
}
