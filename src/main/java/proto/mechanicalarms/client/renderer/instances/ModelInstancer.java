package proto.mechanicalarms.client.renderer.instances;

//Ingests the model loaded by gltf
//and pulls the position/tex coords/normals etc

import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.MeshModel;
import de.javagl.jgltf.model.MeshPrimitiveModel;
import de.javagl.jgltf.model.NodeModel;
import de.javagl.jgltf.model.io.GltfModelReader;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModelInstancer {

    static Object2ObjectArrayMap<NodeModel, MeshModel> node2MeshMap = new Object2ObjectArrayMap<>();
    static Object2ObjectArrayMap<MeshModel, MeshPrimitiveModel> mesh2PrimitiveMeshMap = new Object2ObjectArrayMap<>();


    public static GltfModel loadglTFModel(ResourceLocation resourceLocation) {
        GltfModel g = null;
        try {
            IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation);
            Throwable var2 = null;
            try {

                g = (new GltfModelReader()).readWithoutReferences(new BufferedInputStream(resource.getInputStream()));
                g.getSkinModels();
            } catch (Throwable var12) {
                var2 = var12;
                throw var12;
            } finally {
                if (resource != null) {
                    if (var2 != null) {
                        try {
                            resource.close();
                        } catch (Throwable var11) {
                            var2.addSuppressed(var11);
                        }
                    } else {
                        resource.close();
                    }
                }

            }
        } catch (IOException var14) {
            IOException e = var14;
            e.printStackTrace();
        }
        return g;
    }

    public static List<MeshInstance> makeVertexArrayObjects(GltfModel gltfModel) {
        List<MeshInstance> l = new ArrayList<>();
        for (NodeModel nm : gltfModel.getNodeModels()) {
            for (MeshModel mm : nm.getMeshModels()) {
                node2MeshMap.put(nm,mm);
                for (MeshPrimitiveModel pm : mm.getMeshPrimitiveModels()) {
                    mesh2PrimitiveMeshMap.put(mm,pm);
                    l.add(new MeshInstance(nm, mm, pm));
                }
            }
        }
        return l;
    }
}
