package tankphysics;

import processing.core.PApplet;
import processing.core.PVector;
import tankphysics.engine.CollisionMesh;
import tankphysics.engine.Director;
import tankphysics.engine.GameObject;
import tankphysics.engine.Polygons;
import tankphysics.engine.RigidBody;
import tankphysics.engine.Surfaces;
import tankphysics.engine.VisualPolygon;

public class TestEnvironment extends PApplet {
	GameObject particle;
	GameObject camera;
	Director testDirector;

	public void setup() {
		testDirector = new Director(this);
		camera = testDirector.getCamera();

		GameObject floor = new GameObject(new PVector(30, 2), new PVector(0, 0), false,
				new VisualPolygon(new PVector(), Polygons.makeSquare(new PVector(30, 2)), color(128)),
				new CollisionMesh(new PVector(), Polygons.makeSquare(new PVector(30, 2)), Surfaces.getRoughSurface()));

		particle = new GameObject(new PVector(1, 1), new PVector(0, -8), false,
				new VisualPolygon(new PVector(0.5f, 0.5f), Polygons.makeRegularPolygon(new PVector(1, 1), 26), 255),
				new RigidBody(3, new CollisionMesh(null)));

		testDirector.attach(floor, particle);
	}

	public void settings() {
		size(854, 480, PApplet.P2D);
	}

	public void draw() {
		background(0);
		testDirector.nextFrame();
	}
}
