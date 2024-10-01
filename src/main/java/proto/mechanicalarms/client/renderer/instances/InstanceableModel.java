package proto.mechanicalarms.client.renderer.instances;

public interface InstanceableModel {
    int getVertexArrayBufferId();

    int getModelTransformBufferId();

    int getBlockLightBufferId();

    int getTexGlId();

    int getVertexCount();

    int getElementCount();

    boolean hasEffect();
}
