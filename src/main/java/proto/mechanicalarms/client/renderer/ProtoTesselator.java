package proto.mechanicalarms.client.renderer;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

public class ProtoTesselator extends Tessellator {
    FloatBuffer pos;
    FloatBuffer tex;
    FloatBuffer color;
    FloatBuffer norm;

    int tvx;
    public ProtoTesselator(int size, FloatBuffer pos, FloatBuffer tex, FloatBuffer color, FloatBuffer norm) {
        super(size);
        this.pos =pos;
        this.tex = tex;
        this.color = color;
        this.norm = norm;
    }

    @Override
    public void draw() {
        buffer.finishDrawing();
        int v = buffer.getVertexCount();
        tvx += v;
        if (v > 0) {
            VertexFormat vertexFormat = buffer.getVertexFormat();
            int vertexSize = vertexFormat.getSize();
            ByteBuffer byteBuffer = buffer.getByteBuffer();
            List<VertexFormatElement> elements = vertexFormat.getElements();

            // Buffers for storing separate vertex data

            for (int i = 0; i < elements.size(); ++i) {
                VertexFormatElement element = elements.get(i);
                VertexFormatElement.EnumUsage usage = element.getUsage();

                // Set the buffer position for this element
                byteBuffer.position(vertexFormat.getOffset(i));

                for (int j = 0; j < buffer.getVertexCount(); ++j) {
                    int vertexOffset = j * vertexSize;
                    byteBuffer.position(vertexOffset + vertexFormat.getOffset(i));

                    // Extract based on the usage type
                    switch (usage) {
                        case POSITION:
                            pos.put(byteBuffer.getFloat());
                            pos.put(byteBuffer.getFloat());
                            pos.put(byteBuffer.getFloat());
                            break;
                        case UV:
                            tex.put(byteBuffer.getFloat());
                            tex.put(byteBuffer.getFloat());
                            break;
                        case COLOR:
                            int col = byteBuffer.getInt();
                            float r = ((col & 0xFF0000) >> 16) / 255F;
                            float g = ((col & 0xFF00) >> 8) / 255F;
                            float b = (col & 0xFF) / 255F;
                            float a = ((col & 0xFF000000) >> 24) / 255F;

                            color.put(r);
                            color.put(g);
                            color.put(b);
                            color.put(a);
                            break;
                        case NORMAL:
                            int packedNormal = byteBuffer.getInt();
                            norm.put(((packedNormal) & 255) / 127.0F);
                            norm.put(((packedNormal >> 8) & 255) / 127.0F);
                            norm.put(((packedNormal >> 16) & 255) / 127.0F);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        buffer.reset();
    }

    public int getTvx() {
        return tvx;
    }
}
