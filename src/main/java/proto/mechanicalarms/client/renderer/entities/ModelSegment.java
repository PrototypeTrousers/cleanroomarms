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
        //RECHECK THIS
        ModelSegment child = new ModelSegment(this);
        child.baseVector.set(0.5f, 0.2f,0);
        child.tipVector.set(1.5f, 0.2f,0);
        for (int i = 0; i < segmentCount; i++) {
            child = new ModelSegment(child);
            child.baseVector.set(1.5f + i, 0.2f,0);
            child.tipVector.set(2.5f + i, 0.2f,0);
        }
    }

    public void move(int x, int y, int z) {
        baseVector.x = x;
        baseVector.y = y;
        baseVector.z = z;
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
