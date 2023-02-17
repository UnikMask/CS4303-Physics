package tankphysics;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import processing.core.PVector;
import tankphysics.engine.CollisionMesh;
import tankphysics.engine.Director;
import tankphysics.engine.EngineEventListener;
import tankphysics.engine.GameObject;
import tankphysics.engine.Polygons;
import tankphysics.engine.RigidBody;
import tankphysics.engine.VisualPolygon;

/**
 * Class representing a tank in the tankphysics game.
 */
public class Tank extends GameObject {
	// Tank constants
	private static final List<PVector> belly = Arrays.asList(new PVector(-0.75f, -0.5f), new PVector(0.75f, -0.5f),
			new PVector(0.5f, 0.5f), new PVector(-0.5f, 0.5f));
	private static final List<PVector> hull = Polygons.makeRegularPolygon(new PVector(1, 1), 16, 0,
			new PVector(0, -0.625f));
	private static final float MAX_TANK_MASS = 50;
	private static final float MAX_STRENGTH = 100;

	// GameObject bomponents
	private CollisionMesh bellyMesh = new CollisionMesh(new PVector(), belly, null);
	private RigidBody tankBody = new RigidBody(MAX_TANK_MASS, bellyMesh, new CollisionMesh(new PVector(), hull, null));
	private Nozzle nozzle;
	private float intensity;
	private float tankPercentage = 0.0f;
	private TankController controller;
	private float hitImpulse = 0; // Keep track of hit impulse on bullet hit so it doesn't count 1+ times.

	// Class that represents the nozzle of the tank.
	class Nozzle extends GameObject {
		private static final float MIN_BAD_ANGLE = 0.19f;
		private static final float MAX_BAD_ANGLE = (float) Math.PI - 0.19f;
		private static final List<PVector> nozzle = Polygons.makeSquare(new PVector(1.1f, 0.2f),
				new PVector(0.1f, 0.1f));
		private float extraAngle = 0;
		private IntensityMarker marker;

		@Override
		public float getRotation() {
			return super.rotation + extraAngle;
		}

		public IntensityMarker getMarker() {
			return marker;
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
			marker = new IntensityMarker(position);
			addChild(marker, new PVector(0, 0));
		}
	}

	// Class representing the intensity marker on top of the nozzle for hit marking.
	class IntensityMarker extends GameObject {
		private static final float MAX_LENGTH = 4.0f;
		private static final float DIST_FROM_NOZZLE = 1.75f;
		private static final float HEIGHT = 0.4f;
		private static List<PVector> end = Polygons.makeRegularPolygon(new PVector(HEIGHT, HEIGHT), 26);
		private static List<PVector> bar = Polygons.makeSquare(new PVector(1.0f, HEIGHT));
		private VisualPolygon start;
		private VisualPolygon middle;
		private VisualPolygon endSide;
		private boolean hidden = true;

		public boolean isHidden() {
			return hidden;
		}

		public void setIntensity(float intensity) {
			float width = MAX_LENGTH * intensity / 100;
			middle.setScale(new PVector(width, 1));
			middle.setAnchor(new PVector(-(DIST_FROM_NOZZLE + width / 2), 0));
			endSide.setAnchor(new PVector(-(DIST_FROM_NOZZLE + width), 0));
		}

		public void hide() {
			start.setColor(0x00FFFFFF);
			middle.setColor(0x00FFFFFF);
			endSide.setColor(0x00FFFFFF);
			hidden = true;
		}

		public void show() {
			start.setColor(0xFFFFFFFF);
			middle.setColor(0xFFFFFFFF);
			endSide.setColor(0xFFFFFFFF);
			hidden = false;
		}

		public IntensityMarker(PVector position) {
			super(new PVector(0, 0), new PVector(), true);

			start = new VisualPolygon(new PVector(-DIST_FROM_NOZZLE, 0), end, 0x00FFFFFF);
			middle = new VisualPolygon(new PVector(-(DIST_FROM_NOZZLE + 0.5f), 0), bar, 0x00FFFFFF);
			endSide = new VisualPolygon(new PVector(-(DIST_FROM_NOZZLE + 1.0f), 0), end, 0x00FFFFFF);
			attach(start);
			attach(middle);
			attach(endSide);
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

	public TankController getController() {
		return controller;
	}

	public void setController(TankController controller) {
		this.controller = controller;
	}

	////////////////////
	// Tank Actions //
	////////////////////

	public void drive(boolean left) {
		if (state == TankState.MOVING) {
			tankBody.setVelocity(PVector.add(tankBody.getVelocity(), new PVector(left ? 0.1f : -0.1f, 0)));
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
			nozzle.getMarker().setIntensity(intensity);
		}
	}

	public void setAimOptions(float intensity, float angle) {
		this.intensity = Math.min(intensity, 100);
		nozzle.setExtraAngle(angle);
		nozzle.getMarker().setIntensity(intensity);
	}

	public Bullet spawnProjectile() {
		if (state == TankState.MOVING) {
			setState(TankState.SHOOTING);
			return new Bullet(PVector.add(nozzle.getPosition(), PVector.fromAngle(nozzle.extraAngle).mult(1)),
					nozzle.extraAngle, Math.min(intensity, MAX_STRENGTH));
		}
		return null;
	}

	public Bullet spawnProjectileStateless(float intensity, float angle) {
		return new Bullet(PVector.add(nozzle.getPosition(), PVector.fromAngle(angle).mult(1)), angle,
				Math.min(intensity, MAX_STRENGTH));
	}

	public void decrementHP(float damage) {
		tankPercentage += damage / 200;
		tankBody.setMultiplier(1 + tankPercentage);
	}

	public float getPercentage() {
		return (tankPercentage) * 100;
	}

	//////////////////////////
	// Tank Event Listeners //
	//////////////////////////

	public EngineEventListener getTankBulletOnHitListener(HealthBar bar) {
		return new EngineEventListener() {
			public void call(GameObject caller, Object... parameters) {
				Object obj = parameters[0];

				if (caller instanceof Bullet) {
					decrementHP(hitImpulse);
					bar.setPercentage(getPercentage());
					hitImpulse = 0;
				} else if (obj instanceof RigidBody) {
					RigidBody body = (RigidBody) obj;
					float hitIntensity = PVector.sub(tankBody.getVelocity(), body.getVelocity()).mag() * body.getMass();
					decrementHP(hitIntensity);
					bar.setPercentage(getPercentage());
				}
			}
		};
	}

	public EngineEventListener getTankImpulseListener() {
		return new EngineEventListener() {
			public void call(GameObject caller, Object... parameters) {
				if (parameters.length != 1 || !(parameters[0] instanceof PVector)) {
					return;
				}
				if (caller instanceof Bullet) {
					if (hitImpulse == 0) {
						hitImpulse = ((PVector) parameters[0]).mag();
						((PVector) parameters[0]).mult(tankPercentage * 10);
					}
				}
			}
		};
	}

	//////////////////
	// Constructors //
	//////////////////

	public Tank(PVector position, int color, HealthBar bar) {
		super(new PVector(1.5f, 2.125f), position, false, new VisualPolygon(new PVector(), belly, color),
				new VisualPolygon(new PVector(), hull, color));
		this.attach(tankBody);
		nozzle = new Nozzle(position, color);
		this.addChild(nozzle, new PVector(0, -0.750f));
		attachEventListener("onHit", getTankBulletOnHitListener(bar));
		attachEventListener("impulse", getTankImpulseListener());
	}
}
