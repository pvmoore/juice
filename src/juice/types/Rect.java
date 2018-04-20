package juice.types;

import org.joml.Vector4f;

final public class Rect <T> {
    public T x,y,w,h;

    public Rect(T x, T y, T w, T h) {
        this.x = x; this.y = y; this.w = w; this.h = h;
    }
    public Int2 intDimension() {
        return new Int2((int)w, (int)h);
    }
    public Float2 floatDimension() {
        return new Float2((float)w, (float)h);
    }
    public Vector4f toVector4f() {
        return new Vector4f((float)w,(float)h, (float)w, (float)h);
    }

    @Override public String toString() {
        return "("+x+", "+y+", "+w+", "+h+")";
    }
}
