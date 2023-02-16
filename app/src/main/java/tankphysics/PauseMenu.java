package tankphysics;

import processing.core.PApplet;
import processing.core.PVector;

public class PauseMenu {
	private UIButton quitButton;

	public void draw(PApplet sketch, PVector scaledMousePosition) {
		sketch.pushStyle();
		sketch.fill(32, 32, 32, 128);
		sketch.rect(0, 0, sketch.width, sketch.height);
		sketch.textSize((float) sketch.height / 100 * 30);
		sketch.textAlign(PApplet.CENTER, PApplet.CENTER);
		sketch.fill(255);
		sketch.text("Paused", sketch.width / 2, 0.75f * sketch.height / 4);
		sketch.textSize((float) sketch.height / 100 * 10);
		sketch.text("Press p to resume.", sketch.width / 2, 2 * sketch.height / 4);
		sketch.popStyle();

		quitButton.draw(sketch, scaledMousePosition);

	}

	public void handleClickEvent(PVector scaledMousePosition) {
		quitButton.callOnClickEvent(scaledMousePosition);
	}

	public PauseMenu(Runner runner) {
		quitButton = new UIButton(new PVector(0.45f, 0.85f), new PVector(0.1f, 0.1f), "Quit",
				runner.getBackButtonListener());
	}
}
