package juice.animation.easing;

public interface IEasing {
    IEasing set(EasingType type, double[] start, double[] end, int steps);
    double[] get(double step);
}
