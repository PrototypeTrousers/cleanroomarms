package proto.mechanicalarms.client.renderer;

public interface InstanceableModel {
    int getVertexArrayBufferId();

    int getModelTransformBufferId();

    int getBlockLightBufferId();

    int getTexGlId();

    int getVertexCount();

    int getElementBufferId();

    int getElementCount();
}
