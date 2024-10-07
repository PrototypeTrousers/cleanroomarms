package proto.mechanicalarms.client.renderer.util;

import codechicken.lib.render.item.IItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl3.opengl.*;
import proto.mechanicalarms.client.renderer.ProtoTesselator;
import proto.mechanicalarms.client.renderer.instances.InstanceableModel;
import proto.mechanicalarms.client.renderer.instances.ItemStackEffectModel;

import javax.vecmath.Vector3f;
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

    public Vector3f suggestedScale;
    public boolean rotateX;
    public Vector3f modelCenter;

    ItemStackEffectModel effectModel;
    public float xOffset;
    public float yOffset;
    public float zOffset;

    public ItemStackRenderToVAO(ItemStack stack) {
        this.stack = stack.copy();
        this.setupVAO(this.stack);
    }

    public synchronized void setupVAO(ItemStack stack) {
        IBakedModel mm = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
        IBakedModel model = mm.getOverrides().handleItemState(mm, stack, null, null);
        model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.NONE, false);

        FloatBuffer pos = GLAllocation.createDirectFloatBuffer(300000);
        FloatBuffer norm = GLAllocation.createDirectFloatBuffer(300000);
        FloatBuffer tex = GLAllocation.createDirectFloatBuffer(200000);
        FloatBuffer color = GLAllocation.createDirectFloatBuffer(400000);

        int v = 0;

        List<BakedQuad> loq = new ArrayList<>(model.getQuads(null, null, 0));
        for (EnumFacing e : EnumFacing.VALUES) {
            loq.addAll(model.getQuads(null, e, 0));
        }


        //if an item model has no quads, attempt to capture its rendering
        //a missing item model has quads.

        if (loq.isEmpty()) {

            Tessellator origTess = Tessellator.getInstance();
            Tessellator.INSTANCE = new ProtoTesselator(2097152, pos, tex, color, norm);

            //if an item model has no quads, attempt to capture its rendering
            //a missing item model has quads.

            int originalTexId = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

            // Retrieve feedback data
            if (model.isBuiltInRenderer()) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();
                if (model instanceof IItemRenderer cc) {
                    texGL = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getTextureMap().getGlTextureId();
                    cc.renderItem(stack, ItemCameraTransforms.TransformType.NONE);
                } else {
                    stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
                    texGL = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
                }
            } else {
                Minecraft.getMinecraft().getRenderItem().renderModel(model, stack);
                texGL = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getTextureMap().getGlTextureId();
            }
            v = ((ProtoTesselator) Tessellator.INSTANCE).getTvx();

            Tessellator.INSTANCE = origTess;

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, originalTexId);
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

        vertexArrayBuffer = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vertexArrayBuffer);



        makeBoundingBox(pos);
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

    public void makeBoundingBox(FloatBuffer vertexBuffer) {
        // Initialize with extreme values
        float minX= Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

        // Iterate over the FloatBuffer (assuming it's 3 floats per vertex: x, y, z)
        for (int i = 0; i < vertexBuffer.limit(); i += 3) {
            float x = vertexBuffer.get(i);
            float y = vertexBuffer.get(i + 1);
            float z = vertexBuffer.get(i + 2);

            // Update min values
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (z < minZ) minZ = z;

            // Update max values
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            if (z > maxZ) maxZ = z;
        }

        float width = maxX - minX;
        float height = maxY - minY;
        float depth = maxZ - minZ;

        if (height > depth) {
            rotateX = true;
        }

        if(width > 1) {
            xOffset = -minX;
        } if (height>1){
            yOffset = -minY;
        }if (depth > 1) {
            if (minZ < 0) {
                zOffset = -minZ;
            }
        }

        float s = 0.5f / Math.max(Math.max(width,height),depth);

        suggestedScale = new Vector3f(s,s,s);
        modelCenter = new Vector3f((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);
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
