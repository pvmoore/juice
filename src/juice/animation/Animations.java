package juice.animation;

import java.util.ArrayList;
import java.util.List;

final public class Animations {
    private List<Animation> animations = new ArrayList<>();
    //=========================================================================
    public Animations add(Animation a, boolean start) {
        animations.add(a);
        if(start) a.start();
        return this;
    }
    public void remove(Animation a) {
        animations.remove(a);
    }
    public void removeAll() {
        animations.clear();
    }
    public void pauseAll() {
        animations.forEach(Animation::pause);
    }
    public void resumeAll() {
        animations.forEach(Animation::resume);
    }
    public void update(double speedDelta) {
        animations.removeIf(animation -> animation.update(speedDelta));
    }
}
