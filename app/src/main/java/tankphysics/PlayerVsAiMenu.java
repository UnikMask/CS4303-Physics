package tankphysics;

import processing.core.PApplet;
import processing.core.PVector;

public class PlayerVsAiMenu {
	private UIButton vsPlayer;
	private UIButton vsAI;
	private UIButton backButton;

	public void draw(PApplet sketch, PVector realMousePosition) {
		vsPlayer.draw(sketch, realMousePosition);
		vsAI.draw(sketch, realMousePosition);
		backButton.draw(sketch, realMousePosition);
	}

	public void handleOnClickEvent(PVector scaledMousePosition) {
		vsPlayer.callOnClickEvent(scaledMousePosition);
		vsAI.callOnClickEvent(scaledMousePosition);
		backButton.callOnClickEvent(scaledMousePosition);
	}

	public PlayerVsAiMenu(Runner runner) {
		vsPlayer = new UIButton(new PVector(0.25f, 0.4f), new PVector(0.25f, 0.15f), "Versus Player",
				runner.getVsPlayerButtonListener());
		vsAI = new UIButton(new PVector(0.55f, 0.4f), new PVector(0.25f, 0.15f), "Versus AI",
				runner.getVsAIButtonListener());
		backButton = new UIButton(new PVector(0.4f, 0.6f), new PVector(0.2f, 0.15f), "Back",
				runner.getBackButtonListener());
	}
}
