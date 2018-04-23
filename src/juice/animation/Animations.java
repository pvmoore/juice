package juice.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

final public class Animations {
    private static int ids = 0;
    private Map<String,Animation> animations = new HashMap<>();
    //=========================================================================
    public Animations add(String id, Animation a, boolean start) {
        animations.put(id, a);
        if(start) a.start();
        return this;
    }
    public Animations add(Animation a, boolean start) {
        animations.put("__"+ids++, a);
        if(start) a.start();
        return this;
    }
    public void remove(String id) {
        animations.remove(id);
    }
    public void removeAll() {
        animations.clear();
    }
    public void pauseAll() {
        animations.values().forEach(Animation::pause);
    }
    public void resumeAll() {
        animations.values().forEach(Animation::resume);
    }
    public void update(double speedDelta) {
        var removeList = new ArrayList<String>();

        animations.forEach((id, animation) -> {
            if(animation.update(speedDelta)) removeList.add(id);
        });

        removeList.forEach(it->
            animations.remove(it)
        );
    }
}
