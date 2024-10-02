package proto.mechanicalarms.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl3.opengl.GL11;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Tick {
    public static Tick INSTANCE = new Tick();

    private int width;
    private int height;




    BufferedImage originalTint;
    public static int tintTexGL;

    @SubscribeEvent
    public void onTick(final TickEvent ev) {
        if (Minecraft.getMinecraft().world == null || ev.side == Side.SERVER) {
            return;
        }

        if (ev.phase == TickEvent.Phase.END) {
            if (tintTexGL == 0) {
                tintTexGL = GL11.glGenTextures();
                IResource iresource = null;
                try {
                    iresource = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("textures/misc/enchanted_item_glint.png"));
                    originalTint = TextureUtil.readBufferedImage(iresource.getInputStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                width = originalTint.getWidth();
                height = originalTint.getHeight();
            }
            float f = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
            BufferedImage translatedTint = translateImage(originalTint, f * 10, 0);
            BufferedImage rot = rotateImage(translatedTint, -50);


            TextureUtil.uploadTextureImageAllocate(tintTexGL, rot, false,false);
        }
    }

    private static ByteBuffer convertImageData(BufferedImage image) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        ByteBuffer buffer = GLAllocation.createDirectByteBuffer(image.getWidth() * image.getHeight() * 4).order(ByteOrder.nativeOrder());

        // Convert ARGB to RGBA
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));  // Red
                buffer.put((byte) ((pixel >> 8) & 0xFF));   // Green
                buffer.put((byte) (pixel & 0xFF));          // Blue
                buffer.put((byte) ((pixel >> 24) & 0xFF));  // Alpha
            }
        }
        buffer.flip(); // Make buffer ready for reading

        return buffer;
    }

    private static BufferedImage rotateImage(BufferedImage buffImage, double angle) {
        double radian = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(radian));
        double cos = Math.abs(Math.cos(radian));

        int width = buffImage.getWidth();
        int height = buffImage.getHeight();

        int nWidth = (int) Math.floor((double) width * cos + (double) height * sin);
        int nHeight = (int) Math.floor((double) height * cos + (double) width * sin);

        BufferedImage rotatedImage = new BufferedImage(
                nWidth, nHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = rotatedImage.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        graphics.translate((nWidth - width) / 2, (nHeight - height) / 2);
        // rotation around the center point
        graphics.rotate(radian, (double) (width / 2), (double) (height / 2));
        graphics.drawImage(buffImage, 0, 0, null);
        graphics.dispose();

        return rotatedImage;
    }

    public static BufferedImage translateImage(BufferedImage image, float translateX, float translateY) {
        // Create a new image with the same width and height as the original image
        BufferedImage translatedImage = new BufferedImage(
                image.getWidth(), image.getHeight(), image.getType());

        // Get the Graphics2D object from the new image
        Graphics2D g2d = translatedImage.createGraphics();

        // Apply the translation using AffineTransform
        AffineTransform transform = new AffineTransform();
        transform.translate(translateX, translateY);
        g2d.setTransform(transform);

        // Draw the original image onto the new image with the translation
        g2d.drawImage(image, 0, 0, null);

        // Dispose of the graphics object
        g2d.dispose();

        // Return the translated image
        return translatedImage;
    }
}
