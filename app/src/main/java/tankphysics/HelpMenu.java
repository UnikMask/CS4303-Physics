package tankphysics;

import processing.core.PApplet;
import processing.core.PVector;

public class HelpMenu {
	private UIButton backButton;

	public void draw(PApplet sketch, PVector scaledMousePosition) {
		// Draw help sign
		sketch.pushStyle();
		sketch.textSize((float) sketch.height / 100 * 15);
		sketch.textAlign(PApplet.CENTER, PApplet.CENTER);
		sketch.text("Help", sketch.width / 2, 1 * sketch.height / 16);
		sketch.popStyle();

		// Draw instructions
		sketch.pushStyle();
		sketch.textAlign(PApplet.LEFT, PApplet.CENTER);
		float pageStart = 1.75f * sketch.height / 16;
		float lineSize = 1.5f * sketch.height / 16;
		sketch.textSize((float) sketch.height / 16);
		sketch.text("- Control the tank's power and angle with the mouse.", sketch.width / 16, pageStart + lineSize);
		sketch.text("- Move the tank using A and D.", sketch.width / 16, pageStart + 2 * lineSize);
		sketch.text("- Rotate the tank using Q and E.", sketch.width / 16, pageStart + 3 * lineSize);
		sketch.text("- Shoot a projectile by clicking on the mouse.", sketch.width / 16, pageStart + 4 * lineSize);
		sketch.text("- Win by being the last tank on the platform.", sketch.width / 16, pageStart + 5 * lineSize);
		sketch.text("- Pause and unpause the game by pressing P.", sketch.width / 16, pageStart + 6 * lineSize);
		sketch.popStyle();

		// Draw back button
		backButton.draw(sketch, scaledMousePosition);

	}

	public void handleClickEvent(PVector scaledMousePosition) {
		backButton.callOnClickEvent(scaledMousePosition);
	}

	public HelpMenu(Runner runner) {
		backButton = new UIButton(new PVector(0.45f, 0.85f), new PVector(0.1f, 0.1f), "Back",
				runner.getBackButtonListener());
	}
}
