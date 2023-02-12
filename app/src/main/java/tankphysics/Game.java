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
		CollisionMesh bulletMesh = new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(1, 1)), null);
		bullet = new GameObject(new PVector(1, 1), new PVector(1.5625f, 12.875f), false,
				new VisualPolygon(new PVector(), Polygons.makeSquare(new PVector(1, 1)), loadImage("dirt_block.png"),
						Polygons.getPolygonUVMapping(Polygons.makeSquare(new PVector(1, 1)), new PVector(-0.5f, -0.5f),
								new PVector(0.5f, 0.5f), new PVector(128, 128))),
				bulletCPU);
		bulletCPU.attachToHitbox(bulletMesh);

		RigidBody duplicateCPU = new RigidBody(331.1f);
		CollisionMesh dupMesh = new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(1, 1)), null);
		duplicateBullet = new GameObject(new PVector(1, 1), new PVector(28.4375f, 12.875f), false,
				new VisualPolygon(new PVector(), Polygons.makeSquare(new PVector(1, 1)), color(128)), duplicateCPU);
		duplicateCPU.attachToHitbox(dupMesh);

		// Make a plane for collision checks.
		GameObject plane = new GameObject(new PVector(28.125f, 0.5f), new PVector(15, 16.625f), false,
				new VisualPolygon(new PVector(), Polygons.makeSquare(new PVector(28.125f, 0.5f)), color(255)),
				new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(28.125f, 0.5f)),
						Map.ofEntries(Map.entry("staticFriction", 0.6f), Map.entry("dynamicFriction", 0.5f),
								Map.entry("bounciness", 0.3f))));

		GameObject hexagon = new GameObject(new PVector(8, 8), new PVector(15, 12.65625f), false,
				new VisualPolygon(new PVector(), Polygons.makeRegularPolygon(new PVector(8, 8), 6),
						loadImage("dirt_block.png"),
						Polygons.getPolygonUVMapping(Polygons.makeRegularPolygon(new PVector(8, 8), 6),
								new PVector(-4, -4), new PVector(4, 4), new PVector(128 * 8, 128 * 8))),
				new CollisionMesh(new PVector(), Polygons.makeRegularPolygon(new PVector(8, 8), 6),
						Map.ofEntries(Map.entry("staticFriction", 0.1f), Map.entry("dynamicFriction", 0.1f),
								Map.entry("bounciness", 0.2f))));

		// Make a plane for collision checks.
		GameObject wall = new GameObject(new PVector(0.5f, 2), new PVector(28.8125f, 15.375f), false,
				new VisualPolygon(new PVector(), Polygons.makeSquare(new PVector(0.5f, 2)), color(255)),
				new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(0.5f, 2)), null));

		// Attach all components to director.
		engineDirector.attach(plane, bullet, wall, hexagon, duplicateBullet);

		// Give initial velocity to bullet.
		bulletCPU.setVelocity(new PVector(10, -10));
		duplicateCPU.setVelocity(new PVector(-8, -10));

		// Set initial camera position and zoom
		camera.setPosition(duplicateCPU.getPosition());
		camera.setSize(PVector.mult(new PVector(30 / 2, 16.875f / 2), 1 + (bulletCPU.getVelocity().mag() / (25 * 64))));
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
		PVector newSize = PVector.mult(new PVector(30 / 2, 16.875f / 2),
				1 + (bulletCPU.getVelocity().mag() / (25 * 64)));
		camera.setSize(
				new PVector(lerp(camera.getSize().x, newSize.x, 0.1f), lerp(camera.getSize().y, newSize.y, 0.1f)));
		engineDirector.nextFrame();
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "tankphysics.Game" });
	}
}
