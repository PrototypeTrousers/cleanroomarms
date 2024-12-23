package proto.mechanicalarms.client.renderer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.model.animation.FastTESR;
import proto.mechanicalarms.client.renderer.instances.MeshInstance;
import proto.mechanicalarms.client.renderer.instances.ModelInstance;
import proto.mechanicalarms.client.renderer.instances.NodeInstance;
import proto.mechanicalarms.client.renderer.util.*;
import proto.mechanicalarms.common.proxy.ClientProxy;
import proto.mechanicalarms.common.tile.Slope;
import proto.mechanicalarms.common.tile.TileBeltBasic;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.util.function.Supplier;


public class TileBeltRenderer extends FastTESR<TileBeltBasic> {
    private static final Object2ObjectOpenCustomHashMap<ItemStack, ItemStackRenderToVAO> modelCache = new Object2ObjectOpenCustomHashMap<>(new ItemStackHasher());
    private final Matrix4f tempModelMatrix = new Matrix4f();
    InstanceRender ir = InstanceRender.INSTANCE;
    TileBeltBasic renderingTE;
    float[] mtx = new float[16];

    Matrix4f itemBeltMtx = new Matrix4f();
    Matrix4f beltBseMtx = new Matrix4f();
    Matrix4f translationMatrix = new Matrix4f();
    byte s;
    byte b;
    Quaternion rot = Quaternion.createIdentity();
    Matrix4fStack matrix4fStack = new Matrix4fStack(10);
    ModelInstance baseBeltModel = new ModelInstance(ClientProxy.belt);
    ModelInstance slopedBeltModel = new ModelInstance(ClientProxy.beltSlope);
    float partialTicks;
    ItemStack fakeStack = new ItemStack(Items.END_CRYSTAL);

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
            ir.bufferLight(s, b);
        }
    }

    public Supplier<TileBeltBasic> getRenderingTE() {
        return () -> renderingTE;
    }

    void setRenderingTE(TileBeltBasic tileBeltBasic) {
        renderingTE = tileBeltBasic;
    }

    void renderBase(TileBeltBasic tileBeltBasic) {
        setRenderingTE(tileBeltBasic);
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
        matrix4fStack.mul(translationMatrix);
        matrix4fStack.mul(beltBseMtx);
        traverseHierarchy(ni, tileBeltBasic);
        matrix4fStack.popMatrix();
        setRenderingTE(null);
    }

    void renderHoldingItem(TileBeltBasic tileBeltBasic, double x, double y, double z) {
        ItemStack curStack = tileBeltBasic.getMainItemHandler().getStackInSlot(0);

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

        Vector3f p = new Vector3f(0.5f, 0.5F, 0.5f);

        if (itemvao.renderType == RenderType.BLOCK) {
            translate(matrix4fStack, new Vector3f((float) x, (float) (y - 0.05), (float) z));
        } else {
            translate(matrix4fStack, new Vector3f((float) x, (float) (y - 0.25), (float) z));
        }

        rot.setIndentity();

        Vector3f ap = new Vector3f(p);
        ap.negate();

        EnumFacing facing = tileBeltBasic.getFront();


        if (itemvao.renderType == RenderType.ITEM) {
            if (facing == EnumFacing.NORTH) {
                rot.rotateX((float) (-Math.PI / 2));
            } else if (facing == EnumFacing.EAST) {
                rot.rotateZ((float) (-Math.PI / 2));
            } else if (facing == EnumFacing.WEST) {
                rot.rotateZ((float) (Math.PI / 2));
            } else {
                rot.rotateX((float) (Math.PI / 2));
            }
        }

        float itemProgress = (float) (-0.5F + lerp(tileBeltBasic.getPreviousProgress(), tileBeltBasic.getProgress(), partialTicks) * 0.05);
        Vector3f vecProgress = new Vector3f();

        if (facing == EnumFacing.NORTH) {
            vecProgress.z = -itemProgress;
        }
        if (facing == EnumFacing.SOUTH) {
            vecProgress.z = itemProgress;
            rot.rotateY((float) (Math.PI));
        }
        if (facing == EnumFacing.EAST) {
            vecProgress.x = itemProgress;
            rot.rotateY((float) (-Math.PI / 2));
        } else if (facing == EnumFacing.WEST) {
            vecProgress.x = -itemProgress;
            rot.rotateY((float) (Math.PI / 2));
        }

        float yProgress = 0;
        if (tileBeltBasic.getSlope() == Slope.DOWN) {
            yProgress = 1.0625F - (float) (lerp(tileBeltBasic.getPreviousProgress(), tileBeltBasic.getProgress(), partialTicks) * 0.05);
            rot.rotateX((float) (-Math.PI / 4));


        } else if (tileBeltBasic.getSlope() == Slope.UP) {
            yProgress = (float) (0.0625F + lerp(tileBeltBasic.getPreviousProgress(), tileBeltBasic.getProgress(), partialTicks) * 0.05);
            rot.rotateX((float) (Math.PI / 4));
        }

        translate(itemBeltMtx, vecProgress.x, yProgress, vecProgress.z);

        translate(itemBeltMtx, p);
        itemBeltMtx.setScale(itemvao.suggestedScale.x);

        Quaternion.rotateMatrix(itemBeltMtx, rot);


        translate(itemBeltMtx, ap);


        matrix4fStack.mul(itemBeltMtx);

        matrix4ftofloatarray(matrix4fStack, mtx);
        matrix4fStack.popMatrix();
        ir.bufferModelMatrixData(mtx);
        ir.bufferLight(s, b);
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
    public void renderTileEntityFast(TileBeltBasic tileBeltBasic, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
        Chunk c = tileBeltBasic.getWorld().getChunk(tileBeltBasic.getPos());
        s = (byte) c.getLightFor(EnumSkyBlock.SKY, tileBeltBasic.getPos());
        b = (byte) c.getLightFor(EnumSkyBlock.BLOCK, tileBeltBasic.getPos());
        this.partialTicks = partialTicks;

        translationMatrix.setIdentity();
        rot.setIndentity();

        translate(translationMatrix, (float) x + 0.5F, (float) y, (float) z + 0.5F);
        matrix4fStack.pushMatrix();
        beltBseMtx.setIdentity();

        EnumFacing facing = tileBeltBasic.getFront();
        float xOff = 0;
        float zOff = 0;
        float yOff = 0;


        //model origin
        Vector3f p = new Vector3f(0, 0.6f, 0f);
        Vector3f ap = new Vector3f(p);
        ap.negate();

        if (facing == EnumFacing.SOUTH) {
            rot.rotateY((float) (Math.PI));
        }
        if (facing == EnumFacing.EAST) {
            rot.rotateY((float) (-Math.PI / 2));
        } else if (facing == EnumFacing.WEST) {
            rot.rotateY((float) (Math.PI / 2));
        }


        if (tileBeltBasic.isSlope()) {
            if (tileBeltBasic.getSlope() == Slope.DOWN) {
                rot.rotateX((float) (-Math.PI / 2));
            }
        }

        translate(beltBseMtx, p);
        Quaternion.rotateMatrix(beltBseMtx, rot);
        translate(beltBseMtx, ap);
        matrix4fStack.popMatrix();

        renderBase(tileBeltBasic);


        renderHoldingItem(tileBeltBasic, x + xOff, y + yOff, z + zOff);
    }

    Matrix4f fbToM4f(FloatBuffer fb, Matrix4f mat) {
        mat.m00 = fb.get();
        mat.m01 = fb.get();
        mat.m02 = fb.get();
        mat.m03 = fb.get();
        mat.m10 = fb.get();
        mat.m11 = fb.get();
        mat.m12 = fb.get();
        mat.m13 = fb.get();
        mat.m20 = fb.get();
        mat.m21 = fb.get();
        mat.m22 = fb.get();
        mat.m23 = fb.get();
        mat.m30 = fb.get();
        mat.m31 = fb.get();
        mat.m32 = fb.get();
        mat.m33 = fb.get();
        fb.rewind();
        return mat;
    }

    public Matrix4f createTranslateMatrix(float x, float y, float z) {
        Matrix4f matrix = new Matrix4f();
        matrix.m00 = 1.0F;
        matrix.m11 = 1.0F;
        matrix.m22 = 1.0F;
        matrix.m33 = 1.0F;
        matrix.m03 = x;
        matrix.m13 = y;
        matrix.m23 = z;
        return matrix;
    }


    public void translate(Matrix4f mat, float x, float y, float z) {
        mat.m03 += mat.m00 * x + mat.m01 * y + mat.m02 * z;
        mat.m13 += mat.m10 * x + mat.m11 * y + mat.m12 * z;
        mat.m23 += mat.m20 * x + mat.m21 * y + mat.m22 * z;
        mat.m33 += mat.m30 * x + mat.m31 * y + mat.m32 * z;
    }

    void rotateX(Matrix4f matrix, float angle) {
        this.tempModelMatrix.setIdentity();
        this.tempModelMatrix.rotX(angle);
        matrix.mul(this.tempModelMatrix);
    }

    void rotateY(Matrix4f matrix, float angle) {
        this.tempModelMatrix.setIdentity();
        this.tempModelMatrix.rotY(angle);
        matrix.mul(this.tempModelMatrix);
    }

    void rotateZ(Matrix4f matrix, float angle) {
        this.tempModelMatrix.setIdentity();
        this.tempModelMatrix.rotZ(angle);
        matrix.mul(this.tempModelMatrix);
    }

    void translate(Matrix4f matrix, Vector3f translation) {
        this.tempModelMatrix.setIdentity();
        this.tempModelMatrix.setTranslation(translation);
        matrix.mul(this.tempModelMatrix);
    }

    void restoreScale(Matrix4f matrix) {
        this.tempModelMatrix.setIdentity();
        this.tempModelMatrix.setM00(1F);
        this.tempModelMatrix.setM11(1F);
        this.tempModelMatrix.setM22(1F);
        matrix.mul(this.tempModelMatrix);
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

    void moveToPivot(Matrix4f matrix, Vector3f pivot) {
        this.tempModelMatrix.setIdentity();
        this.tempModelMatrix.setTranslation(pivot);
        matrix.mul(this.tempModelMatrix);
    }

    private float lerp(float previous, float current, float partialTick) {
        var diff = Math.abs(previous) - Math.abs(current);
        if (diff > Math.PI) {
            previous = 0;
        } else if (diff < -Math.PI) {
            current = 0;
        }
        return (previous * (1.0F - partialTick)) + (current * partialTick);
    }

    public void rotate(Quaternion quaternion, Matrix4f matrix4f) {
        // setup rotation matrix
        float xx = 2.0F * quaternion.x * quaternion.x;
        float yy = 2.0F * quaternion.y * quaternion.y;
        float zz = 2.0F * quaternion.z * quaternion.z;
        float xy = quaternion.x * quaternion.y;
        float yz = quaternion.y * quaternion.z;
        float zx = quaternion.z * quaternion.x;
        float xw = quaternion.x * quaternion.w;
        float yw = quaternion.y * quaternion.w;
        float zw = quaternion.z * quaternion.w;

        float r00 = 1.0F - yy - zz;
        float r11 = 1.0F - zz - xx;
        float r22 = 1.0F - xx - yy;
        float r10 = 2.0F * (xy + zw);
        float r01 = 2.0F * (xy - zw);
        float r20 = 2.0F * (zx - yw);
        float r02 = 2.0F * (zx + yw);
        float r21 = 2.0F * (yz + xw);
        float r12 = 2.0F * (yz - xw);

        // multiply matrices
        float f00 = matrix4f.m00;
        float f01 = matrix4f.m01;
        float f02 = matrix4f.m02;
        float f10 = matrix4f.m10;
        float f11 = matrix4f.m11;
        float f12 = matrix4f.m12;
        float f20 = matrix4f.m20;
        float f21 = matrix4f.m21;
        float f22 = matrix4f.m22;
        float f30 = matrix4f.m30;
        float f31 = matrix4f.m31;
        float f32 = matrix4f.m32;

        matrix4f.m00 = f00 * r00 + f01 * r10 + f02 * r20;
        matrix4f.m01 = f00 * r01 + f01 * r11 + f02 * r21;
        matrix4f.m02 = f00 * r02 + f01 * r12 + f02 * r22;
        matrix4f.m10 = f10 * r00 + f11 * r10 + f12 * r20;
        matrix4f.m11 = f10 * r01 + f11 * r11 + f12 * r21;
        matrix4f.m12 = f10 * r02 + f11 * r12 + f12 * r22;
        matrix4f.m20 = f20 * r00 + f21 * r10 + f22 * r20;
        matrix4f.m21 = f20 * r01 + f21 * r11 + f22 * r21;
        matrix4f.m22 = f20 * r02 + f21 * r12 + f22 * r22;
        matrix4f.m30 = f30 * r00 + f31 * r10 + f32 * r20;
        matrix4f.m31 = f30 * r01 + f31 * r11 + f32 * r21;
        matrix4f.m32 = f30 * r02 + f31 * r12 + f32 * r22;
    }
}