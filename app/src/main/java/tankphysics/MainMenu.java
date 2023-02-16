package tankphysics;

import processing.core.PApplet;
import processing.core.PVector;

public class MainMenu {
	private UIButton playButton;
	private UIButton helpButton;
	private UIButton quitButton;

	public void draw(PApplet sketch, PVector realMousePosition) {
		sketch.pushStyle();
		sketch.fill(255);
		sketch.textSize((float) sketch.height / 100 * 30);
		sketch.textAlign(PApplet.CENTER, PApplet.CENTER);
		sketch.text("TankPhysics", sketch.width / 2, 1.5f * sketch.height / 8);
		sketch.popStyle();

		playButton.draw(sketch, realMousePosition);
		helpButton.draw(sketch, realMousePosition);
		quitButton.draw(sketch, realMousePosition);
	}

	public void handleClickEvent(PVector scaledMousePosition) {
		playButton.callOnClickEvent(scaledMousePosition);
		helpButton.callOnClickEvent(scaledMousePosition);
		quitButton.callOnClickEvent(scaledMousePosition);
	}

	public MainMenu(Runner runner) {
		playButton = new UIButton(new PVector(0.25f, 0.4f), new PVector(0.2f, 0.2f), "Play",
				runner.getPlayButtonListener());
		helpButton = new UIButton(new PVector(0.55f, 0.4f), new PVector(0.2f, 0.2f), "Help",
				runner.getHelpButtonListener());
		quitButton = new UIButton(new PVector(0.4f, 0.6f), new PVector(0.2f, 0.2f), "Quit",
				runner.getQuitButtonListener());
	}
}
