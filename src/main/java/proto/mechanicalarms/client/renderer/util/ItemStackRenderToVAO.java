package proto.mechanicalarms.client.renderer.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import org.joml.Vector3f;
import org.lwjgl3.opengl.*;
import proto.mechanicalarms.client.renderer.instances.InstanceableModel;
import proto.mechanicalarms.client.renderer.instances.ItemStackEffectModel;

import java.awt.geom.AffineTransform;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class ItemStackRenderToVAO implements InstanceableModel {

    private final ItemStack stack;
    public int texGL;
    public int posBuffer;
    public int texBuffer;
    public int normalBuffer;
    public int colorBuffer;
    public int lightBuffer;
    public int modelTransform;
    public int vertexCount;
    public int vertexArrayBuffer;
    public boolean hasEffect;

    ItemStackEffectModel effectModel;

    public ItemStackRenderToVAO(ItemStack stack) {
        this.stack = stack.copy();
        this.setupVAO(this.stack);
    }

    public synchronized void setupVAO(ItemStack stack) {
        IBakedModel mm = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
        IBakedModel model = mm.getOverrides().handleItemState(mm, stack, null, null);
        model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.NONE, false);
        FloatBuffer pos = GLAllocation.createDirectFloatBuffer(30000);
        FloatBuffer norm = GLAllocation.createDirectFloatBuffer(30000);
        FloatBuffer tex = GLAllocation.createDirectFloatBuffer(20000);
        FloatBuffer color = GLAllocation.createDirectFloatBuffer(40000);

        int v = 0;

        List<BakedQuad> loq = new ArrayList<>(model.getQuads(null, null, 0));
        for (EnumFacing e : EnumFacing.VALUES) {
            loq.addAll(model.getQuads(null, e, 0));
        }

        //if an item model has no quads, attempt to capture its rendering
        //a missing item model has quads.

        if (loq.isEmpty()) {


            //if an item model has no quads, attempt to capture its rendering
            //a missing item model has quads.

            int originalTexId = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            GL11.glDisable(GL11.GL_CULL_FACE);

            // Allocate buffer for feedback data

            FloatBuffer feedbackBuffer = GLAllocation.createDirectFloatBuffer(875);
            do {

                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glPushMatrix();
                GL11.glLoadIdentity();
                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glPushMatrix();
                GL11.glLoadIdentity();
                GL11.glViewport(0, 0, 1, 1);

                feedbackBuffer = GLAllocation.createDirectFloatBuffer(feedbackBuffer.capacity() * 2);
                GL11.glFeedbackBuffer(GL11.GL_3D_COLOR_TEXTURE, feedbackBuffer);
                GL11.glRenderMode(GL11.GL_FEEDBACK);

                // Retrieve feedback data
                if (model.isBuiltInRenderer()) {
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.enableRescaleNormal();
                    stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
                } else {
                    Minecraft.getMinecraft().getRenderItem().renderModel(model, stack);
                }

                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glPopMatrix();
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glPopMatrix();

                // Return to normal rendering mode
            } while (GL11.glRenderMode(GL11.GL_RENDER) <= 0);
            //save the current bound texture for later.
            //maybe mixin to GlStateManager bindTexture

            texGL = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, originalTexId);

            boolean end = false;
            float[] posv = new float[9];
            while (!end) {
                float cur = feedbackBuffer.get();
                if ((int) cur == GL11.GL_POLYGON_TOKEN) {
                    feedbackBuffer.get();
                    // 3 floats for pos, 3 floats for normal and 2 floats for texture UV, per vertex. always.
                    if (pos.remaining() < 33) {
                        pos = GLAllocation.createDirectFloatBuffer(pos.capacity() * 2).put(pos);
                        norm = GLAllocation.createDirectFloatBuffer(norm.capacity() * 2).put(norm);
                        tex = GLAllocation.createDirectFloatBuffer(tex.capacity() * 2).put(tex);
                        color = GLAllocation.createDirectFloatBuffer(color.capacity() * 2).put(color);
                    }
                    for (int j = 0; j < 3; j++) {
                        v++;
                        float x = feedbackBuffer.get();
                        float y = feedbackBuffer.get();
                        float z = feedbackBuffer.get();

                        pos.put(x); //x
                        pos.put(y); //y
                        pos.put(z); //z

                        color.put(feedbackBuffer.get());//R
                        color.put(feedbackBuffer.get());//G
                        color.put(feedbackBuffer.get());//B
                        color.put(feedbackBuffer.get());//A

                        tex.put(feedbackBuffer.get()); // u
                        tex.put(feedbackBuffer.get()); // v
                        feedbackBuffer.get(); // unused
                        feedbackBuffer.get(); // unused
                    }
                    //face normal:
                    pos.position(pos.position() - 9);
                    pos.get(posv);
                    Vector3f v0 = new Vector3f(posv[0], posv[1], posv[2]);
                    Vector3f v1 = new Vector3f(posv[3], posv[4], posv[5]);
                    Vector3f v2 = new Vector3f(posv[6], posv[7], posv[8]);

                    Vector3f edge1 = new Vector3f(v2).sub(v1);
                    Vector3f edge2 = new Vector3f(v0).sub(v1);

                    Vector3f crsProd = edge1.cross(edge2, new Vector3f()); // Cross product between edge1 and edge2

                    Vector3f normal = crsProd.normalize(); // Normalization of the vector
                    for (int i = 0; i < 3; i++) {
                        norm.put(normal.x);
                        norm.put(normal.y);
                        norm.put(normal.z);
                    }
                } else {
                    end = true;
                }
                if (feedbackBuffer.position() == feedbackBuffer.limit()) {
                    end = true;
                }
            }
        } else {
            texGL = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getTextureMap().getGlTextureId();
            for (BakedQuad bq : loq) {
                int[] quadData = bq.getVertexData();

                for (int k = 0; k < 3; ++k) {
                    v++;
                    // Getting the offset for the current vertex.
                    int vertexIndex = k * 7;
                    pos.put(Float.intBitsToFloat(quadData[vertexIndex]));
                    pos.put(Float.intBitsToFloat(quadData[vertexIndex + 1]));
                    pos.put(Float.intBitsToFloat(quadData[vertexIndex + 2]));

                    tex.put(Float.intBitsToFloat(quadData[vertexIndex + 4])); //texture
                    tex.put(Float.intBitsToFloat(quadData[vertexIndex + 5])); //texture

                    int col;
                    if (bq.hasTintIndex()) {
                        col = Minecraft.getMinecraft().getItemColors().colorMultiplier(stack, bq.getTintIndex());
                    } else {
                        col = quadData[vertexIndex + 3];
                    }

                    float r = ((col & 0xFF0000) >> 16) / 255F;
                    float g = ((col & 0xFF00) >> 8) / 255F;
                    float b = (col & 0xFF) / 255F;
                    float a = ((col & 0xFF000000) >> 24) / 255F;

                    color.put(r);
                    color.put(g);
                    color.put(b);
                    color.put(a);

                    int packedNormal = quadData[vertexIndex + 6];
                    norm.put(((packedNormal) & 255) / 127.0F);
                    norm.put(((packedNormal >> 8) & 255) / 127.0F);
                    norm.put(((packedNormal >> 16) & 255) / 127.0F);

                }
                for (int k = 2; k < 4; ++k) {
                    v++;
                    // Getting the offset for the current vertex.
                    int vertexIndex = k * 7;
                    pos.put(Float.intBitsToFloat(quadData[vertexIndex]));
                    pos.put(Float.intBitsToFloat(quadData[vertexIndex + 1]));
                    pos.put(Float.intBitsToFloat(quadData[vertexIndex + 2]));

                    tex.put(Float.intBitsToFloat(quadData[vertexIndex + 4])); //texture
                    tex.put(Float.intBitsToFloat(quadData[vertexIndex + 5])); //texture

                    int col;
                    if (bq.hasTintIndex()) {
                        col = Minecraft.getMinecraft().getItemColors().colorMultiplier(stack, bq.getTintIndex());
                    } else {
                        col = quadData[vertexIndex + 3];
                    }

                    float r = ((col & 0xFF0000) >> 16) / 255F;
                    float g = ((col & 0xFF00) >> 8) / 255F;
                    float b = (col & 0xFF) / 255F;
                    float a = ((col & 0xFF000000) >> 24) / 255F;

                    color.put(r);
                    color.put(g);
                    color.put(b);
                    color.put(a);

                    int packedNormal = quadData[vertexIndex + 6];
                    norm.put(((packedNormal) & 255) / 127.0F);
                    norm.put(((packedNormal >> 8) & 255) / 127.0F);
                    norm.put(((packedNormal >> 16) & 255) / 127.0F);
                }
                v++;
                // Getting the offset for the current vertex.
                int vertexIndex = 0;
                pos.put(Float.intBitsToFloat(quadData[vertexIndex]));
                pos.put(Float.intBitsToFloat(quadData[vertexIndex + 1]));
                pos.put(Float.intBitsToFloat(quadData[vertexIndex + 2]));

                tex.put(Float.intBitsToFloat(quadData[vertexIndex + 4])); //texture
                tex.put(Float.intBitsToFloat(quadData[vertexIndex + 5])); //texture


                int col;
                if (bq.hasTintIndex()) {
                    col = Minecraft.getMinecraft().getItemColors().colorMultiplier(stack, bq.getTintIndex());
                } else {
                    col = quadData[vertexIndex + 3];
                }

                float r = ((col & 0xFF0000) >> 16) / 255F;
                float g = ((col & 0xFF00) >> 8) / 255F;
                float b = (col & 0xFF) / 255F;
                float a = ((col & 0xFF000000) >> 24) / 255F;

                color.put(r);
                color.put(g);
                color.put(b);
                color.put(a);

                int packedNormal = quadData[vertexIndex + 6];
                norm.put(((packedNormal) & 255) / 127.0F);
                norm.put(((packedNormal >> 8) & 255) / 127.0F);
                norm.put(((packedNormal >> 16) & 255) / 127.0F);

            }
        }
        GL11.glEnable(GL11.GL_CULL_FACE);

        vertexArrayBuffer = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vertexArrayBuffer);

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

        modelTransform = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, modelTransform);

        for (int k = 0; k < 4; k++) {
            GL20.glVertexAttribPointer(4 + k, 4, GL11.GL_FLOAT, false, 64, k * 16);
            GL20.glEnableVertexAttribArray(4 + k);
            GL33.glVertexAttribDivisor(4 + k, 1);
        }
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        this.vertexCount = v;

        if (stack.hasEffect()) {
            hasEffect = true;
            effectModel = new ItemStackEffectModel(this, loq);
        }
    }

    @Override
    public int getTexGlId() {
        return texGL;
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
        return hasEffect;
    }

    public InstanceableModel getEffectModel() {
        return effectModel;
    }

    public ItemStack getStack() {
        return stack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemStackRenderToVAO that = (ItemStackRenderToVAO) o;
        return (stack.getItem() == that.stack.getItem() &&
                stack.getMetadata() == that.stack.getMetadata() &&
                stack.getItemDamage() == that.stack.getItemDamage() &&
                ItemStack.areItemStackTagsEqual(stack, that.stack));
    }

    @Override
    public int hashCode() {
        int hash = stack.getItem().hashCode();
        hash += 31 * stack.getMetadata();
        hash += 31 * stack.getItemDamage();
        if (stack.getTagCompound() != null) {
            hash += 31 * stack.getTagCompound().hashCode();
        }
        return hash;
    }
}
