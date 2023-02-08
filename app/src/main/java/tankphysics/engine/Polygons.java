package tankphysics.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import processing.core.PVector;

/**
 * Helper methods for polygon generation
 */
public class Polygons {

	/////////////////////////////
	// Regular Polygon Methods //
	/////////////////////////////

	public static List<PVector> makeRegularPolygon(PVector size, int sides, float baseAngle) {
		List<PVector> vertices = new ArrayList<>();

		float increment = (float) (2 * Math.PI / sides);
		for (int i = 0; i < sides; i++) {
			float angle = baseAngle + i * increment;
			vertices.add(new PVector((float) (size.x / 2 * Math.cos(angle)), (float) (size.y / 2 * Math.sin(angle))));
		}
		return vertices;
	}

	public static List<PVector> makeRegularPolygon(PVector size, int sides) {
		return makeRegularPolygon(size, sides, (float) Math.PI / 2);
	}

	////////////////////////////
	// Simple Polygon Methods //
	////////////////////////////

	/**
	 * Make a square of the given size, with given point as anchor.
	 *
	 * @param size   The size of the square
	 * @param anchor The anchor - center for which the vertex positions are
	 *               calculated.
	 */
	public static List<PVector> makeSquare(PVector size, PVector anchor) {
		PVector anchoredPlus = PVector.sub(size, anchor);
		PVector anchoredMinus = PVector.sub(anchoredPlus, size);
		return Arrays.asList(anchoredMinus, new PVector(anchoredPlus.x, anchoredMinus.y), anchoredPlus,
				new PVector(anchoredMinus.x, anchoredPlus.y));
	}

	public static List<PVector> makeSquare(PVector size) {
		return makeSquare(size, new PVector(size.x / 2, size.y / 2));
	}

	public static List<PVector> getPolygonUVMapping(List<PVector> polygon, PVector upLeft, PVector downRight,
			PVector textureSize) {
		List<PVector> uvVertices = new ArrayList<>();
		PVector size = PVector.sub(downRight, upLeft);
		for (PVector v : polygon) {
			uvVertices.add(new PVector(textureSize.x * ((v.x + upLeft.x) / size.x),
					textureSize.y * ((v.y + upLeft.y) / size.y)));
		}
		System.out.println(uvVertices);

		return uvVertices;
	}

}
