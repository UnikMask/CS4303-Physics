package tankphysics;

import processing.core.PVector;

public interface TankController {
	public void keyHeld(char key);

	public void onClick(PVector mousePosition);

	public void update(PVector mousePosition, Tank enemyTank);
}
