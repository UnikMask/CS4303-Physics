package tankphysics.engine;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

public class Director {
	GameObject camera;
	PApplet sketch;

	ArrayList<GameObject> world;
	ArrayList<VisualModel> visuals;

	/**
	 * Initiate draw on all visuals in the world.
	 */
	public void draw() {
		for (VisualModel v : visuals) {
			v.draw(camera, sketch);
		}
	}

	public GameObject getCamera() {
		return camera;
	}

	public void setCamera(GameObject camera) {
		this.camera = camera;
	}

	/**
	 * Attach a game object to the director, to interact with its world and get
	 * displayed in relation to its camera.
	 *
	 * @param object The object to attach.
	 */
	public void attach(GameObject object) {
		world.add(object);
		for (Component c : object.getComponents()) {
			if (c instanceof VisualModel) {
				visuals.add((VisualModel) c);
			}
		}
	}

	/**
	 * Constructor for a director object.
	 */
	public Director(PApplet sketch) {
		world = new ArrayList<>();
		visuals = new ArrayList<>();
		camera = new GameObject(new PVector(sketch.displayWidth, sketch.displayHeight), new PVector(0, 0));
		this.sketch = sketch;
	}
}
