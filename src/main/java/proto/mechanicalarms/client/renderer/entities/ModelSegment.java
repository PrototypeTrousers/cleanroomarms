package proto.mechanicalarms.client.renderer.entities;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ModelSegment {
    ModelSegment parent;
    ModelSegment[] children = new ModelSegment[0];
    Quaternionf currentRotation = new Quaternionf();
    Quaternionf prevRotation = new Quaternionf();
    Vector3f currentTranslation = new Vector3f();
}
