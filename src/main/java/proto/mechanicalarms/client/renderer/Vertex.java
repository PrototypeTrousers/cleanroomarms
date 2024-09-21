package proto.mechanicalarms.client.renderer;

public class Vertex {

    //Pos, tex, normal, color
    //12, 8, 12
    public static final int BYTES_PER_VERTEX = 32;

    public float x;
    public float y;
    public float z;
    public float u;
    public float v;
    public byte normalX;
    public byte normalY;
    public byte normalZ;

    // mat4 transform
}
