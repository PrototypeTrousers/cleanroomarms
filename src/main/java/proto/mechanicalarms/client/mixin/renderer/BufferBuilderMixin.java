package proto.mechanicalarms.client.mixin.renderer;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import proto.mechanicalarms.client.renderer.ProtoTesselator;

@Mixin(value = BufferBuilder.class, remap = true)
public class BufferBuilderMixin {

    @Unique
    private static boolean cleanroomarms$inited;

    @Inject(method = "begin", at = @At("HEAD"), cancellable = true)
    void keepGoing(int p_181668_1_, VertexFormat p_181668_2_, CallbackInfo ci) {
        if (Tessellator.INSTANCE instanceof ProtoTesselator pt) {
            if ( pt.isCompilingGlList && this.cleanroomarms$inited) {
                ci.cancel();
                return;
            }
            this.cleanroomarms$inited = true;
        }
    }

    @Inject(method = "finishDrawing", at = @At("HEAD"))
    void clear(CallbackInfo ci) {
        if (Tessellator.INSTANCE instanceof ProtoTesselator) {
            this.cleanroomarms$inited = false;
        }
    }
}

