package proto.mechanicalarms.common.entities.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.jetbrains.annotations.Nullable;
import proto.mechanicalarms.client.model.ModelHexapod;
import proto.mechanicalarms.client.renderer.entities.HexapodRenderer;
import proto.mechanicalarms.common.entities.EntityHexapod;

public class RenderEntityHexapod extends RenderLiving<EntityHexapod> {
    public static final Factory FACTORY = new Factory();

    public RenderEntityHexapod(RenderManager rendermanagerIn) {
        // We use the vanilla zombie model here and we simply
        // retexture it. Of course you can make your own model
        super(rendermanagerIn, new ModelHexapod(), 0.5F);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityHexapod entity) {
        return null;
    }

    @Override
    public void doRender(EntityHexapod entity, double x, double y, double z, float entityYaw, float partialTicks) {
        HexapodRenderer.INSTANCE.renderTileEntityFast(entity, x, y, z, partialTicks, -1, 1, Tessellator.getInstance().getBuffer());
    }

    public static class Factory implements IRenderFactory<EntityHexapod> {

        @Override
        public Render<? super EntityHexapod> createRenderFor(RenderManager manager) {
            return new RenderEntityHexapod(manager);
        }
    }
}
