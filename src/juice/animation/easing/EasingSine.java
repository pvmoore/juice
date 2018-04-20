package juice.animation.easing;

import java.util.Arrays;
import java.util.stream.IntStream;

public final class EasingSine implements IEasing {
    private static final double HALF_PI = Math.PI/2;
    private EasingType type;
    private double[] start, end;
    private int steps;
    private double[] distance;

    @Override
    public IEasing set(EasingType type, double start[], double end[], int steps) {
        this.type     = type;
        this.start    = start;
        this.end      = end;
        this.steps    = steps;
        this.distance = IntStream.range(0, start.length)
                                 .mapToDouble(i->end[i]-start[i])
                                 .toArray();
        return this;
    }
    @Override
    public double[] get(double step) {
        if(step==0) return start;
        if(step>=steps) return end;

        double[] values = Arrays.copyOf(start, start.length);

        switch(type) {
            case LINEAR:
                for(int i=0; i<values.length; i++) {
                    values[i] += (step * (distance[i]/steps));
                }
                break;
            case EASE_IN:
                for(int i=0; i<values.length; i++) {
                    values[i] += -distance[i] * Math.cos(step/steps * HALF_PI) + distance[i];
                }
                break;
            case EASE_OUT:
                for(int i=0; i<values.length; i++) {
                    values[i] += distance[i] * Math.sin(step/steps * HALF_PI);
                }
                break;
            case EASE_IN_OUT:
                for(int i=0; i<values.length; i++) {
                    values[i] += -distance[i]/2 * (Math.cos(Math.PI*step/steps) - 1);
                }
                break;
        }
        return values;
    }
}