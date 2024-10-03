package proto.mechanicalarms.client.mixin.renderer;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import proto.mechanicalarms.client.renderer.ProtoTesselator;

@Mixin(GlStateManager.class)
public class GlStateManagerMixin {
    @Inject(method = "translate(FFF)V", at = @At("HEAD"))
    private static void translateProtoTesselator(float x, float y, float z, CallbackInfo ci) {
        if (Tessellator.INSTANCE instanceof ProtoTesselator protoTesselator) {
            protoTesselator.translate(x, y, z);
        }
    }
}
