package proto.mechanicalarms.client.renderer;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.lwjgl3.opengl.GL11;
import proto.mechanicalarms.client.renderer.util.Matrix4fStack;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

public class ProtoTesselator extends Tessellator {
    FloatBuffer pos;
    FloatBuffer tex;
    FloatBuffer color;
    FloatBuffer norm;

    public int texGL;

    Matrix4fStack modelViewMatrixStack = new Matrix4fStack(10);
    int matrixMode;

    ModelRenderer mr;

    public boolean isCompilingGlList;

    int tvx;
    private float mrScale;

    public ProtoTesselator(int size, FloatBuffer pos, FloatBuffer tex, FloatBuffer color, FloatBuffer norm) {
        super(size);
        this.pos = pos;
        this.tex = tex;
        this.color = color;
        this.norm = norm;
        this.matrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        modelViewMatrixStack.pushMatrix();
    }

    @Override
    public void draw() {
        if (isCompilingGlList) {
            return;
        }
        boolean hasColor = false;
        buffer.finishDrawing();
        int v = buffer.getVertexCount();
        tvx += v / 4 * 6;
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
                                    Point3f posVec = new Point3f(byteBuffer.getFloat(),
                                            byteBuffer.getFloat(),
                                            byteBuffer.getFloat());

                                    modelViewMatrixStack.transform(posVec);


                                    pos.put(posVec.x);
                                    pos.put(posVec.y);
                                    pos.put(posVec.z);
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
        if (!hasColor) {
            float[] fullcolor = new float[v * 6];
            Arrays.fill(fullcolor, 1);
            color.put(fullcolor);
        }
    }

    public int getTvx() {
        return tvx;
    }

    public void translate(float x, float y, float z) {
        if (matrixMode == GL11.GL_MODELVIEW) {
            Matrix4f loc = new Matrix4f();
            loc.setIdentity();
            loc.setTranslation(new Vector3f(x, y, z));
            modelViewMatrixStack.mul(loc);
        }
    }

    public void rotate(float angle, float x, float y, float z) {
        if (matrixMode == GL11.GL_MODELVIEW) {
            Vector3f axis = new Vector3f(x, y, z);
            axis.normalize();
            AxisAngle4f rot = new AxisAngle4f(axis, (float) Math.toRadians(angle));

            Matrix4f loc = new Matrix4f();
            loc.setIdentity();
            loc.setRotation(rot);
            modelViewMatrixStack.mul(loc);
        }
    }

    public void pushMatrix() {
        if (matrixMode == GL11.GL_MODELVIEW) {
            modelViewMatrixStack.pushMatrix();
        }
    }

    public void popMatrix() {
        if (matrixMode == GL11.GL_MODELVIEW) {
            modelViewMatrixStack.popMatrix();
        }
    }

    public void matrixMode(int mode) {
        this.matrixMode = mode;
    }

    public void scale(float x, float y, float z) {
        if (matrixMode == GL11.GL_MODELVIEW) {
            Matrix4f loc = new Matrix4f();
            loc.setIdentity();
            loc.m00 = x;
            loc.m11 = y;
            loc.m22 = z;
            modelViewMatrixStack.mul(loc);
        }
    }

    public void glNewList() {
        this.isCompilingGlList = true;
    }

    public void callList() {
        if (isCompilingGlList) {
            this.isCompilingGlList = false;
            draw();
        }
    }

    public void bindTexture(int textureId) {
        if (textureId != 0) {
            this.texGL = textureId;
        }
    }
}
