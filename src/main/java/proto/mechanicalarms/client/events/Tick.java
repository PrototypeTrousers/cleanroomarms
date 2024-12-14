package proto.mechanicalarms.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl3.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl3.opengl.GL15.*;
import static org.lwjgl3.opengl.GL21.GL_PIXEL_UNPACK_BUFFER;

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
    }
}