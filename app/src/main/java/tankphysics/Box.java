package tankphysics;

import java.util.Map;

import processing.core.PApplet;
import processing.core.PVector;
import tankphysics.engine.CollisionMesh;
import tankphysics.engine.Director;
import tankphysics.engine.EngineEventListener;
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
	private static final int DESTRUCTION_FRAME_COUNT = 25;
	private static final PVector DESTRUCTION_FINAL_SIZE = new PVector(4, 4);
	private Integer numDestructionFrames = 0;
	private VisualPolygon boxLooks;

	public EngineEventListener getBoxOnHitListener(Director engineDirector) {
		return new EngineEventListener() {
			public void call(GameObject caller, Object... parameters) {
				if (caller instanceof Bullet) {
					engineDirector.disattachComponent(rigidBody);
					engineDirector.attachEventListener("update", getBoxDestructionListener(engineDirector));
				}
			}
		};
	}

	public EngineEventListener getBoxDestructionListener(Director engineDirector) {
		GameObject self = this;
		return new EngineEventListener() {
			public void call(GameObject caller, Object... parameters) {
				numDestructionFrames++;

				if (numDestructionFrames >= DESTRUCTION_FRAME_COUNT) {
					engineDirector.disattach(self);
				} else {
					boxLooks.setScale(PVector.lerp(boxLooks.getScale(), DESTRUCTION_FINAL_SIZE, 0.05f));
					int tint = (boxLooks.getTint() >>> 24);
					boxLooks.setTint(((int) PApplet.lerp(tint, 0, 0.1f)) << 24 | 0x00FFFFFF);
				}
			}
		};
	}

	RigidBody getRigidBody() {
		return rigidBody;
	}

	public Box(PVector position, PApplet sketch, Director engineDirector) {
		super(new PVector(1.5f, 1.5f), position, false);
		boxLooks = new VisualPolygon(new PVector(), Polygons.makeSquare(new PVector(1.5f, 1.5f)),
				sketch.loadImage("box_texture.jpg"),
				Polygons.getPolygonUVMapping(Polygons.makeSquare(new PVector(1.5f, 1.5f)), new PVector(-0.75f, -0.75f),
						new PVector(0.75f, 0.75f), new PVector(480, 480)));
		attachEventListener("onHit", getBoxOnHitListener(engineDirector));
		attach(rigidBody);
		attach(boxLooks);
	}
}
