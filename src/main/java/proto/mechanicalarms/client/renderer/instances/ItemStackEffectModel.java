package proto.mechanicalarms.client.renderer.instances;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.item.ItemStack;
import org.lwjgl3.opengl.*;
import proto.mechanicalarms.client.events.Tick;
import proto.mechanicalarms.client.renderer.util.ItemStackRenderToVAO;

import java.nio.FloatBuffer;

public class ItemStackEffectModel implements InstanceableModel {

    private int posBuffer;
    private int texBuffer;
    private int normalBuffer;
    private int colorBuffer;
    private int lightBuffer;
    private int modelTransform;
    private int vertexCount;
    private int vertexArrayBuffer;

    private ItemStack stack;

    public ItemStackEffectModel(ItemStackRenderToVAO parent) {
        posBuffer = parent.posBuffer;
        normalBuffer = parent.normalBuffer;
        lightBuffer = parent.lightBuffer;
        modelTransform = parent.modelTransform;
        vertexCount = parent.vertexCount;
        stack = parent.getStack();
        setupVAO();
    }

    public void setupVAO() {
        FloatBuffer tex = GLAllocation.createDirectFloatBuffer(2000);
        FloatBuffer color = GLAllocation.createDirectFloatBuffer(4000);

        vertexArrayBuffer = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vertexArrayBuffer);

        tex.rewind();
        color.rewind();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, posBuffer);
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
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 8, 0);
        GL20.glEnableVertexAttribArray(1);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalBuffer);
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, true, 12, 0);
        GL20.glEnableVertexAttribArray(2);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lightBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 2, GL15.GL_STATIC_DRAW);

        //Light
        GL20.glVertexAttribPointer(3, 2, GL11.GL_UNSIGNED_BYTE, false, 2, 0);
        GL20.glEnableVertexAttribArray(3);
        GL33.glVertexAttribDivisor(3, 1);

        //Color

        colorBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, color, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(8, 4, GL11.GL_FLOAT, true, 16, 0);
        GL20.glEnableVertexAttribArray(8);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, modelTransform);

        for (int k = 0; k < 4; k++) {
            GL20.glVertexAttribPointer(4 + k, 4, GL11.GL_FLOAT, false, 64, k * 16);
            GL20.glEnableVertexAttribArray(4 + k);
            GL33.glVertexAttribDivisor(4 + k, 1);
        }
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    @Override
    public int getTexGlId() {
        return Tick.tintTexGL;
    }

    @Override
    public int getBlockLightBufferId() {
        return lightBuffer;
    }

    @Override
    public int getVertexArrayBufferId() {
        return vertexArrayBuffer;
    }

    @Override
    public int getModelTransformBufferId() {
        return modelTransform;
    }

    @Override
    public int getVertexCount() {
        return vertexCount;
    }

    @Override
    public int getElementCount() {
        return 0;
    }

    @Override
    public boolean hasEffect() {
        return false;
    }
}
