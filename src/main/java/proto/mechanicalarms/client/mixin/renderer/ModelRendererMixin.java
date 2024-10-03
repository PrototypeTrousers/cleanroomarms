package proto.mechanicalarms.client.mixin.renderer;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import proto.mechanicalarms.client.renderer.ProtoTesselator;

@Mixin(ModelRenderer.class)
public class ModelRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    void injectRender(float scale, CallbackInfo ci){
        if (Tessellator.INSTANCE instanceof ProtoTesselator protoTesselator) {
            protoTesselator.setPostScale(scale);
            protoTesselator.setModelRender((ModelRenderer) (Object)this);
        }
    }
}
