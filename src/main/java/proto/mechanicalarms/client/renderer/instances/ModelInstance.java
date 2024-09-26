package proto.mechanicalarms.client.renderer.instances;

import de.javagl.jgltf.model.GltfModel;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.time.StopWatch;

import java.util.List;

public class ModelInstance {
    NodeInstance root;
    ResourceLocation resourceLocation;

    public ModelInstance(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public List<MeshInstance> init() {
        //StopWatch s = StopWatch.createStarted();
        GltfModel g = ModelInstancer.loadglTFModel(resourceLocation);
        //s.stop();
        //System.out.printf("load from disk took" + (s.getStopTime() - s.getStartTime()) + " ms");
        this.root = new NodeInstance();
        return ModelInstancer.makeVertexArrayObjects(g, root);

    }

    public NodeInstance getRoot() {
        return root;
    }
}
