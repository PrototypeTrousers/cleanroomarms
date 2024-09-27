package proto.mechanicalarms.client.renderer.util;

import javax.vecmath.Matrix4f;

public class Quaternion {

    public float x;
    public float y;
    public float z;
    public float w;

    public Quaternion() {

    }

    public Quaternion(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            Quaternion quaternion = (Quaternion) obj;
            return Float.compare(quaternion.x, this.x) == 0
                    && Float.compare(quaternion.y, this.y) == 0
                    && Float.compare(quaternion.z, this.z) == 0
                    && Float.compare(quaternion.w, this.w) == 0;
        } else {
            return false;
        }
    }

    public int hashCode() {
        int i = this.x != 0.0F ? Float.floatToIntBits(this.x) : 0;
        i = 31 * i + (this.y != 0.0F ? Float.floatToIntBits(this.y) : 0);
        i = 31 * i + (this.z != 0.0F ? Float.floatToIntBits(this.z) : 0);
        return 31 * i + (this.w != 0.0F ? Float.floatToIntBits(this.w) : 0);
    }

    public String toString() {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append("Quaternion: ");
        stringbuilder.append(this.x);
        stringbuilder.append(" ");
        stringbuilder.append(this.y);
        stringbuilder.append(" ");
        stringbuilder.append(this.z);
        stringbuilder.append(" ");
        stringbuilder.append(this.w);
        return stringbuilder.toString();
    }

    public static Quaternion createIdentity() {
        return new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
    }

    public static Quaternion createRotate(float radian, float x, float y, float z) {
        radian *= 0.5F;
        float q = (float) Math.sin(radian);
        return new Quaternion(x * q, y * q, z * q, (float) Math.cos(radian));
    }

    public static Quaternion createRotateX(float radian) {
        radian *= 0.5F;
        return new Quaternion((float) Math.sin(radian), 0.0F, 0.0F, (float) Math.cos(radian));
    }

    public static Quaternion createRotateY(float radian) {
        radian *= 0.5F;
        return new Quaternion(0.0F, (float) Math.sin(radian), 0.0F, (float) Math.cos(radian));
    }

    public static Quaternion createRotateZ(float radian) {
        radian *= 0.5F;
        return new Quaternion(0.0F, 0.0F, (float) Math.sin(radian), (float) Math.cos(radian));
    }

    public void setIndentity() {
        this.x = 0.0F;
        this.y = 0.0F;
        this.z = 0.0F;
        this.w = 1.0F;
    }

    public void multiply(Quaternion quaternion) {
        this.multiply(quaternion.x, quaternion.y, quaternion.z, quaternion.w);
    }

    public void multiply(float x, float y, float z, float w) {
        float x0 = this.x;
        float y0 = this.y;
        float z0 = this.z;
        float w0 = this.w;
        this.x =  x0 * w + y0 * z - z0 * y + w0 * x;
        this.y = -x0 * z + y0 * w + z0 * x + w0 * y;
        this.z =  x0 * y - y0 * x + z0 * w + w0 * z;
        this.w = -x0 * x - y0 * y - z0 * z + w0 * w;
    }

    public void rotate(float radian, float x, float y, float z) {
        radian *= 0.5F;
        float q = (float) Math.sin(radian);
        this.multiply(x * q, y * q, z * q, (float) Math.cos(radian));
    }

    public Quaternion rotateX(float radian) {
        radian *= 0.5F;
        float x = (float) Math.sin(radian);
        float w = (float) Math.cos(radian);
        float x0 = this.x;
        float y0 = this.y;
        float z0 = this.z;
        float w0 = this.w;
        this.x =  x0 * w + w0 * x;
        this.y =  y0 * w + z0 * x;
        this.z = -y0 * x + z0 * w;
        this.w = -x0 * x + w0 * w;
        return this;
    }

    public Quaternion rotateY(float radian) {
        radian *= 0.5F;
        float y = (float) Math.sin(radian);
        float w = (float) Math.cos(radian);
        float x0 = this.x;
        float y0 = this.y;
        float z0 = this.z;
        float w0 = this.w;
        this.x =  x0 * w - z0 * y;
        this.y =  y0 * w + w0 * y;
        this.z =  x0 * y + z0 * w;
        this.w = -y0 * y + w0 * w;
        return this;
    }

    public Quaternion rotateZ(float radian) {
        radian *= 0.5F;
        float z = (float) Math.sin(radian);
        float w = (float) Math.cos(radian);
        float x0 = this.x;
        float y0 = this.y;
        float z0 = this.z;
        float w0 = this.w;
        this.x =  x0 * w + y0 * z;
        this.y = -x0 * z + y0 * w;
        this.z =  z0 * w + w0 * z;
        this.w = -z0 * z + w0 * w;
        return this;
    }

    public static Matrix4f createRotateMatrix(Quaternion quaternion) {
        Matrix4f matrix = new Matrix4f();
        float xx = 2.0F * quaternion.x * quaternion.x;
        float yy = 2.0F * quaternion.y * quaternion.y;
        float zz = 2.0F * quaternion.z * quaternion.z;
        float xy = quaternion.x * quaternion.y;
        float yz = quaternion.y * quaternion.z;
        float zx = quaternion.z * quaternion.x;
        float xw = quaternion.x * quaternion.w;
        float yw = quaternion.y * quaternion.w;
        float zw = quaternion.z * quaternion.w;

        matrix.m00 = 1.0F - yy - zz;
        matrix.m11 = 1.0F - zz - xx;
        matrix.m22 = 1.0F - xx - yy;
        matrix.m33 = 1.0F;
        matrix.m10 = 2.0F * (xy + zw);
        matrix.m01 = 2.0F * (xy - zw);
        matrix.m20 = 2.0F * (zx - yw);
        matrix.m02 = 2.0F * (zx + yw);
        matrix.m21 = 2.0F * (yz + xw);
        matrix.m12 = 2.0F * (yz - xw);
        return matrix;
    }

    public static void rotateMatrix(Matrix4f mat, Quaternion quaternion) {
        // setup rotation matrix
        float xx = 2.0F * quaternion.x * quaternion.x;
        float yy = 2.0F * quaternion.y * quaternion.y;
        float zz = 2.0F * quaternion.z * quaternion.z;
        float xy = quaternion.x * quaternion.y;
        float yz = quaternion.y * quaternion.z;
        float zx = quaternion.z * quaternion.x;
        float xw = quaternion.x * quaternion.w;
        float yw = quaternion.y * quaternion.w;
        float zw = quaternion.z * quaternion.w;

        float r00 = 1.0F - yy - zz;
        float r11 = 1.0F - zz - xx;
        float r22 = 1.0F - xx - yy;
        float r10 = 2.0F * (xy + zw);
        float r01 = 2.0F * (xy - zw);
        float r20 = 2.0F * (zx - yw);
        float r02 = 2.0F * (zx + yw);
        float r21 = 2.0F * (yz + xw);
        float r12 = 2.0F * (yz - xw);

        // multiply matrices
        float f00 = mat.m00;
        float f01 = mat.m01;
        float f02 = mat.m02;
        float f10 = mat.m10;
        float f11 = mat.m11;
        float f12 = mat.m12;
        float f20 = mat.m20;
        float f21 = mat.m21;
        float f22 = mat.m22;
        float f30 = mat.m30;
        float f31 = mat.m31;
        float f32 = mat.m32;

        mat.m00 = f00 * r00 + f01 * r10 + f02 * r20;
        mat.m01 = f00 * r01 + f01 * r11 + f02 * r21;
        mat.m02 = f00 * r02 + f01 * r12 + f02 * r22;
        mat.m10 = f10 * r00 + f11 * r10 + f12 * r20;
        mat.m11 = f10 * r01 + f11 * r11 + f12 * r21;
        mat.m12 = f10 * r02 + f11 * r12 + f12 * r22;
        mat.m20 = f20 * r00 + f21 * r10 + f22 * r20;
        mat.m21 = f20 * r01 + f21 * r11 + f22 * r21;
        mat.m22 = f20 * r02 + f21 * r12 + f22 * r22;
        mat.m30 = f30 * r00 + f31 * r10 + f32 * r20;
        mat.m31 = f30 * r01 + f31 * r11 + f32 * r21;
        mat.m32 = f30 * r02 + f31 * r12 + f32 * r22;
    }

    public static Matrix4f createRotateXMatrix(Quaternion quaternion) {
        Matrix4f matrix = new Matrix4f();
        float xx = 2.0F * quaternion.x * quaternion.x;
        float xw = quaternion.x * quaternion.w;

        matrix.m00 = 1.0F;
        matrix.m11 = 1.0F - xx;
        matrix.m22 = 1.0F - xx;
        matrix.m33 = 1.0F;
        matrix.m21 = 2.0F * xw;
        matrix.m12 = 2.0F * -xw;
        return matrix;
    }

    public static Matrix4f createRotateYMatrix(Quaternion quaternion) {
        Matrix4f matrix = new Matrix4f();
        float yy = 2.0F * quaternion.y * quaternion.y;
        float yw = quaternion.y * quaternion.w;

        matrix.m00 = 1.0F - yy;
        matrix.m11 = 1.0F;
        matrix.m22 = 1.0F - yy;
        matrix.m33 = 1.0F;
        matrix.m20 = 2.0F * -yw;
        matrix.m02 = 2.0F * yw;
        return matrix;
    }

    public static Matrix4f createRotateZMatrix(Quaternion quaternion) {
        Matrix4f matrix = new Matrix4f();
        float zz = 2.0F * quaternion.z * quaternion.z;
        float zw = quaternion.z * quaternion.w;

        matrix.m00 = 1.0F - zz;
        matrix.m11 = 1.0F - zz;
        matrix.m22 = 1.0F;
        matrix.m33 = 1.0F;
        matrix.m10 = 2.0F * zw;
        matrix.m01 = 2.0F * -zw;
        return matrix;
    }

}