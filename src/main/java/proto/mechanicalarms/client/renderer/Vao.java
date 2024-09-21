package proto.mechanicalarms.client.renderer;

import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.io.GltfModelReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.obj.OBJModel;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl3.glfw.GLFW;
import org.lwjgl3.opengl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Vao implements InstanceableModel{

    private static final Logger log = LoggerFactory.getLogger(Vao.class);
    private int texGL;
    public int lightBuffer;
    public int modelTransformBuffer;
    public int posBuffer;
    public int normalBuffer;
    public int texBuffer;
    public int vaoId;
    public int drawMode;
    public int vertexCount;
    public boolean useElements;
    private int vertexArrayBuffer;

    IModel model;
    private int colorBuffer;

    public Vao(ResourceLocation resourceLocation) {
        GltfModel g =null;
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
    }

    public void setupVAO(IModel model) {
        vertexArrayBuffer = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vertexArrayBuffer);
        List<OBJModel.Face> fl = new ArrayList<>();
        ((OBJModel) model).getMatLib().getGroups().values().forEach(g -> fl.addAll(g.getFaces()));
        int vertexAmount = fl.size() * 3;
        FloatBuffer pos = GLAllocation.createDirectFloatBuffer(vertexAmount * 3);
        FloatBuffer norm = GLAllocation.createDirectFloatBuffer(vertexAmount * 3);
        FloatBuffer tex = GLAllocation.createDirectFloatBuffer(vertexAmount * 2);
        FloatBuffer color = GLAllocation.createDirectFloatBuffer(vertexAmount * 4);

        int v = 0;
        for (OBJModel.Face face : fl) {
            OBJModel.Vertex[] vertices = face.getVertices();
            for (int i = 0; i < 3; i++) {
                OBJModel.Vertex vertex = vertices[i];
                pos.put(vertex.getPos().x);
                pos.put(vertex.getPos().y);
                pos.put(vertex.getPos().z);
                //U,V
                tex.put(vertex.getTextureCoordinate().u);
                tex.put(vertex.getTextureCoordinate().v);
                //Normals
                norm.put(vertex.getNormal().x);
                norm.put(vertex.getNormal().y);
                norm.put(vertex.getNormal().z);
                color.put(1);
                color.put(1);
                color.put(1);
                color.put(1);
                v++;
            }
        }
        pos.rewind();
        norm.rewind();
        tex.rewind();
        color.rewind();

        posBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, posBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, pos, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0,
                3,
                GL11.GL_FLOAT,
                false,
                12,
                0);
        GL20.glEnableVertexAttribArray(0);

        texBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, texBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, tex, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, true, 8, 0);
        GL20.glEnableVertexAttribArray(1);


        normalBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, norm, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, true, 12, 0);
        GL20.glEnableVertexAttribArray(2);

        lightBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lightBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 2, GL15.GL_DYNAMIC_DRAW);

        //Light
        GL20.glVertexAttribPointer(3, 2, GL11.GL_UNSIGNED_BYTE, false, 2, 0);
        GL20.glEnableVertexAttribArray(3);
        GL33.glVertexAttribDivisor(3, 1);

        colorBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, color, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(8, 4, GL11.GL_FLOAT, true, 16, 0);
        GL20.glEnableVertexAttribArray(8);

        //Model Transform Matrix
        modelTransformBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, modelTransformBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 64, GL15.GL_DYNAMIC_DRAW);

        for (int i = 0; i < 4; i++) {
            GL20.glVertexAttribPointer(4 + i, 4, GL11.GL_FLOAT, false, 64, i * 16);
            GL20.glEnableVertexAttribArray(4 + i);
            GL33.glVertexAttribDivisor(4 + i, 1);
        }
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        this.vertexCount = v;
    }

    @Override
    public int getVertexCount() {
        return vertexCount;
    }

    @Override
    public int getVertexArrayBuffer() {
        return vertexArrayBuffer;
    }

    @Override
    public int getModelTransformBuffer() {
        return modelTransformBuffer;
    }

    @Override
    public int getBlockLightBuffer() {
        return lightBuffer;
    }

    @Override
    public int getTexGl() {
        if (texGL == 0) {
            ResourceLocation t = new ResourceLocation("mechanicalarms:textures/arm_arm.png");
            ITextureObject itextureobject = Minecraft.getMinecraft().getTextureManager().getTexture(t);

            if (itextureobject == null)
            {
                itextureobject = new SimpleTexture(t);
                Minecraft.getMinecraft().getTextureManager().loadTexture(t, itextureobject);
            }
            texGL = Minecraft.getMinecraft().getTextureManager().getTexture(t).getGlTextureId();
        }
        return texGL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vao vao = (Vao) o;
        return Objects.equals(model, vao.model);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model);
    }
}
