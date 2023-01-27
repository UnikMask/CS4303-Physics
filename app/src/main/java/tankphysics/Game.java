package tankphysics;

import processing.core.PApplet;

public class Game extends PApplet {
	public void settings() {
		size(1920, 1080);
		fullScreen();
	}

	public void draw() {
		background(0);
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "tankphysics.Game" });
	}
}
