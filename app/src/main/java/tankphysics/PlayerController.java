package tankphysics;

import processing.core.PVector;
import tankphysics.Tank.TankState;

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
		tank.getNozzle().getMarker().hide();
		TankController.shootProjectile(tank, game);
	}

	public void update(PVector mouseDist, Tank enemyTank) {
		if (tank.getState() == TankState.MOVING && tank.getNozzle().getMarker().isHidden()) {
			tank.getNozzle().getMarker().show();
		}
		tank.setAimOptions(mouseDist);
	}

	public PlayerController(Tank tank, Game game) {
		this.tank = tank;
		this.game = game;
	}
}
