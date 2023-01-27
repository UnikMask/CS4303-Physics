package tankphysics.engine;

import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PVector;

public class Polygon extends VisualModel {
	ArrayList<PVector> vertices;
	ArrayList<ThreeTuple> triangles;
	ArrayList<Integer> colours;

	/**
	 * Tuple of 3 integers.
	 */
	public static class ThreeTuple {
		public Integer x;
		public Integer y;
		public Integer z;

		public ThreeTuple(Integer x, Integer y, Integer z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	public void draw(GameObject camera) {
		PVector visual_pos = position.copy();
		visual_pos.sub(camera.getPosition());

		// Get the positions of the vertices that will be displayed visually.
		ArrayList<PVector> visual_vertices = new ArrayList<>(vertices.size());
		for (PVector vertex : vertices) {
			PVector visual_vertex = new PVector(vertex.x * object.getSize().x + object.getPosition().x,
					vertex.y * object.getSize().y + object.getPosition().y);
			visual_vertex.x *= ((float) displayWidth) / camera.getSize().x;
			visual_vertex.y *= ((float) displayHeight) / camera.getSize().y;
		}

		for (int i = 0; i < triangles.size(); i++) {
			fill(colours.get(i));
			triangle(visual_vertices.get(triangles.get(i).x).x, visual_vertices.get(triangles.get(i).x).y,
					visual_vertices.get(triangles.get(i).y).x, visual_vertices.get(triangles.get(i).y).y,
					visual_vertices.get(triangles.get(i).z).x, visual_vertices.get(triangles.get(i).z).y);
		}

	}

	/**
	 * Base constructor for a polygon.
	 */
	public Polygon(ArrayList<PVector> vertices, ArrayList<ThreeTuple> triangles, ArrayList<Integer> colours) {
		this.vertices = vertices;
		this.triangles = triangles;
		this.colours = colours;
	}

	/**
	 * Make a triangle polygon from 3 points and a colour.
	 *
	 * @param pt0    the first point's position.
	 * @param pt1    the second point's position.
	 * @param pt2    the third point's position.
	 * @param colour the quadrilateral's colour.
	 */
	public static Polygon makeTriangle(PVector pt0, PVector pt1, PVector pt2, Integer colour) {
		ArrayList<PVector> vertices = new ArrayList<>(Arrays.asList(pt0, pt1, pt2));
		ArrayList<ThreeTuple> triangles = new ArrayList<>(Arrays.asList(new ThreeTuple(0, 1, 2)));
		ArrayList<Integer> colours = new ArrayList<>(Arrays.asList(colour));
		return new Polygon(vertices, triangles, colours);
	}

	/**
	 * Make a quadrilateral polygon from 4 points and a colour.
	 *
	 * @param pt0    the first point's position.
	 * @param pt1    the second point's position.
	 * @param pt2    the third point's position.
	 * @param pt3    the fourth point's position.
	 * @param colour the quadrilateral's colour.
	 */
	public static Polygon makeQuadrilateral(PVector pt0, PVector pt1, PVector pt2, PVector pt3, Integer colour) {
		ArrayList<PVector> vertices = new ArrayList<>(Arrays.asList(pt0, pt1, pt2, pt3));
		ArrayList<ThreeTuple> triangles = new ArrayList<>(
				Arrays.asList(new ThreeTuple(0, 1, 2), new ThreeTuple(0, 2, 3)));
		ArrayList<Integer> colours = new ArrayList<>(Arrays.asList(colour, colour));
		return new Polygon(vertices, triangles, colours);
	}
}
