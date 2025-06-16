package proto.mechanicalarms.client.renderer.instances;

import de.javagl.jgltf.model.GltfModel;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.time.StopWatch;
import proto.mechanicalarms.client.renderer.util.Quaternion;

import javax.vecmath.Vector3f;
import java.util.function.UnaryOperator;

public class ModelInstance {
    NodeInstance root;
    ResourceLocation resourceLocation;
    Object2ObjectArrayMap<String, UnaryOperator<Quaternion>> rmap = new Object2ObjectArrayMap<>();
    Object2ObjectArrayMap<String, UnaryOperator<Vector3f>> tmap = new Object2ObjectArrayMap<>();
    Object2ObjectArrayMap<String, Runnable> attachedmap = new Object2ObjectArrayMap<>();

    public ModelInstance(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public void setMeshRotationFunction(String meshName, UnaryOperator<Quaternion> f) {
        rmap.put(meshName, f);
    }

    public void setMeshTranslationFunction(String meshName, UnaryOperator<Vector3f> f) {
        tmap.put(meshName, f);
    }

    public void init() {
        StopWatch s = StopWatch.createStarted();
        GltfModel g = ModelInstancer.loadglTFModel(resourceLocation);
        s.stop();
        System.out.printf("load from disk took" + (s.getTime() - s.getStartTime()) + " ms");
        this.root = new NodeInstance();
        ModelInstancer.makeVertexArrayObjects(this, g, root);
    }

    public NodeInstance getRoot() {
        return root;
    }

    public void attachModel(String secondArm, Runnable o) {
        attachedmap.put(secondArm, o);
    }
}
