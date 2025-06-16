package proto.mechanicalarms.client.renderer.entities;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ModelSegment {
    ModelSegment parent;
    List<ModelSegment> children = new ArrayList<>();
    Quaternionf currentRotation = new Quaternionf();
    Quaternionf prevRotation = new Quaternionf();

    public Vector3f getBaseVector() {
        return baseVector;
    }

    Vector3f baseVector = new Vector3f();
    Vector3f tipVector = new Vector3f();
    float length = 1f;


    public ModelSegment() {
    }

    public ModelSegment(ModelSegment parent) {
        this.parent = parent;
        parent.children.add(this);
    }

    public ModelSegment(ModelSegment parent, int segmentCount) {
        this.parent = parent;
        parent.children.add(this);
        ModelSegment child = new ModelSegment(this);
        for (int i = 0; i < segmentCount; i++) {
            child = new ModelSegment(child);
        }
    }

    public void move(int x, int y, int z) {
        tipVector.x += x;
        tipVector.y += y;
        tipVector.z += z;
    }

    public Quaternionf getCurrentRotation() {
        if (baseVector.isFinite() && tipVector.isFinite()) {
            currentRotation.rotationTo(baseVector, tipVector);
            if (currentRotation.isFinite()) {
                return currentRotation;
            } else {
                return currentRotation.identity();
            }
        } else {
            return currentRotation.identity();
        }
    }
}
