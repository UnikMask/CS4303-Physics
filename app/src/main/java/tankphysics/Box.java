package tankphysics;

import java.util.Map;

import processing.core.PApplet;
import processing.core.PVector;
import tankphysics.engine.CollisionMesh;
import tankphysics.engine.Director;
import tankphysics.engine.EventListener;
import tankphysics.engine.GameObject;
import tankphysics.engine.Polygons;
import tankphysics.engine.RigidBody;
import tankphysics.engine.VisualPolygon;

public class Box extends GameObject {
	private static final Map<String, Float> boxSurface = Map.ofEntries(Map.entry("bounciness", 0.3f),
			Map.entry("staticFriction", 1.2f), Map.entry("dynamicFriction", 1f));
	private RigidBody rigidBody = new RigidBody(BOX_MASS,
			new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(1.5f, 1.5f)), boxSurface));
	private static final float BOX_MASS = 20;

	public EventListener getBoxOnHitListener(Director engineDirector) {
		GameObject self = this;
		return new EventListener() {
			public void call(GameObject caller, Object... parameters) {
				System.out.println("Hit!");
				if (caller instanceof Bullet) {
					engineDirector.disattach(caller);
					engineDirector.disattach(self);
				}
			}
		};
	}

	RigidBody getRigidBody() {
		return rigidBody;
	}

	public Box(PVector position, PApplet sketch, Director engineDirector) {
		super(new PVector(1.5f, 1.5f), position, false,
				new VisualPolygon(new PVector(), Polygons.makeSquare(new PVector(1.5f, 1.5f)),
						sketch.loadImage("box_texture.jpg"),
						Polygons.getPolygonUVMapping(Polygons.makeSquare(new PVector(1.5f, 1.5f)),
								new PVector(-0.75f, -0.75f), new PVector(0.75f, 0.75f), new PVector(480, 480))));
		attachEventListener("onHit", getBoxOnHitListener(engineDirector));
		attach(rigidBody);
	}
}
