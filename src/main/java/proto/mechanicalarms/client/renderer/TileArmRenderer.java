package proto.mechanicalarms.client.renderer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.item.ItemStack;
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
import proto.mechanicalarms.common.tile.TileArmBasic;

import javax.vecmath.Matrix4f;
import java.util.function.Supplier;


public class TileArmRenderer extends FastTESR<TileArmBasic> {
    InstanceRender ir = InstanceRender.INSTANCE;
    TileArmBasic renderingTE;

    float[] mtx = new float[16];

    Matrix4f translationMatrix = new Matrix4f();
    byte s;
    byte b;
    Quaternion rot = Quaternion.createIdentity();
    Matrix4fStack matrix4fStack = new Matrix4fStack(10);

    ModelInstance modelInstance = new ModelInstance(ClientProxy.base);

    float partialTicks;

    private static Object2ObjectOpenCustomHashMap<ItemStack, ItemStackRenderToVAO> modelCache = new Object2ObjectOpenCustomHashMap<>(new ItemStackHasher());

    public TileArmRenderer() {
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

            if (m.hasAttachedMesh()) {
                m.getAttachedMesh().run();
            }
        }
    }

    void setRenderingTE(TileArmBasic tileArmBasic) {
         renderingTE = tileArmBasic;
    }

    public Supplier<TileArmBasic> getRenderingTE() {
        return () -> renderingTE;
    }

    void renderBase() {
        if (modelInstance.getRoot() == null) {
            modelInstance.setMeshRotationFunction(
                    "BaseMotor", (quaternion) -> quaternion.rotateY(lerp(getRenderingTE().get().getAnimationRotation(0)[1],
                            getRenderingTE().get().getRotation(0)[1], partialTicks)));
            modelInstance.setMeshRotationFunction(
                    "FirstArm", (quaternion) -> quaternion.rotateX(lerp(getRenderingTE().get().getAnimationRotation(0)[0],
                            getRenderingTE().get().getRotation(0)[0], partialTicks)));
            modelInstance.setMeshRotationFunction(
                    "SecondArm", (quaternion) -> quaternion.rotateX(lerp(getRenderingTE().get().getAnimationRotation(1)[0],
                            getRenderingTE().get().getRotation(1)[0], partialTicks)));
            modelInstance.attachModel("Claw", () -> renderHoldingItem(getRenderingTE().get()));
            modelInstance.init();
        }

        NodeInstance ni = modelInstance.getRoot();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul(translationMatrix);
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


        matrix4fStack.setScale(0.375f);
        rot.rotateX((float) (Math.PI));
        translate(matrix4fStack, 0f, 2f, 0.5f);

        Quaternion.rotateMatrix(matrix4fStack, rot);

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
    public void renderTileEntityFast(TileArmBasic tileArmBasic, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {

        Chunk c = tileArmBasic.getWorld().getChunk(tileArmBasic.getPos());
        s = (byte) c.getLightFor(EnumSkyBlock.SKY, tileArmBasic.getPos());
        b = (byte) c.getLightFor(EnumSkyBlock.BLOCK, tileArmBasic.getPos());
        this.partialTicks = partialTicks;

        translationMatrix.setIdentity();
        translate(translationMatrix, (float) x + 0.5F, (float) y, (float) z + 0.5F);
        setRenderingTE(tileArmBasic);
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