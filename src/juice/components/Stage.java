package juice.components;

import juice.Camera2D;
import juice.Frame;
import juice.Window;
import juice.animation.Animations;

import java.util.ArrayList;
import java.util.List;

final public class Stage extends UIComponent {
    public interface Hook {
        void call();
    }
    //===================================================================
    private Window window;
    private Camera2D camera;
    private Animations animations = new Animations();
    private List<Hook> afterUpdateHooks = new ArrayList<>();
    //===================================================================
    public Stage(Window window) {
        this.window = window;
        this.camera = new Camera2D(window.getWindowSize());
    }
    @Override public void destroy() {
        for(var c : getChildren()) {
            c.fireDestroy();
        }
    }

    public Window getWindow() { return window; }
    public Camera2D getCamera() { return camera; }
    public Animations getAnimations() { return animations; }

    @Override public void update(Frame frame) {

        animations.update(frame.delta);

        // Update children in reverse order
        var children = getChildren();
        for(int i = children.size()-1; i>=0; i--) {
            children.get(i).fireUpdate(frame);
        }
//        for(var c : getChildren()) {
//            c.fireUpdate(frame);
//        }

        if(!afterUpdateHooks.isEmpty()) {
            afterUpdateHooks.forEach(Hook::call);
            afterUpdateHooks.clear();
        }
    }
    @Override public void render(Frame frame) {
        for(var c : getChildren()) {
            c.fireRender(frame);
        }
    }

    public void addAfterUpdateHook(Hook h) {
        afterUpdateHooks.add(h);
    }
}
