package tankphysics;

import java.util.List;

import processing.core.PVector;
import tankphysics.engine.CollisionMesh;
import tankphysics.engine.GameObject;
import tankphysics.engine.Polygons;
import tankphysics.engine.RigidBody;
import tankphysics.engine.VisualPolygon;

public class Bullet extends GameObject {
	private static final float MAX_VELOCITY = 600;
	private static final float BULLET_MASS = 20;
	private static final float BULLET_DIAMETER = 0.5f;

	private static List<PVector> bulletPolygon = Polygons
			.makeRegularPolygon(new PVector(BULLET_DIAMETER, BULLET_DIAMETER), 26);
	private RigidBody bulletBody = new RigidBody(BULLET_MASS, new CollisionMesh(new PVector(), bulletPolygon, null));
	private PVector startPosition;

	public RigidBody getRigidBody() {
		return bulletBody;
	}

	public PVector getStartPosition() {
		return startPosition;
	}

	enum BulletType {
		DEFAULT, EXPLOSIVE, PENETRATING
	}

	public Bullet(PVector position, float angle, float intensity) {
		super(new PVector(BULLET_DIAMETER, BULLET_DIAMETER), position, false,
				new VisualPolygon(new PVector(), bulletPolygon, 255));
		attach(bulletBody);
		startPosition = position.copy();
		bulletBody.applyImpulse(PVector.mult(PVector.fromAngle(angle), (intensity / 100f) * MAX_VELOCITY), position,
				null, false);
	}
}
