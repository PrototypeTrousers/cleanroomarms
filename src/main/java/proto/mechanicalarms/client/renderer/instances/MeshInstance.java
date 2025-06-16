package proto.mechanicalarms.client.renderer.instances;

import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.*;
import proto.mechanicalarms.MechanicalArms;
import proto.mechanicalarms.client.jgltf.EmbeddedTexture;
import proto.mechanicalarms.client.renderer.util.Quaternion;

import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Objects;
import java.util.function.UnaryOperator;

public class MeshInstance implements InstanceableModel {

    public int lightBuffer;
    public int modelTransformBuffer;
    public int posBuffer;
    public int normalBuffer;
    public int texBuffer;
    public int vertexCount;
    public int elementCount;
    public int elementBufferId;
    //
    public float[] meshOrigin;
    ResourceLocation texture;
    String nodeName;
    //OpenGL
    private int texGL;
    private int vertexArrayBuffer;
    private int colorBuffer;
    private Quaternion rotation;
    private UnaryOperator<Quaternion> rotationFunction;
    private Runnable runnable;
    private UnaryOperator<Vector3f> translationVectorFunction;

    MeshInstance(NodeModel nm, MeshModel meshModel, MeshPrimitiveModel meshPrimitiveModel) {
        meshOrigin = nm.getTranslation();
        if (meshOrigin == null ){
            meshOrigin = new float[]{0,0,0};
        }
        nodeName = nm.getName();
        texture = new ResourceLocation(MechanicalArms.MODID, "meshes/" + meshModel.getName() + ".png");

        genBuffers(meshModel, meshPrimitiveModel);
    }

    void genBuffers(MeshModel meshModel, MeshPrimitiveModel meshPrimitiveModel) {

        vertexArrayBuffer = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vertexArrayBuffer);

        AccessorModel posAccessor = meshPrimitiveModel.getAttributes().get("POSITION");

        posBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, posBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, posAccessor.getAccessorData().createByteBuffer(), GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 12, 0);
        GL20.glEnableVertexAttribArray(0);

        vertexCount = posAccessor.getCount();

        AccessorModel texAccessor = meshPrimitiveModel.getAttributes().get("TEXCOORD_0");

        texBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, texBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texAccessor.getAccessorData().createByteBuffer(), GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, texAccessor.isNormalized(), 8, 0);
        GL20.glEnableVertexAttribArray(1);


        MaterialModelV2 m = ((MaterialModelV2) meshPrimitiveModel.getMaterialModel());
        TextureModel baseTexture = m.getBaseColorTexture();
        if (baseTexture == null) {
            texGL = 1;
        } else {
            EmbeddedTexture embeddedTexture = new EmbeddedTexture(baseTexture);
            Minecraft.getMinecraft().getTextureManager().loadTexture(texture, embeddedTexture);
            texGL = Minecraft.getMinecraft().getTextureManager().getTexture(texture).getGlTextureId();
        }

        AccessorModel normalAccessor = meshPrimitiveModel.getAttributes().get("NORMAL");

        normalBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalAccessor.getAccessorData().createByteBuffer(), GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, normalAccessor.isNormalized(), 12, 0);
        GL20.glEnableVertexAttribArray(2);

        FloatBuffer color = ByteBuffer.allocateDirect(vertexCount << 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (int i =0; i < vertexCount ;i++) {
            color.put(((MaterialModelV2) meshPrimitiveModel.getMaterialModel()).getBaseColorFactor());
        }
        color.rewind();

        colorBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, color, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(8, 4, GL11.GL_FLOAT, true, 16, 0);
        GL20.glEnableVertexAttribArray(8);

        lightBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lightBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 3, GL15.GL_DYNAMIC_DRAW);

        //Block Light
        GL20.glVertexAttribPointer(3, 3, GL11.GL_UNSIGNED_BYTE, false, 3, 0);
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
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, meshPrimitiveModel.getIndices().getAccessorData().createByteBuffer(), GL15.GL_STATIC_DRAW);

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
        return texGL;
    }

    @Override
    public int getElementCount() {
        return elementCount;
    }

    @Override
    public boolean hasEffect() {
        return false;
    }

    public void applyRotation(Quaternion quaternion) {
        rotationFunction.apply(quaternion);
    }

    public void applyTranslation(Vector3f vector3f) {
        translationVectorFunction.apply(vector3f);
    }

    public void setRotationFunction(UnaryOperator<Quaternion> f) {
        rotationFunction = f;
    }

    public boolean hasRotationFunction() {
        return rotationFunction != null;
    }

    public boolean hasTranslationVector() {
        return translationVectorFunction != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MeshInstance that)) return false;
        return Objects.equals(vertexArrayBuffer, that.vertexArrayBuffer);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(vertexArrayBuffer);
    }

    public void setAttachedMesh(Runnable g) {
        this.runnable = g;
    }

    public boolean hasAttachedMesh() {
        return this.runnable != null;
    }

    public Runnable getAttachedMesh() {
        return this.runnable;
    }

    public void setTranslationVector(UnaryOperator<Vector3f> v) {
        this.translationVectorFunction = v;
    }
}
