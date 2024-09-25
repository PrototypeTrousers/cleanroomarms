package proto.mechanicalarms.client.renderer.instances;

import de.javagl.jgltf.model.GltfModel;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class ModelInstance {
    public static List<MeshInstance> instance(ResourceLocation resourceLocation) {
        GltfModel g = ModelInstancer.loadglTFModel(resourceLocation);
        return ModelInstancer.makeVertexArrayObjects(g);
    }
}
