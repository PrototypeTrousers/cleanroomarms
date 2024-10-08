package proto.mechanicalarms.client.mixin.renderer;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import proto.mechanicalarms.client.renderer.ProtoTesselator;

@Mixin(value = GlStateManager.class, remap = true)
public class GlStateManagerMixin {
    @Inject(method = "translate(FFF)V", at = @At("HEAD"))
    private static void translateProtoTesselator(float x, float y, float z, CallbackInfo ci) {
        if (Tessellator.INSTANCE instanceof ProtoTesselator protoTesselator) {
            protoTesselator.translate(x, y, z);
        }
    }

    @Inject(method = "rotate(FFFF)V", at = @At("HEAD"))
    private static void rotateProtoTesselator(float angle, float x, float y, float z, CallbackInfo ci) {
        if (Tessellator.INSTANCE instanceof ProtoTesselator protoTesselator) {
            protoTesselator.rotate(angle, x, y, z);
        }
    }

    @Inject(method = "pushMatrix", at = @At("HEAD"))
    private static void pushMatrixProtoTesselator(CallbackInfo ci) {
        if (Tessellator.INSTANCE instanceof ProtoTesselator protoTesselator) {
            protoTesselator.pushMatrix();
        }
    }

    @Inject(method = "popMatrix", at = @At("HEAD"))
    private static void popMatrixProtoTesselator(CallbackInfo ci) {
        if (Tessellator.INSTANCE instanceof ProtoTesselator protoTesselator) {
            protoTesselator.popMatrix();
        }
    }

    @Inject(method = "matrixMode", at = @At("HEAD"))
    private static void matrixModeProtoTesselator(int mode, CallbackInfo ci) {
        if (Tessellator.INSTANCE instanceof ProtoTesselator protoTesselator) {
            protoTesselator.matrixMode(mode);
        }
    }

    @Inject(method = "scale(FFF)V", at = @At("HEAD"))
    private static void scaleMatrixModeProtoTesselator(float x, float y, float z, CallbackInfo ci) {
        if (Tessellator.INSTANCE instanceof ProtoTesselator protoTesselator) {
            protoTesselator.scale(x,y,z);
        }
    }

    @Inject(method = "glNewList", at = @At("HEAD"))
    private static void glNewListProtoTesselator(int p_187423_0_, int p_187423_1_, CallbackInfo ci) {
        if (Tessellator.INSTANCE instanceof ProtoTesselator protoTesselator) {
            protoTesselator.glNewList();
        }
    }

    @Inject(method = "callList", at = @At("HEAD"))
    private static void glCallListProtoTesselator(int p_179148_0_, CallbackInfo ci) {
        if (Tessellator.INSTANCE instanceof ProtoTesselator protoTesselator) {
            protoTesselator.callList();
        }
    }

    @Inject(method = "bindTexture", at = @At("HEAD"))
    private static void bindTextureProtoTesselator(int textureId, CallbackInfo ci) {
        if (Tessellator.INSTANCE instanceof ProtoTesselator protoTesselator) {
            protoTesselator.bindTexture(textureId);
        }
    }

}
