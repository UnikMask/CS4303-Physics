package tankphysics;

import java.util.Arrays;
import java.util.List;

import processing.core.PVector;
import tankphysics.engine.Director;
import tankphysics.engine.GameObject;
import tankphysics.engine.Polygons;
import tankphysics.engine.VisualPolygon;

public class WindIndicator extends GameObject {
	private static final List<VisualPolygon> POLYGONS_LEFT = Arrays.asList(
			new VisualPolygon(new PVector(3, 1), Polygons.makeSquare(new PVector(1.5f, 0.2f)), 0xFFFFFFFF),
			new VisualPolygon(new PVector(3, 1),
					Arrays.asList(new PVector(-0.5f, -0.5f), new PVector(-1.5f, 0), new PVector(-0.5f, 0.5f)),
					0xFFFFFFFF));
	private static final List<VisualPolygon> POLYGONS_RIGHT = Arrays.asList(
			new VisualPolygon(new PVector(3, 1), Polygons.makeSquare(new PVector(1.5f, 0.2f)), 0xFFFFFFFF),
			new VisualPolygon(new PVector(3, 1),
					Arrays.asList(new PVector(0.5f, -0.5f), new PVector(1.5f, 0), new PVector(0.5f, 0.5f)),
					0xFFFFFFFF));
	private static final List<VisualPolygon> POLYGONS_NEUTRAL = Arrays
			.asList(new VisualPolygon(new PVector(3, 1), Polygons.makeSquare(new PVector(3, 0.2f)), 0xFFFFFFFF));

	private WindDirection currentDir = WindDirection.NEUTRAL;
	private List<VisualPolygon> currentList = POLYGONS_NEUTRAL;

	enum WindDirection {
		LEFT, NEUTRAL, RIGHT;
	}

	@Override
	public boolean isOnTop() {
		return true;
	}

	public void setDirection(WindDirection dir, Director engineDirector) {
		if (dir != currentDir) {
			for (VisualPolygon p : currentList) {
				disattach(p);
				engineDirector.disattachComponent(p);
			}
			currentDir = dir;
			setPolygons(dir);
			for (VisualPolygon p : currentList) {
				attach(p);
				engineDirector.attachComponent(p);
			}
		}
	}

	private void setPolygons(WindDirection dir) {
		if (dir == WindDirection.LEFT) {
			currentList = POLYGONS_LEFT;
		} else if (dir == WindDirection.RIGHT) {
			currentList = POLYGONS_RIGHT;
		} else {
			currentList = POLYGONS_NEUTRAL;
		}
	}

	public WindIndicator(PVector position) {
		super(new PVector(3, 1), position);
		for (VisualPolygon p : currentList) {
			attach(p);
		}
	}
}
