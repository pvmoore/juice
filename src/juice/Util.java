package juice;

import juice.types.Int2;
import juice.types.RGBA;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;

final public class Util {
    private static final float PI_OVER_180  = (float)(Math.PI / 180.0);
    private static final float _180_OVER_PI = (float)(180.0 / Math.PI);

    public static String toString(Vector2f v) {
        return v.toString(new DecimalFormat("####0.##"));
    }
    public static String toString(Vector3f v) {
        return v.toString(new DecimalFormat("####0.##  "));
    }
    public static String toString(Vector4f v) {
        return v.toString(new DecimalFormat("####0.##  "));
    }
    public static String toString(Matrix4f m) {
        return m.toString(new DecimalFormat("####0.##  "));
    }

    public static float toRadians(float degrees) {
        return degrees * PI_OVER_180;
    }
    public static float toDegrees(float radians) {
        return radians * _180_OVER_PI;
    }

    /**
     * Run some checked exception code and wrap any exceptions in RuntimeException.
     */
    public static <T> T exceptionContext(Lambda.R_Throws<T> s) {
        try{
            return s.call();
        }catch(Throwable t) {
            throw new RuntimeException(t);
        }
    }
    public static void exceptionContext(Lambda.V_Throws s) {
        try{
            s.call();
        }catch(Throwable t) {
            throw new RuntimeException(t);
        }


    }

    public static ByteBuffer toBuffer(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.allocateDirect(bytes.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(bytes, 0, bytes.length);
        bb.flip();
        return bb;
    }

    public static String toString(ByteBuffer buf) {
        StringBuilder b = new StringBuilder();
        for(int i=0; i<buf.limit(); i++) {
            b.append((char)buf.get(i));
        }
        return b.toString();
    }
    public static void putFloats(ByteBuffer buf, float f1) {
        buf.putFloat(f1);
    }
    public static void putFloats(ByteBuffer buf, Int2 p) {
        buf.putFloat(p.getX());
        buf.putFloat(p.getY());
    }
    public static void putFloats(ByteBuffer buf, RGBA f) {
        buf.putFloat(f.r);
        buf.putFloat(f.g);
        buf.putFloat(f.b);
        buf.putFloat(f.a);
    }
}
