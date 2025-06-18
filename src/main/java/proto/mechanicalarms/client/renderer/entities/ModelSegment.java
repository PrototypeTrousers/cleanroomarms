package proto.mechanicalarms.client.renderer.entities;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ModelSegment {
    ModelSegment parent;
    public List<ModelSegment> children = new ArrayList<>();
    Quaternionf currentRotation = new Quaternionf();
    Quaternionf prevRotation = new Quaternionf();

    public Vector3f originalVector = new Vector3f();
    public Quaternionf originalRotation = new Quaternionf();

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

    public ModelSegment(int segmentCount) {
        //RECHECK THIS
        this.baseVector.set(0f, 0f, 0);
        this.tipVector.set(1f, 0f, 0);
        tipVector.sub(baseVector, originalVector);
        originalRotation.rotationTo(new Vector3f(1, 0, 0), originalVector);
        //originalRotation.rotateZ((float) (-Math.PI/2));

        if (segmentCount > 0) {
            ModelSegment parent = this;
            for (int i = 0; i < segmentCount; i++) {
                ModelSegment child = new ModelSegment(parent);
                child.baseVector.set(1.0f + i, 0.0f, 0);
                child.tipVector.set(2.0f + i, 0.0f, 0);
                child.tipVector.sub(baseVector, child.originalVector);
                child.originalRotation.rotationTo(new Vector3f(1, 0, 0), child.originalVector);
                //child.originalRotation.rotateZ((float) (Math.PI/2));
                parent = child;
            }
        }
    }

    public void move(float x, float y, float z) {
        baseVector.x = x;
        baseVector.y = y;
        baseVector.z = z;
    }

    public Quaternionf getCurrentRotation() {
        if (baseVector.isFinite() && tipVector.isFinite()) {
            Vector3f currRotationVector = currentRotation.getEulerAnglesXYZ(new Vector3f());
            Vector3f toRotationVector = getcurvec();
            currentRotation.rotationTo(originalVector, getcurvec());
            if (currentRotation.isFinite()) {
                return currentRotation.mul(originalRotation);
            } else {
                return currentRotation.identity();
            }
        } else {
            return currentRotation.identity();
        }
    }

    public Vector3f getcurvec() {
        Vector3f direction = new Vector3f();
        tipVector.sub(baseVector, direction).normalize();
        if (!direction.isFinite()) {
            direction.set(0, 0, 0);
        }
        return direction;
    }
}
