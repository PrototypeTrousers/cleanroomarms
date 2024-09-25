package proto.mechanicalarms.client.renderer.instances;

import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import org.lwjgl3.opengl.*;
import proto.mechanicalarms.client.renderer.InstanceableModel;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Objects;

public class MeshInstance implements InstanceableModel {

    //OpenGL
    private int texGL;
    public int lightBuffer;
    public int modelTransformBuffer;
    public int posBuffer;
    public int normalBuffer;
    public int texBuffer;
    public int vertexCount;
    public int elementCount;
    private int vertexArrayBuffer;
    public int elementBufferId;
    private int colorBuffer;

    //
    public float[] meshOrigin;

    MeshInstance(NodeModel nm, MeshModel meshModel, MeshPrimitiveModel meshPrimitiveModel) {
        meshOrigin = nm.getTranslation();
        NodeModel parent = nm.getParent();
        while (parent != null) {
            float[] parentOrigin = parent.getTranslation();
            meshOrigin[0] += parentOrigin[0];
            meshOrigin[1] += parentOrigin[1];
            meshOrigin[2] += parentOrigin[2];
            parent = parent.getParent();
        }
        genBuffers(meshModel, meshPrimitiveModel);
    }

    void genBuffers(MeshModel meshModel, MeshPrimitiveModel meshPrimitiveModel) {

        vertexArrayBuffer = org.lwjgl.opengl.GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vertexArrayBuffer);

        AccessorModel posAccessor = meshPrimitiveModel.getAttributes().get("POSITION");

        posBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, posBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, posAccessor.getBufferViewModel().getBufferViewData(), GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 12, 0);
        GL20.glEnableVertexAttribArray(0);

        vertexCount = posAccessor.getCount() / 3;

        AccessorModel texAccessor = meshPrimitiveModel.getAttributes().get("TEXCOORD_0");

        texBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, texBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texAccessor.getBufferViewModel().getBufferViewData(), GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, texAccessor.isNormalized(), 8, 0);
        GL20.glEnableVertexAttribArray(1);

        AccessorModel normalAccessor = meshPrimitiveModel.getAttributes().get("NORMAL");

        normalBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalAccessor.getBufferViewModel().getBufferViewData(), GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, normalAccessor.isNormalized(), 12, 0);
        GL20.glEnableVertexAttribArray(2);

        FloatBuffer color = ByteBuffer.allocateDirect(16).asFloatBuffer();
        color.put(((MaterialModelV2)meshPrimitiveModel.getMaterialModel()).getBaseColorFactor());
        color.rewind();

        colorBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, color, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(8, 4, GL11.GL_FLOAT, true, 16, 0);
        GL20.glEnableVertexAttribArray(8);

        lightBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lightBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 2, GL15.GL_DYNAMIC_DRAW);

        //Block Light
        GL20.glVertexAttribPointer(3, 2, GL11.GL_UNSIGNED_BYTE, false, 2, 0);
        GL20.glEnableVertexAttribArray(3);
        GL33.glVertexAttribDivisor(3, 1);

        //Model Transform Matrix
        modelTransformBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, modelTransformBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 64, GL15.GL_DYNAMIC_DRAW);

        for (int i = 0; i < 4; i++) {
            GL20.glVertexAttribPointer(4 + i, 4, GL11.GL_FLOAT, false, 64, i * 16);
            GL20.glEnableVertexAttribArray(4 + i);
            GL33.glVertexAttribDivisor(4 + i, 1);
        }

        elementBufferId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, elementBufferId);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, meshPrimitiveModel.getIndices().getBufferViewModel().getBufferViewData(), GL15.GL_STATIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        elementCount = meshPrimitiveModel.getIndices().getCount();
    }

    @Override
    public int getVertexCount() {
        return vertexCount;
    }

    @Override
    public int getVertexArrayBufferId() {
        return vertexArrayBuffer;
    }

    @Override
    public int getModelTransformBufferId() {
        return modelTransformBuffer;
    }

    @Override
    public int getBlockLightBufferId() {
        return lightBuffer;
    }

    @Override
    public int getTexGlId() {
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
    public int getElementBufferId() {
        return elementBufferId;
    }

    @Override
    public int getElementCount() {
        return elementCount;
    }
}
