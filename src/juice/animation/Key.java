package juice.animation;


import juice.animation.easing.EasingType;

final public class Key {
    public interface FrameCallback {
        void call(int frame, double[] values);
    }
    public interface EndCallback {
        void call();
    }
    //=========================================================================
    int frame;
    double[] values;
    EasingType easingType = EasingType.LINEAR;
    FrameCallback frameCallback;
    EndCallback endCallback;

    public Key frame(int f) {
        this.frame = f;
        return this;
    }
    public Key values(double[] v) {
        this.values = v;
        return this;
    }
    public Key eachFrame(FrameCallback c) {
        this.frameCallback = c;
        return this;
    }
    public Key atEnd(EndCallback c) {
        endCallback = c;
        return this;
    }
    public Key easing(EasingType e) {
        this.easingType = e;
        return this;
    }
}
