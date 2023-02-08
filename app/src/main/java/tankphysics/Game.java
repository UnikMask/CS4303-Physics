package tankphysics;

import processing.core.PApplet;
import processing.core.PVector;
import tankphysics.engine.*;

public class Game extends PApplet {
	Director engineDirector;
	GameObject camera;
	GameObject bullet;
	RigidBody bulletCPU;

	public void setup() {
		frameRate(60);
		engineDirector = new Director(this);
		camera = engineDirector.getCamera();

		// Set up vertices for blocks

		// Make gravity-bound object.
		bulletCPU = new RigidBody(18.7f, 0.2f);
		CollisionMesh bulletMesh = new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(64, 64)), 0.3f);
		bullet = new GameObject(new PVector(64, 64), new PVector(120, displayHeight - 180), false,
				new Sprite("dirt_block.png", new PVector(32, 32)), bulletCPU, bulletMesh);
		bulletCPU.attachToHitbox(bulletMesh);

		// Make a plane for collision checks.
		GameObject plane = new GameObject(new PVector(1800, 32), new PVector(960, displayHeight - 16), false,
				new VisualPolygon(new PVector(), Polygons.makeSquare(new PVector(1800, 32)), color(255)),
				new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(1800, 32)), 0.3f));

		GameObject hexagon = new GameObject(new PVector(512, 512), new PVector(displayWidth / 2, 3 * displayHeight / 4),
				false,
				new VisualPolygon(new PVector(), Polygons.makeRegularPolygon(new PVector(512, 512), 6), color(255)),
				new CollisionMesh(new PVector(), Polygons.makeRegularPolygon(new PVector(512, 512), 6), 1f));

		// Make a plane for collision checks.
		GameObject wall = new GameObject(new PVector(64, 128), new PVector(displayWidth - 128, displayHeight - 112),
				false, new VisualPolygon(new PVector(), Polygons.makeSquare(new PVector(64, 128)), color(255)),
				new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(64, 128)), 0.3f));

		// Attach all components to director.
		engineDirector.attach(plane, bullet, wall, hexagon);

		// Give initial velocity to bullet.
		bulletCPU.setVelocity(new PVector(2, -10));
		camera.setPosition(bullet.getPosition());
		camera.setSize(PVector.mult(new PVector(displayWidth / 2, displayHeight / 2),
				1 + (bulletCPU.getVelocity().mag() / 40)));
	}

	public void settings() {
		size(1920, 1080, PApplet.P2D);
		fullScreen();
	}

	public void draw() {
		background(0);

		camera.setPosition(new PVector(lerp(camera.getPosition().x, bullet.getPosition().x, 0.02f),
				lerp(camera.getPosition().y, bullet.getPosition().y, 0.02f)));
		PVector newSize = PVector.mult(new PVector(displayWidth / 2, displayHeight / 2),
				1 + (bulletCPU.getVelocity().mag() / 40));
		camera.setSize(
				new PVector(lerp(camera.getSize().x, newSize.x, 0.05f), lerp(camera.getSize().y, newSize.y, 0.05f)));
		engineDirector.nextFrame();
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "tankphysics.Game" });
	}
}
