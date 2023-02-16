package tankphysics;

import processing.core.PApplet;
import processing.core.PVector;
import tankphysics.Game.GameState;
import tankphysics.engine.EngineEventListener;
import tankphysics.engine.GameObject;

public class Runner extends PApplet {
	public RunnerState state = RunnerState.MAIN;
	public Game gameSystem;
	public MainMenu menu;
	public HelpMenu help;
	public PauseMenu pause;

	enum RunnerState {
		MAIN, HELP, PLAY, GAME, GAME_PAUSE
	}

	public void setState(RunnerState state) {
		this.state = state;
	}

	////////////////////
	// Input Handling //
	////////////////////

	public void mouseClicked() {
		PVector realMousePosition = new PVector(mouseX, mouseY);
		PVector scaledMousePosition = new PVector(realMousePosition.x / width, realMousePosition.y / height);

		if (state == RunnerState.MAIN) {
			menu.handleClickEvent(scaledMousePosition);
		} else if (state == RunnerState.HELP) {
			help.handleClickEvent(scaledMousePosition);
		} else if (state == RunnerState.GAME) {
			gameSystem.mouseClicked();
		} else if (state == RunnerState.GAME_PAUSE) {
			pause.handleClickEvent(scaledMousePosition);
		}
	}

	public void keyPressed() {
		if (keyCode == ESC) {
			key = 0;
		}
		if (state == RunnerState.GAME) {
			if (key == 'p') {
				setState(RunnerState.GAME_PAUSE);
			} else {
				gameSystem.keyPressed(key);
			}
		} else if (state == RunnerState.GAME_PAUSE) {
			if (key == 'p') {
				setState(RunnerState.GAME);
			}
		}
	}

	public void keyReleased() {
		if (state == RunnerState.GAME) {
			gameSystem.keyReleased(key);
		}
	}

	///////////////////////////////////
	// Event Listeners / Transitions //
	///////////////////////////////////

	public EngineEventListener getPlayButtonListener() {
		return new EngineEventListener() {
			public void call(GameObject caller, Object... parameters) {
				setState(RunnerState.GAME);
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
		help = new HelpMenu(this);
		pause = new PauseMenu(this);
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
			help.draw(this, new PVector(mouseX, mouseY));
			break;
		case PLAY:
			break;
		case GAME:
			if (gameSystem == null) {
				gameSystem = new Game(this);
			} else {
				if (gameSystem.getState() == GameState.PAUSE) {
					gameSystem.setState(GameState.ONGOING);
				}
				gameSystem.draw();
			}
			break;
		case GAME_PAUSE:
			if (gameSystem.getState() == GameState.ONGOING) {
				gameSystem.setState(GameState.PAUSE);
			}
			gameSystem.draw();
			pause.draw(this, new PVector(mouseX, mouseY));
			break;
		}
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "tankphysics.Runner" });
	}
}
