package juice.types;
/**
 * Immutable int vector.
 */
final public class Int2 {
    private int x;
    private int y;

    public static Int2 ZERO = new Int2(0, 0);

    public Int2(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public Int2(Float2 f) {
        this.x = (int)f.getX();
        this.y = (int)f.getY();
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public Int2 add(int v) {
        return new Int2(x + v, y + v);
    }
    public Int2 add(int x, int y) {
        return new Int2(this.x + x, this.y + y);
    }
    public Int2 add(Int2 p) {
        return new Int2(x+p.x, y+p.y);
    }

    public Int2 sub(int v) {
        return new Int2(x-v, y-v);
    }
    public Int2 sub(int x, int y) {
        return new Int2(this.x-x, this.y-y);
    }
    public Int2 sub(Int2 p) {
        return new Int2(x-p.x, y-p.y);
    }

    public Int2 mul(int p) {
        return new Int2(x*p, y*p);
    }
    public Int2 mul(int x, int y) {
        return new Int2(this.x*x, this.y*y);
    }
    public Int2 mul(Int2 p) {
        return new Int2(x*p.x, y*p.y);
    }

    public Int2 div(int p) {
        return new Int2(x/p, y/p);
    }
    public Int2 div(int x, int y) {
        return new Int2(this.x/x, this.y/y);
    }
    public Int2 div(Int2 p) {
        return new Int2(x/p.x, y/p.y);
    }

    @Override public int hashCode() {
        return Integer.hashCode(x) ^ Integer.hashCode(y);
    }
    @Override public boolean equals(Object obj) {
        Int2 o = (Int2)obj;
        return x==o.x && y==o.y;
    }
    @Override public String toString() {
        return "("+x+", "+y+")";
    }
}
