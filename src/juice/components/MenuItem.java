package juice.components;

import juice.Frame;
import juice.Lambda;
import juice.Mouse;
import juice.renderers.TextRenderer;
import juice.types.RGBA;

final public class MenuItem extends UIComponent {
    private String label;
    private int index;
    private TextRenderer text;
    private Lambda.V callback;
    private boolean enabled = true;

    public String getLabel() { return label; }
    public Menu getMenu() { return (Menu)getParent(); }
    public MenuBar getBar() { return getMenu().getBar(); }
    public int getIndex() { return index; }

    public MenuItem(String label, Lambda.V callback) {
        this.label = label;
        this.callback = callback;
    }
    public MenuItem setEnabled(boolean flag) {
        this.enabled = flag;
        return this;
    }
    @Override public void onAdded() {
        this.index = getMenu().getIndex(this);
        this.text  = getMenu().getText();

        if(enabled) {
            text.setColour(RGBA.BLACK);
        } else {
            text.setColour(RGBA.WHITE.gamma(0.75f));
        }
        text.appendText(label, getAbsPos().add(10,0));
        text.setColour(RGBA.BLACK);
    }
    @Override public void onRemoved() {
        text.removeText(1);
    }
    @Override public void update(Frame frame) {
        if(!enabled) return;

        if(enclosesPoint(frame.window.getMousePos())) {
            getMenu().highlightItem(this);

            var events = frame.getLocalMouseEvents(this);

            for(var e : events) {
                if(e.type==Mouse.EventType.BUTTON_PRESS) {
                    getMenu().getText().replaceColour(index+1, RGBA.WHITE.gamma(0.6f));
                    callback.call();
                } else if(e.type==Mouse.EventType.BUTTON_RELEASE) {
                    getMenu().closeMenu();
                }
            }
        }
    }
}
