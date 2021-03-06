package juice.types;

import org.joml.Vector4f;

import java.util.Arrays;

final public class RGBA {
    public static final RGBA BLACK  = new RGBA(0,0,0,1);
    public static final RGBA WHITE  = new RGBA(1,1,1,1);
    public static final RGBA RED    = new RGBA(1,0,0,1);
    public static final RGBA GREEN  = new RGBA(0,1,0,1);
    public static final RGBA BLUE   = new RGBA(0,0,1,1);
    public static final RGBA YELLOW = new RGBA(1,1,0,1);
    public float r,g,b,a;

    public RGBA(float r, float g, float b, float a) {
        this.r = r; this.g = g; this.b = b; this.a = a;
    }
    public RGBA(float r, float g, float b) {
        this.r = r; this.g = g; this.b = b; this.a = 1;
    }
    public RGBA(int rgb) {
        this(((rgb>>16)&0xff)/255f, ((rgb>>8)&0xff)/255f, (rgb&0xff)/255f);
    }
    public RGBA gamma(float f) {
        return new RGBA(r*f, g*f, b*f, a);
    }
    public RGBA blend(RGBA o) {
        return new RGBA((r+o.r)/2, (g+o.g)/2, (b+o.b)/2, (a+o.a)/2);
    }
    public RGBA red(float r) {
        return new RGBA(r,g,b,a);
    }
    public RGBA green(float g) {
        return new RGBA(r,g,b,a);
    }
    public RGBA blue(float b) {
        return new RGBA(r,g,b,a);
    }
    public RGBA alpha(float a) {
        return new RGBA(r,g,b,a);
    }
    public Vector4f toVector4f() {
        return new Vector4f(r,g,b,a);
    }
    @Override public int hashCode() {
        return Arrays.hashCode(new float[]{r,g,b,a});
    }
    @Override public boolean equals(Object obj) {
        RGBA o = (RGBA)obj;
        return r==o.r && g==o.g && b==o.b && a==o.a;
    }
    @Override public String toString() {
        return String.format("[%.2f, %.2f, %.2f, %.2f]", r,g,b,a);
    }
}
