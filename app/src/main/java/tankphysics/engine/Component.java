package tankphysics.engine;

public interface Component {
	public void attach(GameObject object);

	public GameObject getObject();
}
