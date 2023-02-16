package tankphysics;

import processing.core.PApplet;
import processing.core.PVector;
import tankphysics.engine.EngineEventListener;
import tankphysics.engine.GameObject;

public class Runner extends PApplet {
	public RunnerState state = RunnerState.MAIN;
	public Game gameSystem;
	public MainMenu menu;

	enum RunnerState {
		MAIN, HELP, PLAY, GAME, GAME_PAUSE
	}

	////////////////////
	// Input Handling //
	////////////////////

	public void mouseClicked() {
		PVector realMousePosition = new PVector(mouseX, mouseY);
		PVector scaledMousePosition = new PVector(realMousePosition.x / width, realMousePosition.y / height);

		if (state == RunnerState.MAIN) {
			menu.handleClickEvent(scaledMousePosition);
		}
	}

	public void setState(RunnerState state) {
		this.state = state;
	}

	///////////////////////////////////
	// Event Listeners / Transitions //
	///////////////////////////////////

	public EngineEventListener getPlayButtonListener() {
		return new EngineEventListener() {
			public void call(GameObject caller, Object... parameters) {
				setState(RunnerState.PLAY);
			}
		};
	}

	public EngineEventListener getHelpButtonListener() {
		return new EngineEventListener() {
			public void call(GameObject caller, Object... parameters) {
				setState(RunnerState.HELP);
			}
		};
	}

	public EngineEventListener getQuitButtonListener() {
		return new EngineEventListener() {
			public void call(GameObject caller, Object... parameters) {
				exit();
			}
		};
	}

	public EngineEventListener getBackButtonListener() {
		return new EngineEventListener() {
			public void call(GameObject caller, Object... parameters) {
				setState(RunnerState.MAIN);
			}
		};
	}

	//////////////////////////////////
	// PApplet Main Handler Methods //
	//////////////////////////////////

	public void setup() {
		frameRate(60);
		menu = new MainMenu(this);
	}

	public void settings() {
		size(1920, 1080, PApplet.P2D);
		fullScreen();
	}

	public void draw() {
		background(0);

		switch (state) {
		case MAIN:
			menu.draw(this, new PVector(mouseX, mouseY));
			break;
		case HELP:
			break;
		case PLAY:
			break;
		case GAME:
		case GAME_PAUSE:
			break;
		}
	}

	public static void main() {
		PApplet.main(new String[] { "tankphysics.Runner" });
	}
}
