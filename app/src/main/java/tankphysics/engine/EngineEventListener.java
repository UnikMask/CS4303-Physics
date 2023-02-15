package tankphysics.engine;

/**
 * Interface for event listeners - interfaces that can be used to call methods
 * from other game objects during events involving remote GameObjects or on
 * Director events.
 */
public interface EngineEventListener {
	/**
	 * Call the event listener.
	 *
	 * @param caller     The GameObject that called the event, or null if none or
	 *                   Director.
	 * @param parameters Set of parameters that the call takes. Because the
	 *                   parameters depend on the kind of event, handling of those
	 *                   is left at caution of the user.
	 */
	public void call(GameObject caller, Object... parameters);
}
