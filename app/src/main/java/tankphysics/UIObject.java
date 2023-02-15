package tankphysics;

import java.util.List;

import processing.core.PApplet;
import processing.core.PVector;

public interface UIObject {
	public void draw(PApplet sketch);

	public void setPosition(PVector position);

	public PVector getPosition();

	public List<UIObject> getContainingObjects();
}
