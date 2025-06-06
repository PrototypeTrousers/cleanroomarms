package proto.mechanicalarms.client.renderer.util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.common.model.TRSRTransformation;
import org.lwjgl.opengl.*;
import proto.mechanicalarms.client.renderer.ProtoTesselator;
import proto.mechanicalarms.client.renderer.instances.InstanceableModel;
import proto.mechanicalarms.client.renderer.instances.ItemStackEffectModel;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;

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

    public RenderType renderType;

    public Vector3f suggestedScale;
    public boolean rotateX;
    public Vector3f modelCenter;
    public Vector3f dimensions;

    ItemStackEffectModel effectModel;
    public float xOffset;
    public float yOffset;
    public float zOffset;


    public ItemStackRenderToVAO(ItemStack stack) {
        this.stack = stack.copy();
        this.setupVAO(this.stack);
    }

    public static float[] getEulerAnglesYXZ(Matrix4f m) {
        // Extract the column vectors of the 3x3 rotation+scale matrix
        Vector3f xAxis = new Vector3f(m.m00, m.m01, m.m02);
        Vector3f yAxis = new Vector3f(m.m10, m.m11, m.m12);
        Vector3f zAxis = new Vector3f(m.m20, m.m21, m.m22);

        // Compute scale factors
        float scaleX = xAxis.length();
        float scaleY = yAxis.length();
        float scaleZ = zAxis.length();

        // Normalize axes to remove scale
        xAxis.scale(1.0f / scaleX);
        yAxis.scale(1.0f / scaleY);
        zAxis.scale(1.0f / scaleZ);

        // Build a pure rotation matrix
        float[][] rot = new float[3][3];
        rot[0][0] = xAxis.x; rot[0][1] = xAxis.y; rot[0][2] = xAxis.z;
        rot[1][0] = yAxis.x; rot[1][1] = yAxis.y; rot[1][2] = yAxis.z;
        rot[2][0] = zAxis.x; rot[2][1] = zAxis.y; rot[2][2] = zAxis.z;

        // Now extract Euler angles (YXZ order)
        float[] angles = new float[3];

        angles[1] = (float) Math.asin(-rot[2][0]); // Y

        if (Math.abs(rot[2][0]) < 0.99999f) {
            angles[0] = (float) Math.atan2(rot[2][1], rot[2][2]); // X
            angles[2] = (float) Math.atan2(rot[1][0], rot[0][0]); // Z
        } else {
            // Gimbal lock case
            angles[0] = 0;
            angles[2] = (float) Math.atan2(-rot[0][1], rot[1][1]);
        }

        return angles;
    }

    public synchronized void setupVAO(ItemStack stack) {
        IBakedModel mm = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
        IBakedModel model = mm.getOverrides().handleItemState(mm, stack, null, null);
        Matrix4f matModelfixed = ForgeHooksClient.handlePerspective(model, ItemCameraTransforms.TransformType.FIXED).getValue();
        Matrix4f matModelGui = ForgeHooksClient.handlePerspective(model, ItemCameraTransforms.TransformType.GUI).getValue();
        Matrix4f matModelGround = ForgeHooksClient.handlePerspective(model, ItemCameraTransforms.TransformType.GROUND).getValue();


        float[] ft = matModelfixed != null ? getEulerAnglesYXZ(matModelfixed) : new float[] {0, 0, 0};
        float[] gt = matModelGui != null ? getEulerAnglesYXZ(matModelGui) : new float[] {0, 0, 0};
        float[] groundt = matModelGround != null ? getEulerAnglesYXZ(matModelGround) : new float[] {0, 0, 0};
        
        if (model instanceof BakedItemModel) {
            renderType = RenderType.ITEM;
        } else if (model.isBuiltInRenderer()) {
            if (gt[0] == 30 && (gt[1] == 45 || gt[1] == 225)) {
                renderType = RenderType.BLOCK;
            } else if (ft[1] > 0 && ft[1] % 90 == 0) {
                renderType =RenderType.ITEM;
            } else {
                renderType = RenderType.BLOCK;
            }
        }
        else {
            if (groundt[0] == 0) {
                if (ft[0] > 0 && ft[0] % 90 == 0) {
                    renderType = RenderType.BLOCK;
                }else if (ft[0] == 0) {
                    renderType = RenderType.BLOCK;
                } else if (ft[1] > 0 && ft[1] % 90 == 0) {
                    renderType = RenderType.ITEM;
                }

            } else if (gt[0] == 30 && (gt[1] == 45 || gt[1] == 225)) {
                renderType = RenderType.BLOCK;
            } else {
                renderType = RenderType.ITEM;
            }
        }

        FloatBuffer pos = GLAllocation.createDirectFloatBuffer(300000);
        FloatBuffer norm = GLAllocation.createDirectFloatBuffer(300000);
        FloatBuffer tex = GLAllocation.createDirectFloatBuffer(200000);
        FloatBuffer color = GLAllocation.createDirectFloatBuffer(400000);

        int v = 0;

        Tessellator origTess = Tessellator.getInstance();
        Tessellator.INSTANCE = new ProtoTesselator(2097152, pos, tex, color, norm);

        //if an item model has no quads, attempt to capture its rendering
        //a missing item model has quads.

        int originalTexId = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

        // Retrieve feedback data
        if (model.isBuiltInRenderer()) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
//            if (model instanceof IItemRenderer cc) {
//                texGL = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getTextureMap().getGlTextureId();
//                cc.renderItem(stack, ItemCameraTransforms.TransformType.NONE);
//            } else {
                stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
                texGL = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
//            }
        } else {
            Minecraft.getMinecraft().getRenderItem().renderModel(model, stack);
            texGL = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getTextureMap().getGlTextureId();
        }
        v = ((ProtoTesselator) Tessellator.INSTANCE).getTvx();

        Tessellator.INSTANCE = origTess;

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, originalTexId);

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
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 3, GL15.GL_STATIC_DRAW);

        //Light
        GL20.glVertexAttribPointer(3, 3, GL11.GL_UNSIGNED_BYTE, false, 3, 0);
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
            effectModel = new ItemStackEffectModel(this);
        }
    }

    public void makeBoundingBox(FloatBuffer vertexBuffer) {
        // Initialize with extreme values
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
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


        if (width > 1) {
            xOffset = -minX;
        }
        if (height > 1) {
            yOffset = -minY;
        }
        if (depth > 1) {
            if (minZ < 0) {
                zOffset = -minZ;
            }
        }


        float s = 0.5f / Math.max(Math.max(width, height), depth);

        suggestedScale = new Vector3f(s, s, s);
        modelCenter = new Vector3f((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);
        dimensions = new Vector3f(width, height, depth);
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
                areItemStackTagsEqual(stack, that.stack));
    }

    public static boolean areItemStackTagsEqual(ItemStack itemStack, ItemStack otherStack)
    {
        if (itemStack.isEmpty() && otherStack.isEmpty())
        {
            return true;
        }
        else if (!itemStack.isEmpty() && !otherStack.isEmpty())
        {
            if (itemStack.getTagCompound() == null && otherStack.getTagCompound() != null)
            {
                return false;
            }
            else
            {
                return (itemStack.getTagCompound() == null || itemStack.getTagCompound().equals(otherStack.getTagCompound()));
            }
        }
        else
        {
            return false;
        }
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


