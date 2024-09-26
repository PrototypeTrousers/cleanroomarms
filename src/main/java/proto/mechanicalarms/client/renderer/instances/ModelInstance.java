package proto.mechanicalarms.client.renderer.instances;

import de.javagl.jgltf.model.GltfModel;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.time.StopWatch;

import java.util.List;

public class ModelInstance {
    public static List<MeshInstance> instance(ResourceLocation resourceLocation) {
        //StopWatch s = StopWatch.createStarted();
        GltfModel g = ModelInstancer.loadglTFModel(resourceLocation);
        //s.stop();
        //System.out.printf("load from disk took" + (s.getStopTime() - s.getStartTime()) + " ms");
        return ModelInstancer.makeVertexArrayObjects(g);
    }
}
