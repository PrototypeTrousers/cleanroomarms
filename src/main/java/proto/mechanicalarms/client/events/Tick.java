package proto.mechanicalarms.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;
import proto.mechanicalarms.client.renderer.InstanceRender;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class Tick {
    public static Tick INSTANCE = new Tick();
    BufferedImage originalTint;
    public static int tintTexGL;

    @SubscribeEvent
    public void onTick(final TickEvent.ClientTickEvent ev) {
        if (ev.phase == TickEvent.Phase.END) {
            if (tintTexGL == 0) {
                tintTexGL = GL11.glGenTextures();
                IResource iresource;
                try {
                    iresource = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("textures/misc/enchanted_item_glint.png"));
                    originalTint = TextureUtil.readBufferedImage(iresource.getInputStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                TextureUtil.uploadTextureImageAllocate(tintTexGL, originalTint, true, false);
            }
        }
        InstanceRender.INSTANCE.clear();
    }
}