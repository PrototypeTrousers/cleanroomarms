package proto.mechanicalarms.client.renderer;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

public class ProtoTesselator extends Tessellator {
    FloatBuffer pos;
    FloatBuffer tex;
    FloatBuffer color;
    FloatBuffer norm;
    float xOff;
    float yOff;
    float zOff;

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
        boolean hasColor = false;
        buffer.finishDrawing();
        int v = buffer.getVertexCount();
        tvx += v / 4 * 6 ;
        if (v > 0) {
            VertexFormat vertexFormat = buffer.getVertexFormat();
            int vertexSize = vertexFormat.getSize();
            ByteBuffer byteBuffer = buffer.getByteBuffer();
            List<VertexFormatElement> elements = vertexFormat.getElements();

            // Buffers for storing separate vertex data

            for (int i = 0; i < elements.size(); i++) {
                VertexFormatElement element = elements.get(i);
                VertexFormatElement.EnumUsage usage = element.getUsage();

                // Process quads in groups of 4 vertices
                for (int j = 0; j < buffer.getVertexCount(); j += 4) {
                    int[] quadIndices = {0, 1, 2, 3};

                    // Create two triangles (v0, v1, v2) and (v2, v3, v0)
                    int[][] triangleIndices = {
                            {quadIndices[0], quadIndices[1], quadIndices[2]}, // First triangle
                            {quadIndices[2], quadIndices[3], quadIndices[0]}  // Second triangle
                    };

                    for (int[] triangle : triangleIndices) {
                        for (int vertexIndex : triangle) {
                            int vertexOffset = (j + vertexIndex) * vertexSize;
                            byteBuffer.position(vertexOffset + vertexFormat.getOffset(i));

                            // Extract based on the usage type
                            switch (usage) {
                                case POSITION:
                                    pos.put(byteBuffer.getFloat() + xOff);
                                    pos.put(byteBuffer.getFloat() + yOff);
                                    pos.put(byteBuffer.getFloat() + zOff);
                                    break;
                                case UV:
                                    tex.put(byteBuffer.getFloat());
                                    tex.put(byteBuffer.getFloat());
                                    break;
                                case COLOR:
                                    hasColor = true;
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
            }
        }
        buffer.reset();
        xOff = yOff = zOff = 0;
        if (!hasColor) {
            float[] fullcolor = new float[v * 6];
            Arrays.fill(fullcolor,1);
            color.put(fullcolor);
        }
    }

    public int getTvx() {
        return tvx;
    }

    public void translate(float x, float y, float z) {
        this.xOff += x;
        this.yOff += y;
        this.zOff += z;
    }
}
