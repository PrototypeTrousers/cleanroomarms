package proto.mechanicalarms.client.renderer.entities;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class KinematicChain {
    public ModelSegment root;
    List<KinematicChain> children = new ArrayList<>();
    KinematicChain parent;
    int maxIteration = 10;
    float epsilon = 0.1f;
    public Vector3f endEffectorPosition = new Vector3f();

    public KinematicChain(ModelSegment root) {
        this.root = root;
    }

    public KinematicChain(KinematicChain parent, ModelSegment root) {
        this.root = root;
        this.parent = parent;
        parent.children.add(this);
    }

    public void doFabrik(Vector3f target) {
        Vector3f direction = new Vector3f(target).setComponent(1, 0).normalize();;
        fabrikBackward2(root, root.baseVector, direction);
        restPose(root, root.baseVector);
        for (int i = 0; i < maxIteration; i++) {
            fabrikForward(root, target);
            fabrikBackward(root, root.baseVector);
        }
        endEffectorPosition.set(target);
    }

    /**
     * Updates the entire chain's position after the root's base has been moved.
     * This method propagates the change from the root down to the end-effector.
     *
     * @param newBasePosition The new position for the base of the root segment.
     */
    public void updateFromNewBase(Vector3f newBasePosition) {
        for (KinematicChain kc : children) {
            kc.fabrikBackward(kc.root, newBasePosition);
        }
    }

    /**
     * The Forward Pass (End-Effector to Root), implemented with post-order recursion.
     * This function works by "unwinding" the call stack. The logic for each segment
     * runs *after* the recursive call for its child has returned.
     *
     * @param segment The current segment being processed.
     * @param target The final goal for the end of the chain.
     * @return The new, corrected position of the segment's base.
     */
    private Vector3f fabrikForward(ModelSegment segment, Vector3f target) {
        // --- Base Case: If we're at the end effector. ---
        if (segment.children.isEmpty()) {
            segment.tipVector.set(target);
        }
        // --- Recursive Step: If we are not at the end. ---
        else {
            // Recurse all the way down the chain first. The returned vector is the
            // corrected position where our tip should attach.
            for (int i = 0; i < segment.children.size(); i++) {
                Vector3f childsCorrectedBase = fabrikForward(segment.children.get(i), target);
                segment.tipVector.set(childsCorrectedBase);
            }
        }

        if (segment.parent == null) {
            return segment.baseVector;
        }

        // --- This logic runs for EVERY segment on the way back up the chain. ---
        // Calculate the new base position by moving backward from the corrected tip.
        Vector3f direction = new Vector3f();
        segment.baseVector.sub(segment.tipVector, direction).normalize();
        if (!direction.isFinite()) {
            direction.set(0, 0, 0);
        }
        segment.baseVector.set(segment.tipVector).add(direction.mul(segment.length));

        // Return our corrected base position for our parent to use.
        return segment.baseVector;
    }

    /**
     * The Backward Pass (Root to End-Effector), implemented with pre-order recursion.
     * This function works by "descending" the call stack. The logic for each segment
     * runs *before* the recursive call for its child.
     *
     * @param segment The current segment being processed.
     * @param constraintPosition The position where this segment's base must be.
     */
    private void fabrikBackward(ModelSegment segment, Vector3f constraintPosition) {
        // --- This logic runs for the current segment on the way down the chain. ---
        // Set our base to the constrained position provided by our parent.
        segment.baseVector.set(constraintPosition);

        // Calculate the new tip position by moving forward from the corrected base.
        Vector3f direction = new Vector3f();
        segment.tipVector.sub(segment.baseVector, direction).normalize();
        if (!direction.isFinite()) {
            direction.set(0, 0, 0);
        }
        segment.tipVector.set(segment.baseVector).add(direction.mul(segment.length));

        // --- Recursive Step: If we are not at the end. ---
        // Call the function for our child, passing our new tip as its constraint.
        if (!segment.children.isEmpty()) {
            for (int i = 0; i < segment.children.size(); i++) {
                fabrikBackward(segment.children.get(i), segment.tipVector);
            }
        }
    }

    private void fabrikBackward2(ModelSegment segment, Vector3f constraintPosition, Vector3f direction) {
        // --- This logic runs for the current segment on the way down the chain. ---
        // Set our base to the constrained position provided by our parent.
        segment.baseVector.set(constraintPosition);
        segment.tipVector.set(segment.baseVector).add(direction.mul(segment.length));

        // --- Recursive Step: If we are not at the end. ---
        // Call the function for our child, passing our new tip as its constraint.
        if (!segment.children.isEmpty()) {
            for (int i = 0; i < segment.children.size(); i++) {
                fabrikBackward2(segment.children.get(i), segment.tipVector, direction);
            }
        }
    }

    private void restPose(ModelSegment segment, Vector3f constraintPosition) {
        // --- This logic runs for the current segment on the way down the chain. ---
        // Set our base to the constrained position provided by our parent.
        segment.baseVector.set(constraintPosition);
        segment.tipVector.set(segment.baseVector).add(segment.restingTipDirection.mul(segment.length));

        // --- Recursive Step: If we are not at the end. ---
        // Call the function for our child, passing our new tip as its constraint.
        if (!segment.children.isEmpty()) {
            for (int i = 0; i < segment.children.size(); i++) {
                restPose(segment.children.get(i), segment.tipVector);
            }
        }
    }
}
