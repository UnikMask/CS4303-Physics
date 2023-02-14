package tankphysics.engine;

import java.util.ArrayList;

import processing.core.PVector;

public class Particle implements PhysicalObject, Component {
    // Component properties
    private GameObject object;

    // Physical Object properties
    private PVector velocity;
    private float inverseMass;
    private final static PVector PARTICLE_SIZE = new PVector(0.01f, 0.01f);

    /////////////////////////
    // Getters and Setters //
    /////////////////////////

    public PVector getVelocity() {
        return velocity;
    }

    public PVector getSize() {
        return PARTICLE_SIZE;
    }

    public void setVelocity(PVector velocity) {
        this.velocity = velocity;
    }

    public void setMass(float mass) {
        this.inverseMass = mass != 0? mass: 0;
    }

    public GameObject getObject() {
        return object;
    }

    ///////////////////////
    // Interface Methods //
    ///////////////////////

    // Component //

    public void attach(GameObject object) {
        this.object = object;
    }

    // PhysicalObject //

    public float getInverseMass() {
        return inverseMass;
    }

    public PVector getPosition() {
        return object.getPosition();
    }

    public float getOrientation() {
        return 0;
    }

    public void setOrientation(float angle) {
    }

    public float getRotationalVelocity() {
        return 0;
    }

    public void setRotationalVelocity(float velocity) {
    }

    public void setPosition(PVector position) {
        object.setPosition(position);
    }

    public PVector getCOM() {
        return object.getPosition();
    }

    public float getInverseInertia() {
        return 0;
    }

    public Iterable<CollisionMesh> getMeshes() {
        return new ArrayList<>(0);
    }

    public void applyImpulse(PVector impulse, PVector affectPt) {
        velocity = PVector.add(velocity, PVector.mult(impulse, inverseMass));
    }

    /////////////////
    // Constructor //
    /////////////////

    public Particle(float mass) {
        this.inverseMass = mass != 0 ? 1 / mass : 0;
    }
}
