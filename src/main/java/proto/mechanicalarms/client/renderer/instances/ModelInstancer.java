package proto.mechanicalarms.client.renderer.instances;

//Ingests the model loaded by gltf
//and pulls the position/tex coords/normals etc

import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import proto.mechanicalarms.client.renderer.util.Quaternion;

import javax.vecmath.Vector3f;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class ModelInstancer {

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

    public static List<MeshInstance> makeVertexArrayObjects(ModelInstance modelInstance, GltfModel gltfModel, NodeInstance root) {
        List<MeshInstance> l = new ArrayList<>();
        for (SceneModel sm : gltfModel.getSceneModels()) {
            for (NodeModel nm : sm.getNodeModels()) {
                addNodeChildren(modelInstance, nm, l, root);
            }
        }
        return l;
    }

    static void addNodeChildren(ModelInstance modelInstance, NodeModel nm, List<MeshInstance> l, NodeInstance parentNode) {
        NodeInstance currentNode = new NodeInstance();
        parentNode.addChild(currentNode);
        currentNode.setParent(parentNode);  // Set parent immediately after adding as a child

        for (NodeModel mm : nm.getChildren()) {
            addNodeChildren(modelInstance, mm, l, currentNode);  // Pass currentNode as the parent for children
        }

        for (MeshModel mm : nm.getMeshModels()) {
            for (MeshPrimitiveModel pm : mm.getMeshPrimitiveModels()) {
                MeshInstance mi = new MeshInstance(nm, mm, pm);
                UnaryOperator<Quaternion> f = modelInstance.rmap.get(nm.getName());
                if (f != null) {
                    mi.setRotationFunction(f);
                }
                UnaryOperator<Vector3f> v = modelInstance.tmap.get(nm.getName());
                if (v != null) {
                    mi.setTranslationVector(v);
                }
                Runnable g = modelInstance.attachedmap.get(nm.getName());
                if (g != null) {
                    mi.setAttachedMesh(g);
                }
                currentNode.addMesh(mi);
            }
        }
    }
}
