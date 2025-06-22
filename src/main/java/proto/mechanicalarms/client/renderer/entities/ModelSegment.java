package proto.mechanicalarms.client.renderer.entities;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ModelSegment {
    public static final Vector3f UP = new Vector3f(0, 1, 0);
    public static final Vector3f DOWN = new Vector3f(0, -1, 0);
    public static final Vector3f RIGHT = new Vector3f(1, 0, 0);
    public static final Vector3f LEFT = new Vector3f(-1, 0, 0);
    public static final Vector3f FORWARD = new Vector3f(0, 0, 1);
    public static final Vector3f BACKWARD = new Vector3f(0, 0, -1);

    ModelSegment parent;
    public List<ModelSegment> children = new ArrayList<>();
    Quaternionf currentRotation = new Quaternionf();
    Quaternionf yawQ = new Quaternionf();
    Quaternionf pitchQ = new Quaternionf();
    Quaternionf prevRotation = new Quaternionf();

    public Vector3f originalVector = new Vector3f();
    public Quaternionf originalRotation = new Quaternionf();

    public Vector3f getBaseVector() {
        return baseVector;
    }

    Vector3f baseVector = new Vector3f();
    Vector3f tipVector = new Vector3f();
    Vector3f restingTipDirection = new Vector3f();
    float length = 1f;

    public ModelSegment(Vector3f tipDirection, Vector3f restingDirection) {
        this.tipVector.set(tipDirection);
        this.originalVector.set(tipDirection);
        restingTipDirection.set(restingDirection);
    }

    public ModelSegment withChild(ModelSegment child) {
        child.parent = this;
        children.add(child);
        return this;
    }

    public void move(float x, float y, float z) {
        baseVector.x = x;
        baseVector.y = y;
        baseVector.z = z;
        tipVector.set(baseVector).add(originalVector.mul(length));
    }

    public static float normalizeRadians(float angleRadians)
    {
        angleRadians %= (2 *(float)Math.PI);

        if (angleRadians >= (float)Math.PI)
        {
            angleRadians -= (2 * (float)Math.PI);
        }

        if (angleRadians < -(float)Math.PI)
        {
            angleRadians += (2 * (float)Math.PI);
        }

        if (Math.PI - Math.abs(angleRadians) < 0.0001f) {
            angleRadians = 0;
        }

        return angleRadians;
    }

    public Quaternionf getCurrentRotation(Vector3f endEffectorPos) {
        if (baseVector.isFinite() && tipVector.isFinite()) {
            if (parent != null) {
                Vector3f v3 = getcurvec().setComponent(2,0);
                pitchQ.rotationTo(parent.getcurvec().setComponent(2,0), v3);

                currentRotation.set(yawQ).mul(pitchQ);
            } else {
                Vector3f v2 = getcurvec(endEffectorPos).setComponent(1,0);
                yawQ.rotationTo(originalVector, v2);

                Vector3f v3 = getcurvec().setComponent(2,0);
                pitchQ.rotationTo(originalVector, v3);

                currentRotation.set(yawQ).mul(pitchQ);
            }
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

    public Vector3f getcurvec(Vector3f target) {
        Vector3f direction = new Vector3f();
        target.sub(baseVector, direction).normalize();
        if (!direction.isFinite()) {
            direction.set(0, 0, 0);
        }
        return direction;
    }
}
