package tankphysics;

import processing.core.PVector;

public class ComputerController {
	private static final float MAX_DIST_TO_FLOOR = 19;
	private static final float CONTROL_VELOCITY = 5;
	private static final float EPSILON_VELOCITY = 0.5f;
	private static final float CONTROL_ROT_VEL = (float) Math.PI / 16;
	private static final float EPSILON_ROTATION = 0.1f;

	Game game;
	Tank tank;

	public void keyHeld(char key) {
		return;
	}

	public void onClick(PVector mousePosition) {
		return;
	}

	public void update(PVector mousePosition, Tank enemyTank) {
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

	public void takeShot() {

	}

	public ComputerController(Game game, Tank tank) {
		this.game = game;
		this.tank = tank;
	}
}
