package proto.mechanicalarms.client.mixin.renderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import proto.mechanicalarms.client.renderer.ProtoTesselator;

@Mixin(ModelRenderer.class)
public class ModelRendererMixin {

    @Shadow public boolean compiled;

    @Inject(method = "compileDisplayList", at = @At("TAIL"))
    void unsetCompiled(float p_78788_1_, CallbackInfo ci){
        if (Tessellator.INSTANCE instanceof ProtoTesselator) {
            this.compiled = false;
        }
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelRenderer;compiled:Z"))
    boolean runGeometry(boolean original){
        if (Tessellator.INSTANCE instanceof ProtoTesselator){
            return false;
        }
        return original;
    }
    @ModifyExpressionValue(method = "renderWithRotation", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelRenderer;compiled:Z"))
    boolean runGeometryWithRotation(boolean original){
        if (Tessellator.INSTANCE instanceof ProtoTesselator){
            return false;
        }
        return original;
    }
    @ModifyExpressionValue(method = "postRender", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelRenderer;compiled:Z"))
    boolean runGeometryPostRender(boolean original){
        if (Tessellator.INSTANCE instanceof ProtoTesselator){
            return false;
        }
        return original;
    }
}
