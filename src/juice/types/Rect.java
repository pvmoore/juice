package juice.types;

import org.joml.Vector4f;

final public class Rect <T> {
    public T x,y,w,h;

    public static Rect<Integer> of(Int2 pos, Int2 size) {
        return new Rect<>(pos.getX(), pos.getY(), size.getX(), size.getY());
    }
    public static Rect<Float> of(Float2 pos, Float2 size) {
        return new Rect<>(pos.getX(), pos.getY(), size.getX(), size.getY());
    }

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

    public boolean contains(Int2 p) {
        return p.getX() >= (int)x &&
               p.getY() >= (int)y &&
               p.getX() < (int)x+(int)w &&
               p.getY() < (int)y+(int)h;
    }

    @Override public String toString() {
        return "("+x+", "+y+", "+w+", "+h+")";
    }
}
