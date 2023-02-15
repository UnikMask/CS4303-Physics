package tankphysics.engine;

import java.util.HashMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/* Collection of surface properties for collision meshes */
public class Surfaces {
	public static HashMap<String, Float> getRoughSurface() {
		return Maps.newHashMap(ImmutableMap.of("staticFriction", 0.6f, "dynamicFriction", 0.5f, "bounciness", 0.3f));
	}

	public static HashMap<String, Float> getWoodenSurface() {
		return Maps.newHashMap(ImmutableMap.of("bounciness", 0.1f, "staticFricion", 1.2f, "dynamicFriction", 1f));
	}

	public static HashMap<String, Float> getBoundarySurface() {
		return Maps.newHashMap(ImmutableMap.of("staticFriction", 0f, "dynamicFriction", 0f, "bounciness", 0.3f));
	}
}
