package juice.components;

import juice.Frame;
import juice.animation.Animations;

import java.util.ArrayList;
import java.util.List;

final public class Stage extends UIComponent {
    public interface Hook {
        void call();
    }
    //===================================================================
    private Animations animations = new Animations();
    private List<Hook> afterUpdateHooks = new ArrayList<>();
    //===================================================================
    public Animations getAnimations() { return animations; }

    @Override public void update(Frame frame) {
        animations.update(frame.delta);

        super.update(frame);

        if(!afterUpdateHooks.isEmpty()) {
            afterUpdateHooks.forEach(Hook::call);
            afterUpdateHooks.clear();
        }
    }
    public void addAfterUpdateHook(Hook h) {
        afterUpdateHooks.add(h);
    }
}
