package tankphysics.engine;

import processing.core.PVector;

public class Force {
	private boolean massProportional;
	private boolean fluidResistance;
	private PVector vectorForce;

	public boolean isFluidResistance() {
		return fluidResistance;
	}

	public boolean isMassProportional() {
		return massProportional;
	}

	public void setMassProportional(boolean massProportional) {
		this.massProportional = massProportional;
	}

	public PVector getVectorForce() {
		return vectorForce;
	}

	public void setVectorForce(PVector vectorForce) {
		this.vectorForce = vectorForce;
	}

	public Force(PVector force, boolean fluidResistance, boolean massProportional) {
		this.vectorForce = force;
		this.fluidResistance = fluidResistance;
		this.massProportional = massProportional;
	}

}
