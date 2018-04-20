package juice.types;

final public class Float2 {
    private float x;
    private float y;

    public static Float2 ZERO = new Float2(0, 0);

    public Float2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() { return x; }
    public float getY() { return y; }

    public Float2 add(float x, float y) {
        return new Float2(this.x + x, this.y + y);
    }
    public Float2 add(Float2 p) {
        return new Float2(x+p.x, y+p.y);
    }
    public Float2 sub(Float2 p) {
        return new Float2(x-p.x, y-p.y);
    }

    @Override public int hashCode() {
        return Float.hashCode(x) ^ Float.hashCode(y);
    }
    @Override public boolean equals(Object obj) {
        Float2 o = (Float2)obj;
        return x==o.x && y==o.y;
    }
    @Override public String toString() {
        return "("+x+", "+y+")";
    }
}
