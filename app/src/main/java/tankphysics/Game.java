package tankphysics;

import java.util.Map;

import processing.core.PApplet;
import processing.core.PVector;
import tankphysics.engine.*;

public class Game extends PApplet {
	Director engineDirector;
	GameObject camera;
	GameObject bullet;
	RigidBody bulletCPU;
	GameObject duplicateBullet;
	float angle = 0;

	public void setup() {
		frameRate(144);
		engineDirector = new Director(this);
		camera = engineDirector.getCamera();

		// Set up vertices for blocks

		// Make gravity-bound object.
		bulletCPU = new RigidBody(31.2f);
		CollisionMesh bulletMesh = new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(64, 64)), null);
		bullet = new GameObject(new PVector(64, 64), new PVector(100, displayHeight - 256), false,
				new VisualPolygon(new PVector(), Polygons.makeSquare(new PVector(64, 64)), loadImage("dirt_block.png"),
						Polygons.getPolygonUVMapping(Polygons.makeSquare(new PVector(64, 64)), new PVector(-32, -32),
								new PVector(32, 32), new PVector(128, 128))),
				bulletCPU);
		bulletCPU.attachToHitbox(bulletMesh);

		RigidBody duplicateCPU = new RigidBody(31.1f);
		CollisionMesh dupMesh = new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(64, 64)), null);
		duplicateBullet = new GameObject(new PVector(64, 64), new PVector(displayWidth - 100, displayHeight - 128),
				false, new VisualPolygon(new PVector(), Polygons.makeSquare(new PVector(64, 64)), color(128)),
				duplicateCPU);
		duplicateCPU.attachToHitbox(dupMesh);

		// Make a plane for collision checks.
		GameObject plane = new GameObject(new PVector(1800, 32), new PVector(960, displayHeight - 16), false,
				new VisualPolygon(new PVector(), Polygons.makeSquare(new PVector(1800, 32)), color(255)),
				new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(1800, 32)),
						Map.ofEntries(Map.entry("staticFriction", 0.6f), Map.entry("dynamicFriction", 0.5f),
								Map.entry("bounciness", 0.3f))));

		GameObject hexagon = new GameObject(new PVector(512, 512), new PVector(displayWidth / 2, 3 * displayHeight / 4),
				false,
				new VisualPolygon(new PVector(), Polygons.makeRegularPolygon(new PVector(512, 512), 6),
						loadImage("dirt_block.png"),
						Polygons.getPolygonUVMapping(Polygons.makeRegularPolygon(new PVector(512, 512), 6),
								new PVector(-256, -256), new PVector(256, 256), new PVector(128 * 8, 128 * 8))),
				new CollisionMesh(new PVector(), Polygons.makeRegularPolygon(new PVector(512, 512), 6),
						Map.ofEntries(Map.entry("staticFriction", 0.1f), Map.entry("dynamicFriction", 0.1f),
								Map.entry("bounciness", 0.2f))));

		// Make a plane for collision checks.
		GameObject wall = new GameObject(new PVector(32, 128), new PVector(displayWidth - 76, displayHeight - 96),
				false, new VisualPolygon(new PVector(), Polygons.makeSquare(new PVector(32, 128)), color(255)),
				new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(32, 128)), null));

		// Attach all components to director.
		engineDirector.attach(plane, bullet, wall, hexagon, duplicateBullet);

		// Give initial velocity to bullet.
		bulletCPU.setVelocity(new PVector(9, -10));
		duplicateCPU.setVelocity(new PVector(-8, -10));

		// Set initial camera position and zoom
		camera.setPosition(duplicateCPU.getPosition());
		camera.setSize(PVector.mult(new PVector(displayWidth / 2, displayHeight / 2),
				1 + (bulletCPU.getVelocity().mag() / 25)));
	}

	public void settings() {
		size(1920, 1080, PApplet.P2D);
		fullScreen();
	}

	public void keyReleased() {
		if (key == 'w') {
			engineDirector.togglePause();
		}
		if (key == 's') {
			engineDirector.disattach(duplicateBullet);
		}
	}

	public void draw() {
		background(0);

		camera.setPosition(new PVector(lerp(camera.getPosition().x, bullet.getPosition().x, 0.02f),
				lerp(camera.getPosition().y, bullet.getPosition().y, 0.02f)));
		PVector newSize = PVector.mult(new PVector(displayWidth / 2, displayHeight / 2),
				1 + (bulletCPU.getVelocity().mag() / 25));
		camera.setSize(
				new PVector(lerp(camera.getSize().x, newSize.x, 0.1f), lerp(camera.getSize().y, newSize.y, 0.1f)));
		engineDirector.nextFrame();
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "tankphysics.Game" });
	}
}
