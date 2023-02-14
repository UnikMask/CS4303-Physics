package tankphysics;

import java.util.List;

import processing.core.PVector;
import tankphysics.engine.CollisionMesh;
import tankphysics.engine.GameObject;
import tankphysics.engine.Polygons;
import tankphysics.engine.RigidBody;
import tankphysics.engine.VisualPolygon;

public class Bullet extends GameObject {
	private static final float MAX_VELOCITY = 100;
	private static final float BULLET_MASS = 5;

	private static List<PVector> bulletPolygon = Polygons.makeRegularPolygon(new PVector(0.2f, 0.2f), 26);
	private RigidBody bulletBody = new RigidBody(BULLET_MASS, new CollisionMesh(new PVector(), bulletPolygon, null));

	public RigidBody getRigidBody() {
		return bulletBody;
	}

	enum BulletType {
		DEFAULT, EXPLOSIVE, PENETRATING
	}

	public Bullet(PVector position, float angle, float intensity) {
		super(new PVector(0.2f, 0.2f), position, false, new VisualPolygon(new PVector(), bulletPolygon, 255));
		attach(bulletBody);
		bulletBody.applyImpulse(PVector.mult(PVector.fromAngle(angle), (intensity / 100f) * MAX_VELOCITY), position,
				false);
	}
}
