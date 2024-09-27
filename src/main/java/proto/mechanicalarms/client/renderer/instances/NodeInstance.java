package proto.mechanicalarms.client.renderer.instances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NodeInstance {
    NodeInstance parent;
    List<NodeInstance> children = Collections.emptyList();
    List<MeshInstance> meshes = Collections.emptyList();

    NodeInstance() {
    }

    public void setParent(NodeInstance parent) {
        this.parent = parent;
    }

    public List<NodeInstance> getChildren() {
        return children;
    }

    public List<MeshInstance> getMeshes() {
        return meshes;
    }

    public void addMeshes(List<MeshInstance> mesh) {
        if (this.meshes.isEmpty()) {
            this.meshes = new ArrayList<>(1);
        }
        this.meshes.addAll(mesh);
    }

    public void addMesh(MeshInstance mesh) {
        if (this.meshes.isEmpty()) {
            this.meshes = new ArrayList<>(1);
        }
        this.meshes.add(mesh);
    }

    public void addChildren(List<NodeInstance> child) {
        if (this.children.isEmpty()) {
            this.children = new ArrayList<>(1);
        }
        this.children.addAll(child);
    }

    public void addChild(NodeInstance child) {
        if (this.children.isEmpty()) {
            this.children = new ArrayList<>(1);
        }
        this.children.add(child);
    }


}
