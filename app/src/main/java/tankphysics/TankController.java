package tankphysics;

import processing.core.PVector;

public interface TankController {
	public void keyHeld(char key);

	public void onClick(PVector mousePosition);

	public void update(PVector mousePosition, Tank enemyTank);

	public static void shootProjectile(Tank tank, Game game) {
		Bullet bullet = tank.spawnProjectile();
		if (bullet != null) {
			game.engineDirector.attach(bullet);
			bullet.attachEventListener("onHit", game.getBulletOnHitListener(bullet));
			game.currentBullet = bullet;
			game.engineDirector.attachEventListener("update", game.getBulletUpdateListener(bullet));
			game.engineDirector.removeCollisions(bullet.getRigidBody(), tank.getRigidBody());
		}
	}

}
