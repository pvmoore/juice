package juice.animation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final public class Animations {
    private static int ids = 0;
    private Map<String,Animation> animations = new HashMap<>();
    private Map<String, Animation> toBeAdded = new HashMap<>();
    private Set<String> toBeRemoved = new HashSet<>();
    //=========================================================================
    public Animations add(String id, Animation a, boolean start) {
        toBeAdded.put(id, a);
        if(start) a.start();
        return this;
    }
    public Animations add(Animation a, boolean start) {
        toBeAdded.put("__"+ids++, a);
        if(start) a.start();
        return this;
    }
    public void remove(String id) {
        toBeRemoved.add(id);
    }
    public void removeAll() {
        toBeRemoved.addAll(animations.keySet());
    }
    public void pauseAll() {
        animations.values().forEach(Animation::pause);
    }
    public void resumeAll() {
        animations.values().forEach(Animation::resume);
    }
    public void update(double speedDelta) {
        // Remove any that have been removed
        toBeRemoved.forEach(it->
            animations.remove(it)
        );
        toBeRemoved.clear();

        // Add pending animations now
        animations.putAll(toBeAdded);
        toBeAdded.clear();

        // Update animations
        animations.forEach((id, animation) -> {
            if(animation.update(speedDelta)) toBeRemoved.add(id);
        });

        // Remove any that have finished
        toBeRemoved.forEach(it->
            animations.remove(it)
        );
        toBeRemoved.clear();
    }
}
