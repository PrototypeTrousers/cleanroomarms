package proto.mechanicalarms.client.jgltf;

import de.javagl.jgltf.model.TextureModel;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

public class EmbeddedTexture extends AbstractTexture {
    protected final TextureModel textureModel;

    public EmbeddedTexture(TextureModel textureModel) {
        this.textureModel = textureModel;
    }

    @Override
    public void loadTexture(IResourceManager resourceManager) throws IOException {
        this.deleteGlTexture();
        BufferedImage bufferedimage = TextureUtil.readBufferedImage(new ByteBufferInputStream(textureModel.getImageModel().getImageData()));
        TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), bufferedimage, textureModel.getMagFilter() != GL11.GL_NEAREST, false);
    }

    static class ByteBufferInputStream extends InputStream {
        private final ByteBuffer byteBuffer;

        ByteBufferInputStream(ByteBuffer byteBuffer) {
            this.byteBuffer = (ByteBuffer) Objects.requireNonNull(byteBuffer, "The byteBuffer may not be null");
        }

        public int read() throws IOException {
            return !this.byteBuffer.hasRemaining() ? -1 : this.byteBuffer.get() & 255;
        }

        public int read(byte[] bytes, int off, int len) throws IOException {
            if (!this.byteBuffer.hasRemaining()) {
                return -1;
            } else {
                int readLength = Math.min(len, this.byteBuffer.remaining());
                this.byteBuffer.get(bytes, off, readLength);
                return readLength;
            }
        }
    }
}
