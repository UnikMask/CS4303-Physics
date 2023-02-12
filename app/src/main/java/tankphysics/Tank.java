package tankphysics;

import tankphysics.engine.CollisionMesh;
import tankphysics.engine.RigidBody;
import tankphysics.engine.GameObject;
import tankphysics.engine.Polygons;
import tankphysics.engine.VisualPolygon;

import java.util.List;
import java.util.Arrays;
import processing.core.PVector;
import processing.core.PApplet;

public class Tank extends GameObject {
	private static final List<PVector> belly = Arrays.asList(new PVector(-4, 0), new PVector(4, 0), new PVector(3, 3),
			new PVector(-3, 3));
	private static final List<PVector> hull = Arrays.asList(new PVector(-2.5f, -2.5f), new PVector(2.5f, -2.5f),
			new PVector(2.5f, 0), new PVector(-2.5f, 0));
	private static final float TANK_MASS = 6200;
	private RigidBody tankBody;

	public RigidBody getTankBody() {
		return tankBody;
	}

	public Tank(PVector position, int color) {
		super(new PVector(8, 5.5f), position, false, new VisualPolygon(new PVector(), belly, color),
				new VisualPolygon(new PVector(), hull, color));
		CollisionMesh bodyMesh = new CollisionMesh(new PVector(), belly, null);
		CollisionMesh hullMesh = new CollisionMesh(new PVector(), hull, null);
		tankBody = new RigidBody(TANK_MASS);
		tankBody.attachToHitbox(bodyMesh, hullMesh);
		this.attach(tankBody);
	}
}
