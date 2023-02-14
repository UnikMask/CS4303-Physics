package tankphysics;

import tankphysics.engine.CollisionMesh;
import tankphysics.engine.RigidBody;
import tankphysics.engine.GameObject;
import tankphysics.engine.Polygons;
import tankphysics.engine.VisualPolygon;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import processing.core.PVector;

/**
 * Class representing a tank in the tankphysics game.
 */
public class Tank extends GameObject {
	// Tank constants
	private static final List<PVector> belly = Arrays.asList(new PVector(-0.75f, -0.5f), new PVector(0.75f, -0.5f),
			new PVector(0.5f, 0.5f), new PVector(-0.5f, 0.5f));
	private static final List<PVector> hull = Polygons.makeRegularPolygon(new PVector(1, 1), 16, 0,
			new PVector(0, -0.625f));
	private static final float TANK_MASS = 800;
	private static final float MAX_STRENGTH = 100;

	// GameObject bomponents
	private CollisionMesh bellyMesh = new CollisionMesh(new PVector(), belly, null);
	private RigidBody tankBody = new RigidBody(TANK_MASS, bellyMesh, new CollisionMesh(new PVector(), hull, null));
	private Nozzle nozzle;
	private float intensity;

	// Class that represents the nozzle of the tank.
	class Nozzle extends GameObject {
		private static final float MIN_BAD_ANGLE = 0.19f;
		private static final float MAX_BAD_ANGLE = (float) Math.PI - 0.19f;
		private static final List<PVector> nozzle = Polygons.makeSquare(new PVector(1.1f, 0.2f),
				new PVector(0.1f, 0.1f));
		private float extraAngle = 0;

		@Override
		public float getRotation() {
			System.out.println(extraAngle);
			return super.rotation + extraAngle;
		}

		public void setExtraAngle(float angle) {
			if (angle < MIN_BAD_ANGLE || angle > MAX_BAD_ANGLE) {
				this.extraAngle = angle;
			} else {
				this.extraAngle = angle > (MIN_BAD_ANGLE + MAX_BAD_ANGLE) / 2 ? MAX_BAD_ANGLE : MIN_BAD_ANGLE;
			}
		}

		Nozzle(PVector position, int color) {
			super(new PVector(1.5f, 0.3f), position, true, new VisualPolygon(new PVector(), nozzle, color));
		}
	}

	// Tank states
	enum TankState {
		MOVING, SHOOTING, IDLE
	}

	private TankState state = TankState.IDLE;

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public RigidBody getRigidBody() {
		return tankBody;
	}

	public Nozzle getNozzle() {
		return nozzle;
	}

	public TankState getState() {
		return state;
	}

	public void setState(TankState state) {
		this.state = state;
	}

	////////////////////
	// Tank Actions //
	////////////////////

	public void drive(boolean left) {
		if (state == TankState.MOVING) {
			tankBody.setVelocity(PVector.add(tankBody.getVelocity(),
					Polygons.getRotatedVector(new PVector(left ? 0.1f : -0.1f, 0), rotation)));
		}
	}

	public void setMovingFriction() {
		bellyMesh.setProperties(Map.ofEntries(Map.entry("dynamicFriction", 0f)));
	}

	public void setIdleFriction() {
		bellyMesh.setProperties(Map.ofEntries(Map.entry("dynamicFriction", 1f)));
	}

	public void setAimOptions(PVector mouseDist) {
		if (state == TankState.MOVING) {
			nozzle.setExtraAngle(mouseDist.heading());
			intensity = Math.min(mouseDist.mag() / 5, 100);
		}
	}

	public Bullet spawnProjectile() {
		if (state == TankState.MOVING) {
			setState(TankState.SHOOTING);
			return new Bullet(PVector.add(nozzle.getPosition(), PVector.fromAngle(nozzle.extraAngle).mult(1)),
					nozzle.extraAngle, Math.min(intensity, MAX_STRENGTH));
		}
		return null;
	}

	//////////////////
	// Constructors //
	//////////////////

	public Tank(PVector position, int color) {
		super(new PVector(1.5f, 2.125f), position, false, new VisualPolygon(new PVector(), belly, color),
				new VisualPolygon(new PVector(), hull, color));
		this.attach(tankBody);
		nozzle = new Nozzle(position, color);
		this.addChild(nozzle, new PVector(0, -0.750f));
	}
}
