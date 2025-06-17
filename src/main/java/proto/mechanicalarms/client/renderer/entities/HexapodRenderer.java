package proto.mechanicalarms.client.renderer.entities;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import proto.mechanicalarms.client.renderer.InstanceRender;
import proto.mechanicalarms.client.renderer.instances.MeshInstance;
import proto.mechanicalarms.client.renderer.instances.ModelInstance;
import proto.mechanicalarms.client.renderer.instances.NodeInstance;
import proto.mechanicalarms.client.renderer.util.ItemStackHasher;
import proto.mechanicalarms.client.renderer.util.ItemStackRenderToVAO;
import proto.mechanicalarms.client.renderer.util.Matrix4fStack;
import proto.mechanicalarms.client.renderer.util.Quaternion;
import proto.mechanicalarms.common.entities.EntityHexapod;
import proto.mechanicalarms.common.proxy.ClientProxy;
import proto.mechanicalarms.common.tile.TileArmBasic;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.function.Supplier;


public class HexapodRenderer{
    InstanceRender ir = InstanceRender.INSTANCE;
    public static HexapodRenderer INSTANCE = new HexapodRenderer();
    EntityHexapod entityHexapod;

    float[] mtx = new float[16];

    Matrix4f translationMatrix = new Matrix4f();
    byte s;
    byte b;
    Quaternion rot = Quaternion.createIdentity();
    Vector3f translation = new Vector3f();
    Matrix4fStack matrix4fStack = new Matrix4fStack(10);

    ModelInstance modelInstance = new ModelInstance(ClientProxy.hexapod);

    float partialTicks;

    private static Object2ObjectOpenCustomHashMap<ItemStack, ItemStackRenderToVAO> modelCache = new Object2ObjectOpenCustomHashMap<>(new ItemStackHasher());
    private byte alpha;

    public HexapodRenderer() {
        super();
    }

    void traverseHierarchy(NodeInstance node) {
        // Process the current node (for example, print its information)
        processNode(node);

        // Recursively traverse each child node
        for (NodeInstance child : node.getChildren()) {
            matrix4fStack.pushMatrix();
            traverseHierarchy(child);
            matrix4fStack.popMatrix();
        }
    }

    void processNode(NodeInstance node) {
        for (MeshInstance m : node.getMeshes()) {
            ir.schedule(m); // Schedule the mesh for rendering

            boolean tto = false;
            // Reset the rotation matrix
            if (m.hasRotationFunction()) {
                rot.setIndentity();
                m.applyRotation(rot);
                translate(matrix4fStack, m.meshOrigin[0], m.meshOrigin[1], m.meshOrigin[2]);
                tto = true;
                Quaternion.rotateMatrix(matrix4fStack, rot);
            }
            if (m.hasTranslationVector()) {
                m.applyTranslation(translation);
                translate(matrix4fStack, translation.x, translation.y, translation.z);
            }

            if (!tto) {

                translate(matrix4fStack, m.meshOrigin[0], m.meshOrigin[1], m.meshOrigin[2]);
            }

            matrix4ftofloatarray(matrix4fStack, mtx);

            // Buffer matrix and lighting data
            ir.bufferModelMatrixData(mtx);
            ir.bufferLight(s, b, alpha);

            if (m.hasAttachedMesh()) {
                m.getAttachedMesh().run();
            }
        }
    }

    void setEntityHexapod(EntityHexapod tileArmBasic) {
         entityHexapod = tileArmBasic;
    }

    public Supplier<EntityHexapod> getEntityHexapod() {
        return () -> entityHexapod;
    }

    void renderBase() {
        if (modelInstance.getRoot() == null) {
            modelInstance.setMeshTranslationFunction("Body", (vector3f -> {
                vector3f.set(getEntityHexapod().get().getTranslation());
                return vector3f;
            }));
            modelInstance.setMeshRotationFunction(
                    "FrontRightLegBase", (quaternion) -> {
                        quaternion.multiply(getEntityHexapod().get().getR1());
                        return quaternion;
                    });
//            modelInstance.setMeshRotationFunction(
//                    "FrontRightLegMid", (quaternion) -> quaternion.rotateZ(2* -(getEntityHexapod().get().getR1())));
//            modelInstance.setMeshRotationFunction(
//                    "FrontRightLegTip", (quaternion) -> quaternion.rotateZ((getEntityHexapod().get().getR1())));
//            modelInstance.setMeshRotationFunction(
//                    "FirstArm", (quaternion) -> quaternion.rotateX(lerp(getEntityHexapod().get().getAnimationRotation(0)[0],
//                            getEntityHexapod().get().getRotation(0)[0], partialTicks)));
//            modelInstance.setMeshRotationFunction(
//                    "SecondArm", (quaternion) -> quaternion.rotateX(lerp(getEntityHexapod().get().getAnimationRotation(1)[0],
//                            getEntityHexapod().get().getRotation(1)[0], partialTicks)));
//            modelInstance.attachModel("SecondArm", () -> renderHoldingItem(getEntityHexapod().get()));
            modelInstance.init();
        }

        NodeInstance ni = modelInstance.getRoot();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul(translationMatrix);
        translation.set(0,0,0);
        traverseHierarchy(ni);
        matrix4fStack.popMatrix();
    }

    void renderHoldingItem(TileArmBasic tileArmBasic) {
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
        matrix4fStack.pushMatrix();

        rot.setIndentity();

        scale(matrix4fStack, 0.375f, 0.375f, 0.375f);
        rot.rotateX((float) (Math.PI));
        translate(matrix4fStack, -0.5f, 3f, 0.5f);

        Quaternion.rotateMatrix(matrix4fStack, rot);

        matrix4ftofloatarray(matrix4fStack, mtx);
        matrix4fStack.popMatrix();
        ir.bufferModelMatrixData(mtx);
        ir.bufferLight(s, b, alpha);
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

    public void renderTileEntityFast(EntityHexapod hexapod, double x, double y, double z, float partialTicks, int destroyStage, float alpha, BufferBuilder buffer) {

        Chunk c = hexapod.world.getChunk(hexapod.getPosition());
        s = (byte) c.getLightFor(EnumSkyBlock.SKY, hexapod.getPosition());
        b = (byte) c.getLightFor(EnumSkyBlock.BLOCK, hexapod.getPosition());
        this.partialTicks = partialTicks;
        this.alpha = (byte) Math.clamp(alpha * 10, 0, 10);

        translationMatrix.setIdentity();
        translate(translationMatrix, (float) x, (float) y, (float) z);
        setEntityHexapod(hexapod);
        renderBase();
    }

    public void translate(Matrix4f mat, float x, float y, float z) {
        mat.m03 += mat.m00 * x + mat.m01 * y + mat.m02 * z;
        mat.m13 += mat.m10 * x + mat.m11 * y + mat.m12 * z;
        mat.m23 += mat.m20 * x + mat.m21 * y + mat.m22 * z;
        mat.m33 += mat.m30 * x + mat.m31 * y + mat.m32 * z;
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
}