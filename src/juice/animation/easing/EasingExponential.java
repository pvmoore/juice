package juice.animation.easing;

import java.util.Arrays;
import java.util.stream.IntStream;

public final class EasingExponential implements IEasing {
    private EasingType type;
    private double[] start, end;
    private int steps;
    private double[] distance;

    @Override
    public IEasing set(EasingType type, double[] start, double[] end, int steps) {
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
                    values[i] += step * distance[i]/steps;
                }
                break;
            case EASE_IN:
                for(int i=0; i<values.length; i++) {
                    values[i] += distance[i] * Math.pow(2, 10 * (step/steps - 1));
                }
                break;
            case EASE_OUT:
                for(int i=0; i<values.length; i++) {
                    values[i] += distance[i] * (-Math.pow(2, -10 * step/steps) + 1);
                }
                break;
            case EASE_IN_OUT:
                if((step/=steps/2) < 1) {
                    for(int i=0; i<values.length; i++) {
                        values[i] += (distance[i] / 2) * Math.pow(2, 10 * (step - 1));
                    }
                } else {
                    --step;
                    for(int i=0; i<values.length; i++) {
                        values[i] += (distance[i] / 2) * (-Math.pow(2, -10 * step) + 2);
                    }
                }
                break;
        }
        return values;
    }
}
