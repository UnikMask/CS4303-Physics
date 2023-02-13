package tankphysics;

import tankphysics.engine.CollisionMesh;
import tankphysics.engine.RigidBody;
import tankphysics.engine.GameObject;
import tankphysics.engine.Polygons;
import tankphysics.engine.VisualPolygon;

import java.util.List;
import java.util.Arrays;
import processing.core.PVector;

public class Tank extends GameObject {
	private static final List<PVector> belly = Arrays.asList(new PVector(-0.75f, -0.5f), new PVector(0.75f, -0.5f),
			new PVector(0.5f, 0.5f), new PVector(-0.5f, 0.5f));
	private static final List<PVector> hull = Polygons.makeRegularPolygon(new PVector(1, 1), 16, 0,
			new PVector(0, -0.625f));
	private static final float TANK_MASS = 80;
	private RigidBody tankBody;

	public RigidBody getRigidBody() {
		return tankBody;
	}

	public Tank(PVector position, int color) {
		super(new PVector(1.5f, 2.125f), position, false, new VisualPolygon(new PVector(), belly, color),
				new VisualPolygon(new PVector(), hull, color));
		CollisionMesh bodyMesh = new CollisionMesh(new PVector(), belly, null);
		CollisionMesh hullMesh = new CollisionMesh(new PVector(), hull, null);
		tankBody = new RigidBody(TANK_MASS);
		tankBody.attachToHitbox(bodyMesh, hullMesh);
		this.attach(tankBody);
	}
}
