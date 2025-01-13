package proto.mechanicalarms.client.renderer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.model.animation.FastTESR;
import proto.mechanicalarms.client.renderer.instances.MeshInstance;
import proto.mechanicalarms.client.renderer.instances.ModelInstance;
import proto.mechanicalarms.client.renderer.instances.NodeInstance;
import proto.mechanicalarms.client.renderer.util.ItemStackHasher;
import proto.mechanicalarms.client.renderer.util.ItemStackRenderToVAO;
import proto.mechanicalarms.client.renderer.util.Matrix4fStack;
import proto.mechanicalarms.client.renderer.util.Quaternion;
import proto.mechanicalarms.common.proxy.ClientProxy;
import proto.mechanicalarms.common.tile.TileSplitter;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.util.function.Supplier;


public class TileSplitterRender extends FastTESR<TileSplitter> {
    InstanceRender ir = InstanceRender.INSTANCE;
    TileSplitter renderingTE;

    float[] mtx = new float[16];

    Matrix4f itemBeltMtx = new Matrix4f();
    Matrix4f splitterBseMtx = new Matrix4f();

    private final Matrix4f tempModelMatrix = new Matrix4f();
    Matrix4f translationMatrix = new Matrix4f();
    byte s;
    byte b;
    Quaternion rot = Quaternion.createIdentity();
    Matrix4fStack matrix4fStack = new Matrix4fStack(10);

    ModelInstance modelInstance = new ModelInstance(ClientProxy.splitter);

    float partialTicks;

    private static Object2ObjectOpenCustomHashMap<ItemStack, ItemStackRenderToVAO> modelCache = new Object2ObjectOpenCustomHashMap<>(new ItemStackHasher());
    private byte alpha;

    public TileSplitterRender() {
        super();
    }

    void traverseHierarchy(NodeInstance node, TileSplitter tileSplitter) {
        // Process the current node (for example, print its information)
        processNode(node, tileSplitter);

        // Recursively traverse each child node
        for (NodeInstance child : node.getChildren()) {
            matrix4fStack.pushMatrix();
            traverseHierarchy(child, tileSplitter);
            matrix4fStack.popMatrix();
        }
    }

    void processNode(NodeInstance node, TileSplitter tileSplitter) {
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

    void setRenderingTE(TileSplitter tileSplitter) {
         renderingTE = tileSplitter;
    }

    public Supplier<TileSplitter> getRenderingTE() {
        return () -> renderingTE;
    }

    void renderBase(TileSplitter tileSplitter) {
        setRenderingTE(tileSplitter);
        if (modelInstance.getRoot() == null) {
            modelInstance.init();
        }

        NodeInstance ni = modelInstance.getRoot();

        matrix4fStack.pushMatrix();
        matrix4fStack.mul(translationMatrix);
        matrix4fStack.mul(splitterBseMtx);
        traverseHierarchy(ni, tileSplitter);
        matrix4fStack.popMatrix();
        setRenderingTE(null);
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
    public void renderTileEntityFast(TileSplitter tileSplitter, double x, double y, double z, float partialTicks, int destroyStage, float alpha, BufferBuilder buffer) {

        Chunk c = tileSplitter.getWorld().getChunk(tileSplitter.getPos());
        s = (byte) c.getLightFor(EnumSkyBlock.SKY, tileSplitter.getPos());
        b = (byte) c.getLightFor(EnumSkyBlock.BLOCK, tileSplitter.getPos());
        this.partialTicks = partialTicks;
        this.alpha = (byte) (alpha * 10);

        translationMatrix.setIdentity();
        rot.setIndentity();

        translate(translationMatrix, (float) x +1, (float) y, (float) z +0.5F );
        matrix4fStack.pushMatrix();
        splitterBseMtx.setIdentity();

        EnumFacing facing = tileSplitter.getFront();
        float xOff = 0;
        float zOff = 0;
        float yOff = 0;


        //model origin
        Vector3f p = new Vector3f(-0.5F, 0f, 0F);
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


        translate(splitterBseMtx, p);
        Quaternion.rotateMatrix(splitterBseMtx, rot);
        translate(splitterBseMtx, ap);
        matrix4fStack.popMatrix();

        renderBase(tileSplitter);
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