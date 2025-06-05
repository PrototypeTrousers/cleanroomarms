package proto.mechanicalarms.client.renderer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.model.animation.FastTESR;
import proto.mechanicalarms.client.renderer.instances.MeshInstance;
import proto.mechanicalarms.client.renderer.instances.ModelInstance;
import proto.mechanicalarms.client.renderer.instances.NodeInstance;
import proto.mechanicalarms.client.renderer.util.*;
import proto.mechanicalarms.common.block.properties.Directions;
import proto.mechanicalarms.common.proxy.ClientProxy;
import proto.mechanicalarms.common.tile.TileBeltBasic;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;


public class TileBeltRenderer extends FastTESR<TileBeltBasic> {
    private static final Object2ObjectOpenCustomHashMap<ItemStack, ItemStackRenderToVAO> modelCache = new Object2ObjectOpenCustomHashMap<>(new ItemStackHasher());
    private static final Matrix4f tempModelMatrix = new Matrix4f();
    InstanceRender ir = InstanceRender.INSTANCE;
    float[] mtx = new float[16];

    Matrix4f itemBeltMtx = new Matrix4f();
    Matrix4f beltBseMtx = new Matrix4f();
    Matrix4f translationMatrix = new Matrix4f();
    Matrix4f m2 = new Matrix4f();
    byte s;
    byte b;
    byte alpha;
    Quaternion rot = Quaternion.createIdentity();
    Quaternion rotProgress = Quaternion.createIdentity();
    Matrix4fStack matrix4fStack = new Matrix4fStack(10);
    ModelInstance baseBeltModel = new ModelInstance(ClientProxy.belt);
    ModelInstance slopedBeltModel = new ModelInstance(ClientProxy.beltSlope);
    float partialTicks;
    ItemStack fakeStack = new ItemStack(Blocks.REDSTONE_BLOCK);

    public TileBeltRenderer() {
        super();
    }

    void traverseHierarchy(NodeInstance node, TileBeltBasic tileBeltBasic) {
        // Process the current node (for example, print its information)
        processNode(node, tileBeltBasic);

        // Recursively traverse each child node
        for (NodeInstance child : node.getChildren()) {
            matrix4fStack.pushMatrix();
            traverseHierarchy(child, tileBeltBasic);
            matrix4fStack.popMatrix();
        }
    }

    void processNode(NodeInstance node, TileBeltBasic tileBeltBasic) {
        for (MeshInstance m : node.getMeshes()) {
            ir.schedule(m); // Schedule the mesh for rendering

            // Reset the rotation matrix
            if (m.hasRotationFunction()) {
                rot.setIndentity();
                m.applyRotation(rot);
                translate(matrix4fStack, m.meshOrigin[0], m.meshOrigin[1], m.meshOrigin[2]);
                Quaternion.rotateMatrix(matrix4fStack, rot);
            } else {
                translate(matrix4fStack, m.meshOrigin[0], m.meshOrigin[1], m.meshOrigin[2]);
            }

            matrix4ftofloatarray(matrix4fStack, mtx);

            // Buffer matrix and lighting data
            ir.bufferModelMatrixData(mtx);
            ir.bufferLight(s, b, alpha);
        }
    }

    void renderBase(TileBeltBasic tileBeltBasic) {
        NodeInstance ni;
        if (tileBeltBasic.isSlope()) {
            if (slopedBeltModel.getRoot() == null) {
                slopedBeltModel.init();
            }
            ni = slopedBeltModel.getRoot();
        } else {
            if (baseBeltModel.getRoot() == null) {
                baseBeltModel.init();
            }
            ni = baseBeltModel.getRoot();
        }

        matrix4fStack.pushMatrix();
//        matrix4fStack.mul(translationMatrix);
//        matrix4fStack.mul(beltBseMtx);
        multiply(matrix4fStack, translationMatrix);
        multiply(matrix4fStack, beltBseMtx);
        traverseHierarchy(ni, tileBeltBasic);
        matrix4fStack.popMatrix();
    }

    void renderHoldingItem(TileBeltBasic tileBeltBasic, ItemStack curStack, int progress, int previousProgress, double x, double y, double z, boolean isLeft) {
        if (curStack.isEmpty()) {
            return;
        }

        ItemStackRenderToVAO itemvao = modelCache.get(curStack);

        if (itemvao == null) {
            itemvao = new ItemStackRenderToVAO(curStack);
            modelCache.put(curStack, itemvao);
        }
        ir.schedule(itemvao);
        //upload model matrix, light

        matrix4fStack.pushMatrix();
        itemBeltMtx.setIdentity();

        if (itemvao.renderType == RenderType.BLOCK) {
            translate(matrix4fStack, (float) x, (float) (y - 0.05), (float) z);
        } else {
            translate(matrix4fStack, (float) x, (float) (y - 0.25), (float) z);
        }

        rot.setIndentity();

        EnumFacing facing = tileBeltBasic.getFront();

        if (itemvao.renderType == RenderType.ITEM) {
            if (facing == EnumFacing.NORTH) {
                rot.rotateX((float) (-Math.PI / 2));
            } else if (facing == EnumFacing.EAST) {
                rot.rotateX((float) (-Math.PI / 2));
                rot.rotateZ((float) (-Math.PI / 2));
            } else if (facing == EnumFacing.WEST) {
                rot.rotateX((float) (Math.PI / 2));
                rot.rotateZ((float) (Math.PI / 2));
            } else {
                rot.rotateX((float) (Math.PI / 2));
            }
        }

        float itemProgress = -0.5F + (lerp(previousProgress, progress, partialTicks) / 7F);
        float itemProgressa = (lerp(previousProgress, progress, partialTicks) / 7F);
        Vector3f vecProgress = new Vector3f();
        vecProgress.z = -itemProgress;
        vecProgress.x = isLeft ? -0.2f : 0.2f;

        if (tileBeltBasic.isOnlyRightConnected()) {

            float r;
            if (isLeft) {
                r = 0.8f;
            } else {
                r = 0.4f;
            }

            float xNormalized = (float) Math.pow(itemProgressa, 0.25);
            float yNormalizedPositive = (float) Math.pow(1.0f - itemProgressa, 0.25);

            float xx = r * xNormalized;
            float yx = r * yNormalizedPositive; // y is negative in the bottom-right quadrant

            vecProgress.x = -xx + 0.6f;
            vecProgress.z = yx - 0.6f;
        }
        if (tileBeltBasic.isOnlyLeftConnected()) {

            float r;
            if (isLeft) {
                r = 0.4f;
            } else {
                r = 0.8f;
            }

            float xNormalized = (float) Math.pow(itemProgressa, 0.25);
            float yNormalizedPositive = (float) Math.pow(1.0f - itemProgressa, 0.25);

            float xx = r * xNormalized;
            float yx = r * yNormalizedPositive; // y is negative in the bottom-right quadrant

            vecProgress.x = xx - 0.6f;
            vecProgress.z = yx - 0.6f;
        }

        m2.setIdentity();
        rotProgress.setIndentity();
        m2.m03 = 1;
        m2.m13 = 0;
        m2.m23 = 0;

        if (facing == EnumFacing.SOUTH) {
            rotProgress.rotateY((float) Math.PI);
        } else if (facing == EnumFacing.WEST) {
            rotProgress.rotateY((float) Math.PI / 2);
        } else if (facing == EnumFacing.EAST) {
            rotProgress.rotateY((float) -Math.PI / 2);
        }

        Quaternion.rotateMatrix(m2, rotProgress);
        m2.transform(vecProgress);
        float yProgress = 0;
        if (tileBeltBasic.getDirection().getRelativeHeight() == Directions.RelativeHeight.BELOW) {
            yProgress = 1.0625F - (lerp(previousProgress, progress, partialTicks) * 1 / 7F);
            rot.rotateX((float) (-Math.PI / 4));
        } else if (tileBeltBasic.getDirection().getRelativeHeight() == Directions.RelativeHeight.ABOVE) {
            yProgress = 0.0625F + lerp(previousProgress, progress, partialTicks) * 1 / 7F;
            rot.rotateX((float) (Math.PI / 4));
        }

        translate(itemBeltMtx, vecProgress.x + 0.5f, yProgress + 0.5f, vecProgress.z + 0.5f);
        scale(itemBeltMtx, itemvao.suggestedScale.x / 1.5f, itemvao.suggestedScale.x / 1.5f, itemvao.suggestedScale.x / 1.5f);
        Quaternion.rotateMatrix(itemBeltMtx, rot);
        translate(itemBeltMtx, -0.5f, -0.5f, -0.5f);

        //matrix4fStack.mul(itemBeltMtx);
        multiply(matrix4fStack, itemBeltMtx);

        matrix4ftofloatarray(matrix4fStack, mtx);
        matrix4fStack.popMatrix();
        ir.bufferModelMatrixData(mtx);
        ir.bufferLight(s, b, alpha);
    }


    public void multiply(Matrix4f mat, Matrix4f other) {
        float a00 = mat.m00;
        float a01 = mat.m01;
        float a02 = mat.m02;
        float a03 = mat.m03;
        float a10 = mat.m10;
        float a11 = mat.m11;
        float a12 = mat.m12;
        float a13 = mat.m13;
        float a20 = mat.m20;
        float a21 = mat.m21;
        float a22 = mat.m22;
        float a23 = mat.m23;
        float a30 = mat.m30;
        float a31 = mat.m31;
        float a32 = mat.m32;
        float a33 = mat.m33;

        mat.m00 = Math.fma(a00, other.m00, Math.fma(a01, other.m10, Math.fma(a02, other.m20, a03 * other.m30)));
        mat.m01 = Math.fma(a00, other.m01, Math.fma(a01, other.m11, Math.fma(a02, other.m21, a03 * other.m31)));
        mat.m02 = Math.fma(a00, other.m02, Math.fma(a01, other.m12, Math.fma(a02, other.m22, a03 * other.m32)));
        mat.m03 = Math.fma(a00, other.m03, Math.fma(a01, other.m13, Math.fma(a02, other.m23, a03 * other.m33)));

        mat.m10 = Math.fma(a10, other.m00, Math.fma(a11, other.m10, Math.fma(a12, other.m20, a13 * other.m30)));
        mat.m11 = Math.fma(a10, other.m01, Math.fma(a11, other.m11, Math.fma(a12, other.m21, a13 * other.m31)));
        mat.m12 = Math.fma(a10, other.m02, Math.fma(a11, other.m12, Math.fma(a12, other.m22, a13 * other.m32)));
        mat.m13 = Math.fma(a10, other.m03, Math.fma(a11, other.m13, Math.fma(a12, other.m23, a13 * other.m33)));

        mat.m20 = Math.fma(a20, other.m00, Math.fma(a21, other.m10, Math.fma(a22, other.m20, a23 * other.m30)));
        mat.m21 = Math.fma(a20, other.m01, Math.fma(a21, other.m11, Math.fma(a22, other.m21, a23 * other.m31)));
        mat.m22 = Math.fma(a20, other.m02, Math.fma(a21, other.m12, Math.fma(a22, other.m22, a23 * other.m32)));
        mat.m23 = Math.fma(a20, other.m03, Math.fma(a21, other.m13, Math.fma(a22, other.m23, a23 * other.m33)));

        mat.m30 = Math.fma(a30, other.m00, Math.fma(a31, other.m10, Math.fma(a32, other.m20, a33 * other.m30)));
        mat.m31 = Math.fma(a30, other.m01, Math.fma(a31, other.m11, Math.fma(a32, other.m21, a33 * other.m31)));
        mat.m32 = Math.fma(a30, other.m02, Math.fma(a31, other.m12, Math.fma(a32, other.m22, a33 * other.m32)));
        mat.m33 = Math.fma(a30, other.m03, Math.fma(a31, other.m13, Math.fma(a32, other.m23, a33 * other.m33)));
    }


    public void scale(Matrix4f matrix, float x, float y, float z) {
        matrix.m00 *= x;
        matrix.m10 *= x;
        matrix.m20 *= x;
        matrix.m30 *= x;
        matrix.m01 *= y;
        matrix.m11 *= y;
        matrix.m21 *= y;
        matrix.m31 *= y;
        matrix.m02 *= z;
        matrix.m12 *= z;
        matrix.m22 *= z;
        matrix.m32 *= z;
    }

    void matrix4ftofloatarray(Matrix4f matrix4f, float[] floats) {
        floats[0] = matrix4f.m00;
        floats[1] = matrix4f.m10;
        floats[2] = matrix4f.m20;
        floats[3] = matrix4f.m30;
        floats[4] = matrix4f.m01;
        floats[5] = matrix4f.m11;
        floats[6] = matrix4f.m21;
        floats[7] = matrix4f.m31;
        floats[8] = matrix4f.m02;
        floats[9] = matrix4f.m12;
        floats[10] = matrix4f.m22;
        floats[11] = matrix4f.m32;
        floats[12] = matrix4f.m03;
        floats[13] = matrix4f.m13;
        floats[14] = matrix4f.m23;
        floats[15] = matrix4f.m33;
    }

    @Override
    public void renderTileEntityFast(TileBeltBasic tileBeltBasic, double x, double y, double z, float partialTicks, int destroyStage, float alpha, BufferBuilder buffer) {
        if (tileBeltBasic.hasWorld()) {
            Chunk c = tileBeltBasic.getWorld().getChunk(tileBeltBasic.getPos());
            s = (byte) c.getLightFor(EnumSkyBlock.SKY, tileBeltBasic.getPos());
            b = (byte) c.getLightFor(EnumSkyBlock.BLOCK, tileBeltBasic.getPos());
        } else {
            s = b = (byte) 15;
        }

        this.alpha = (byte) Math.clamp(alpha * 10, 0, 10);

        this.partialTicks = partialTicks;

        translationMatrix.setIdentity();
        rot.setIndentity();

        translate(translationMatrix, (float) x + 0.5F, (float) y, (float) z + 0.5F);
        matrix4fStack.pushMatrix();
        beltBseMtx.setIdentity();

        EnumFacing facing = tileBeltBasic.getFront();
        //model origin

        if (facing == EnumFacing.SOUTH) {
            rot.rotateY((float) (Math.PI));
        }
        if (facing == EnumFacing.EAST) {
            rot.rotateY((float) (-Math.PI / 2));
        } else if (facing == EnumFacing.WEST) {
            rot.rotateY((float) (Math.PI / 2));
        }

        if (tileBeltBasic.isSlope()) {
            if (tileBeltBasic.getDirection().getRelativeHeight() == Directions.RelativeHeight.BELOW) {
                rot.rotateX((float) (-Math.PI / 2));
            }
        }

        translate(beltBseMtx, 0, 0.6f, 0f);
        Quaternion.rotateMatrix(beltBseMtx, rot);
        translate(beltBseMtx, 0, -0.6f, 0f);
        matrix4fStack.popMatrix();

        renderBase(tileBeltBasic);
        ItemStack curStack = tileBeltBasic.getLogic().getLeftItemHandler().getStackInSlot(0);
        renderHoldingItem(tileBeltBasic, curStack, tileBeltBasic.getLogic().getProgressLeft(), tileBeltBasic.getLogic().getPreviousProgressLeft(), x, y, z, true);

        curStack = tileBeltBasic.getLogic().getRightItemHandler().getStackInSlot(0);
        renderHoldingItem(tileBeltBasic, curStack, tileBeltBasic.getLogic().getProgressRight(), tileBeltBasic.getLogic().getPreviousProgressRight(), x, y, z, false);
    }

    //    public void translate(Matrix4f mat, float x, float y, float z) {
//        mat.m03 += mat.m00 * x + mat.m01 * y + mat.m02 * z;
//        mat.m13 += mat.m10 * x + mat.m11 * y + mat.m12 * z;
//        mat.m23 += mat.m20 * x + mat.m21 * y + mat.m22 * z;
//        mat.m33 += mat.m30 * x + mat.m31 * y + mat.m32 * z;
//    }
//
    //the above function, but usinng fused multiply-add
    public void translate(Matrix4f mat, float x, float y, float z) {
        mat.m03 = Math.fma(mat.m00, x, Math.fma(mat.m01, y, (Math.fma(mat.m02, z, mat.m03))));
        mat.m13 = Math.fma(mat.m10, x, Math.fma(mat.m11, y, (Math.fma(mat.m12, z, mat.m13))));
        mat.m23 = Math.fma(mat.m20, x, Math.fma(mat.m21, y, (Math.fma(mat.m22, z, mat.m23))));
        mat.m33 = Math.fma(mat.m30, x, Math.fma(mat.m31, y, (Math.fma(mat.m32, z, mat.m33))));
    }

    private float lerp(float previous, float current, float partialTick) {
        return (previous * (1.0F - partialTick)) + (current * partialTick);
    }
}