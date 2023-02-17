package tankphysics;

import java.util.HashSet;
import java.util.List;

import javax.swing.event.EventListenerList;

import processing.core.PVector;
import tankphysics.Tank.TankState;
import tankphysics.engine.EngineEventListener;
import tankphysics.engine.GameObject;
import tankphysics.engine.PhysicalObject;

public class ComputerController implements TankController {
	// Constants
	private static final float MAX_DIST_TO_FLOOR = 19;
	private static final float CONTROL_VELOCITY = 5;
	private static final float EPSILON_VELOCITY = 0.5f;
	private static final float CONTROL_ROT_VEL = (float) Math.PI / 16;
	private static final float EPSILON_ROTATION = 0.1f;
	private static final float NUM_NEIGHBOURS = 8;
	private static final float INTENSITY_STEP = 5;
	private static final float ANGLE_STEP = (float) Math.PI / 200;
	private static final int TIMEOUT_NUM_STEPS = 10;

	private Game game;
	private Tank tank;

	public void keyHeld(char key) {
		return;
	}

	public void onClick(PVector mousePosition) {
		return;
	}

	public void update(PVector mousePosition, Tank enemyTank) {
		if (tank.getState() != TankState.MOVING) {
			return;
		}

		// Decision tree - check distance to floor
		float distToFloor = tank.getPosition().x - game.floor.getPosition().x;
		if (distToFloor > MAX_DIST_TO_FLOOR) {
			tank.drive(true);
			return;
		} else if (distToFloor < -MAX_DIST_TO_FLOOR) {
			tank.drive(false);
			return;
		}

		// Decision tree - check velocity control
		float velocity = tank.getRigidBody().getVelocity().x;
		if (velocity > CONTROL_VELOCITY) {
			tank.drive(true);
			return;
		} else if (velocity < -CONTROL_VELOCITY) {
			tank.drive(false);
			return;
		}

		// Decision tree - check rotational velocity control
		if (tank.getRigidBody().getRotationalVelocity() > CONTROL_ROT_VEL) {
			tank.getRigidBody().setRotationalVelocity(tank.getRigidBody().getRotationalVelocity() - 0.1f);
			return;
		} else if (tank.getRigidBody().getRotationalVelocity() < -CONTROL_ROT_VEL) {
			tank.getRigidBody().setRotationalVelocity(tank.getRigidBody().getRotationalVelocity() + 0.1f);
			return;
		}

		// Decision tree - check rotational velocity control
		if (tank.getRigidBody().getOrientation() > EPSILON_ROTATION) {
			tank.getRigidBody().setRotationalVelocity(tank.getRigidBody().getRotationalVelocity() - 0.1f);
			return;
		} else if (tank.getRigidBody().getOrientation() < -EPSILON_ROTATION) {
			tank.getRigidBody().setRotationalVelocity(tank.getRigidBody().getRotationalVelocity() + 0.1f);
			return;
		}

		// Decision tree - set a stable velocity
		if (velocity > EPSILON_VELOCITY || velocity < -EPSILON_VELOCITY) {
			return;
		}

		// Take a shot
		takeShot();
	}

	// Tank random velocity and angle, then perform climbing hill algorithm until a
	// point at distance
	// from tank is found.
	public void takeShot() {
		float intensity = (float) Math.random() * 100;
		float angle = (float) Math.random() * (float) Math.PI / 2;
		float dist = simulateShotAndGetDistanceFromEnemyTank(tank, intensity, angle);
		for (int step = 0; step < TIMEOUT_NUM_STEPS && dist != 0.0f; step++) {
			float minIntensity = intensity;
			float minAngle = angle;
			float minDist = dist;
			for (int i = 0; i < 8; i++) {
				float newIntensity = intensity + (float) Math.cos(i * Math.PI / NUM_NEIGHBOURS) * INTENSITY_STEP;
				float newAngle = intensity + (float) Math.cos(i * Math.PI / NUM_NEIGHBOURS) * ANGLE_STEP;

				float newDist = simulateShotAndGetDistanceFromEnemyTank(tank, newIntensity, newAngle);
				if (newDist < minDist) {
					minIntensity = newIntensity;
					minAngle = newAngle;
					minDist = newDist;
				}
			}
			if (dist != minDist) {
				intensity = minIntensity;
				angle = minAngle;
				dist = minDist;
			}
		}

		// Take aim and shoot!
		System.out.println("Taking shot - intensity: " + intensity + ", angle: " + angle);
		tank.setAimOptions(intensity, angle);
		TankController.shootProjectile(tank, game);
	}

	public float simulateShotAndGetDistanceFromEnemyTank(Tank enemyTank, float intensity, float angle) {
		tank.setAimOptions(intensity, angle);
		Bullet bullet = tank.spawnProjectileStateless();
		bullet.attachEventListener("onHit", new EngineEventListener() {
			public void call(GameObject caller, Object... parameters) {
				game.engineDirector.disattach(bullet);
			}
		});
		bullet.attachEventListener("update", game.getBulletUpdateListener(bullet));

		HashSet<PhysicalObject> hits = game.engineDirector.startSimulationAndReturnInteractions(bullet,
				bullet.getRigidBody());
		if (hits.contains(enemyTank.getRigidBody())) {
			return 0.0f;
		} else {
			return Math.abs(bullet.getPosition().x - enemyTank.getPosition().x);
		}
	}

	public ComputerController(Tank tank, Game game) {
		this.game = game;
		this.tank = tank;
	}
}
