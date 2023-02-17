package tankphysics;

import processing.core.PVector;

public class PlayerController implements TankController {
	Game game;
	Tank tank;

	public void keyHeld(char key) {
		if (key == 'd') {
			tank.drive(true);
		} else if (key == 'a') {
			tank.drive(false);
		} else if (key == 'q') {
			tank.getRigidBody().setRotationalVelocity(tank.getRigidBody().getRotationalVelocity() - 0.1f);
		} else if (key == 'e') {
			tank.getRigidBody().setRotationalVelocity(tank.getRigidBody().getRotationalVelocity() + 0.1f);
		}
	}

	public void onClick(PVector mousePosition) {
		TankController.shootProjectile(tank, game);
	}

	public void update(PVector mouseDist, Tank enemyTank) {
		tank.setAimOptions(mouseDist);
	}

	public PlayerController(Tank tank, Game game) {
		this.tank = tank;
		this.game = game;
	}
}
