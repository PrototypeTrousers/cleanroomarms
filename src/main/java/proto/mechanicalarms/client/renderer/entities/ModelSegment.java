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

    public Quaternionf getCurrentRotation() {
        if (baseVector.isFinite() && tipVector.isFinite()) {
            if (parent != null) {
                float angle = parent.getcurvec().angleSigned(getcurvec(), new Vector3f(0, 1, 0));
                angle = normalizeRadians(angle);
                currentRotation.setAngleAxis(angle, 0,1,0);
                float angle2 = parent.getcurvec().angleSigned(getcurvec(), new Vector3f(0, 0, 1));
                angle2 = normalizeRadians(angle2);
                currentRotation.rotateZ((float) (angle2% Math.PI));
            } else {
                float angle = originalVector.angleSigned(getcurvec(), new Vector3f(0, 1, 0));
                angle = normalizeRadians(angle);

                float yaw = (float) Math.atan2(-0.1f, -0.1f);



                currentRotation.setAngleAxis(yaw % Math.PI, 0,1,0);
                float angle2 = originalVector.angleSigned(getcurvec(), new Vector3f(0, 0, 1));
                angle2 = normalizeRadians(angle2);
                currentRotation.rotateZ((float) (angle2% Math.PI));

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

    public Quaternionf getCurrentRotation(Vector3f endEffectorPos) {
        if (baseVector.isFinite() && tipVector.isFinite()) {
            if (parent != null) {
                float yaw = parent.getcurvec().angleSigned(getcurvec(endEffectorPos), new Vector3f(0, 1, 0));
                yaw = normalizeRadians(yaw);
                currentRotation.setAngleAxis(yaw, 0,1,0);
                float pitch = parent.getcurvec().angleSigned(getcurvec(), new Vector3f(0, 0, 1));
                pitch = normalizeRadians(pitch);
                currentRotation.rotateZ((float) (pitch% Math.PI));
            } else {
                float yaw = originalVector.angleSigned(getcurvec(endEffectorPos), new Vector3f(0, 1, 0));
                yaw = normalizeRadians(yaw);
                currentRotation.setAngleAxis(yaw % Math.PI, 0,1,0);
                float pitch = originalVector.angleSigned(getcurvec(), new Vector3f(0, 0, 1));
                pitch = normalizeRadians(pitch);
                currentRotation.rotateZ((float) (pitch% Math.PI));
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
