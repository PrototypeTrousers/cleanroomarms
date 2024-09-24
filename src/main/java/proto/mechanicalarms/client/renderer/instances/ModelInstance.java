package proto.mechanicalarms.client.renderer.instances;

import net.minecraft.util.ResourceLocation;

public class ModelInstance {
    ModelInstance(ResourceLocation resourceLocation) {
        ModelInstancer.loadglTFModel(resourceLocation);
    }
}
