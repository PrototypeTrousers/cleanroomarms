package proto.mechanicalarms.client.renderer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import proto.mechanicalarms.client.renderer.util.ItemStackHasher;
import proto.mechanicalarms.client.renderer.util.ItemStackRenderToVAO;
import proto.mechanicalarms.client.renderer.util.Quaternion;
import proto.mechanicalarms.common.proxy.ClientProxy;
import proto.mechanicalarms.common.tile.TileArmBasic;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.model.animation.FastTESR;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;


public class TileArmRenderer extends FastTESR<TileArmBasic> {
    InstanceRender ir = InstanceRender.INSTANCE;

    float[] mtx = new float[16];
    float[] mtx2 = new float[16];

    Matrix4f baseMotorMatrix = new Matrix4f();
    Matrix4f firstArmMatrix = new Matrix4f();
    Matrix4f secondArmMatrix = new Matrix4f();

    Matrix4f handMatrix = new Matrix4f();
    Matrix4f itemArmMatrix = new Matrix4f();



    Vao base;
    private final Matrix4f tempModelMatrix = new Matrix4f();
    Matrix4f transformMatrix = new Matrix4f();
    Matrix4f translationMatrix = new Matrix4f();
    byte s;
    byte b;
    Quaternion rot = Quaternion.createIdentity();

    float partialTicks;

    private Vao baseMotor;
    private Vao firstArm;
    private Vao secondArm;
    private Vao hand;


    private static Object2ObjectOpenCustomHashMap<ItemStack, ItemStackRenderToVAO> modelCache = new Object2ObjectOpenCustomHashMap<>(new ItemStackHasher());

    public TileArmRenderer() {
        super();
    }

    void renderBase() {
        if (base == null) {
            base = new Vao(ClientProxy.base);
        }
        ir.schedule(base);
        matrix4ftofloatarray(translationMatrix, mtx);
        ir.bufferModelMatrixData(mtx);
        ir.bufferLight(s, b);
    }

    void renderBaseMotor(TileArmBasic tileArmBasic, double x, double y, double z) {
        if (baseMotor == null) {
            baseMotor = new Vao(ClientProxy.baseMotor);
        }
        ir.schedule(baseMotor);


        //upload model matrix, light

        float[] firstArmCurrRot = tileArmBasic.getRotation(0);
        float[] firstArmPrevRot = tileArmBasic.getAnimationRotation(0);

        baseMotorMatrix.setIdentity();
        rot.setIndentity();

        baseMotorMatrix.mul(translationMatrix);

        Vector3f p = new Vector3f(0.5f,0,0.6f);
        Vector3f ap = new Vector3f(p);
        ap.negate();

        translate(baseMotorMatrix, p);

        rot.rotateY(lerp(firstArmPrevRot[1], firstArmCurrRot[1], partialTicks));
        Quaternion.rotateMatrix(baseMotorMatrix, rot);
        translate(baseMotorMatrix, ap);


        matrix4ftofloatarray(baseMotorMatrix, mtx);
        ir.bufferModelMatrixData(mtx);
        ir.bufferLight(s, b);
    }
    void renderFirstArm(TileArmBasic tileArmBasic, double x, double y, double z) {
        if (firstArm == null) {
            firstArm = new Vao(ClientProxy.firstArm);
        }
        ir.schedule(firstArm);


        //upload model matrix, light

        float[] firstArmCurrRot = tileArmBasic.getRotation(0);
        float[] firstArmPrevRot = tileArmBasic.getAnimationRotation(0);

        firstArmMatrix.setIdentity();
        rot.setIndentity();

        firstArmMatrix.mul(translationMatrix);

        Vector3f p = new Vector3f(0.5f,1.5f,0.6f);
        Vector3f ap = new Vector3f(p);
        ap.negate();

        translate(firstArmMatrix, p);
        rot.rotateY(lerp(firstArmPrevRot[1], firstArmCurrRot[1], partialTicks));
        rot.rotateX(lerp(firstArmPrevRot[0], firstArmCurrRot[0], partialTicks));


        Quaternion.rotateMatrix(firstArmMatrix, rot);
        translate(firstArmMatrix, ap);


        matrix4ftofloatarray(firstArmMatrix, mtx);
        ir.bufferModelMatrixData(mtx);
        ir.bufferLight(s, b);
    }

    void renderSecondArm(TileArmBasic tileArmBasic, double x, double y, double z) {
        if (secondArm == null) {
            secondArm = new Vao(ClientProxy.secondArm);
        }
        ir.schedule(secondArm);


        //upload model matrix, light

        float[] secondArmCurrRot = tileArmBasic.getRotation(1);
        float[] secondArmPrevRot = tileArmBasic.getAnimationRotation(1);

        secondArmMatrix.setIdentity();
        rot.setIndentity();

        secondArmMatrix.mul(firstArmMatrix);

        Vector3f p = new Vector3f(0.4f,3.8f,0.5f);
        Vector3f ap = new Vector3f(p);
        ap.negate();

        translate(secondArmMatrix, p);
        //rot.rotateY(lerp(secondArmPrevRot[1], secondArmCurrRot[1], partialTicks));
        rot.rotateX(lerp(secondArmPrevRot[0], secondArmCurrRot[0], partialTicks));

        Quaternion.rotateMatrix(secondArmMatrix, rot);
        translate(secondArmMatrix, ap);


        matrix4ftofloatarray(secondArmMatrix, mtx);
        ir.bufferModelMatrixData(mtx);
        ir.bufferLight(s, b);
    }

    void renderHand(TileArmBasic tileArmBasic, double x, double y, double z) {
        if (hand == null) {
            hand = new Vao(ClientProxy.hand);
        }
        ir.schedule(hand);


        //upload model matrix, light

        float[] handCurrRot = tileArmBasic.getRotation(2);
        float[] handPrevRot = tileArmBasic.getAnimationRotation(2);

        handMatrix.setIdentity();
        rot.setIndentity();

        handMatrix.mul(secondArmMatrix);

        Vector3f p = new Vector3f(0.5f,6.1f,0.5f);
        Vector3f ap = new Vector3f(p);
        ap.negate();

        translate(handMatrix, p);
        //rot.rotateY(lerp(handPrevRot[1], handCurrRot[1], partialTicks));
        //rot.rotateX(lerp(handPrevRot[0], handCurrRot[0], partialTicks));

        Quaternion.rotateMatrix(handMatrix, rot);
        translate(handMatrix, ap);


        matrix4ftofloatarray(handMatrix, mtx);
        ir.bufferModelMatrixData(mtx);
        ir.bufferLight(s, b);
    }

    void renderHoldingItem(TileArmBasic tileArmBasic, double x, double y, double z) {
        ItemStack curStack = tileArmBasic.getItemStack();

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

        float[] secondArmCurrRot = tileArmBasic.getRotation(1);
        float[] secondArmPrevRot = tileArmBasic.getAnimationRotation(1);

        itemArmMatrix.setIdentity();
        rot.setIndentity();

        itemArmMatrix.mul(handMatrix);

        Vector3f p = new Vector3f(0.0f,6f,-0.2f);

        translate(itemArmMatrix, p);
        //itemArmMatrix.setScale(0.5f);
        //rot.rotateY(lerp(secondArmPrevRot[1], secondArmCurrRot[1], partialTicks));
        //rot.rotateX(lerp(secondArmPrevRot[0], secondArmCurrRot[0], partialTicks));

        //Quaternion.rotateMatrix(itemArmMatrix, rot);

        matrix4ftofloatarray(itemArmMatrix, mtx);
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
    public void renderTileEntityFast(TileArmBasic tileArmBasic, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {

        Chunk c = tileArmBasic.getWorld().getChunk(tileArmBasic.getPos());
        s = (byte) c.getLightFor(EnumSkyBlock.SKY, tileArmBasic.getPos());
        b = (byte) c.getLightFor(EnumSkyBlock.BLOCK, tileArmBasic.getPos());
        this.partialTicks = partialTicks;

        translationMatrix.setIdentity();
        translate(translationMatrix, (float) x, (float) y, (float) z);

        renderBase();
//        renderBaseMotor(tileArmBasic,x ,y, z);
//        renderFirstArm(tileArmBasic,x ,y, z);
//        renderSecondArm(tileArmBasic,x ,y, z);
//        renderHand(tileArmBasic,x ,y, z);
//        renderHoldingItem(tileArmBasic, x, y, z);
        //renderPart(tileArmBasic, x, y, z, partialTicks, transformMatrix);
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