package juice.components;

import juice.Frame;
import juice.types.Int2;

import java.util.ArrayList;
import java.util.List;

public class UIComponent {
    protected Int2 pos  = Int2.ZERO;
    protected Int2 size = Int2.ZERO;
    protected List<UIComponent> children = new ArrayList<>();
    protected UIComponent parent;
    //====================================================================
    /** Returns the sum of all relative positions. */
    public Int2 getPos() {
        if(parent==null) return pos;
        return pos.add(parent.getPos());
    }
    public void setPos(Int2 p) {
        boolean changed = p!=pos;
        pos = p;
        if(changed) onMoved();
    }
    public Int2 getSize() {
        return size;
    }
    public void setSize(Int2 s) {
        boolean changed = s!=size;
        size = s;
        if(changed) onResized();
    }
    public boolean enclosesPoint(Int2 p) {
        var pos    = getPos();
        var extent = pos.add(getSize());
        return p.getX() >= pos.getX()   && p.getY() >= pos.getY() &&
               p.getX() < extent.getX() && p.getY() < extent.getY();

    }
    public Stage getStage() {
        if(this instanceof Stage) return (Stage)this;
        if(parent==null) System.out.println("Wowza!! "+this.getClass().getSimpleName());
        return parent.getStage();
    }
    //====================================================================
    public void add(UIComponent child) {
        child.parent = this;
        children.add(child);
        child.onAdded();
    }
    public void remove(UIComponent child) {
        child.parent = null;
        if(children.remove(child)) {
            child.onRemoved();
        }
    }
    public boolean isAttached() {
        return parent!=null;
    }
    public void detach() {
        if(parent==null) return;
        parent.remove(this);
    }
    public void destroy() {
        for(var c : children) {
            c.destroy();
        }
    }
    public void update(Frame frame) {
        for(var c : children) {
            c.update(frame);
        }
    }
    public void render(Frame frame) {
        for(var c : children) {
            c.render(frame);
        }
    }
//    public void mouseButton(int button, Window.MouseState state) {
//        for(var c : children) {
//            c.mouseButton(button, state);
//        }
//    }
//    public void mouseMoved(Window.MouseState state) {
//        for(var c : children) {
//            c.mouseMoved(state);
//        }
//    }
//    public void mouseDragEnd(Window.MouseDrag drag) {
//        for(var c : children) {
//            c.mouseDragEnd(drag);
//        }
//    }
    //====================================================================
    public void onAdded() {
        // override this if you need to do things after you are added
    }
    public void onRemoved() {
        // override if you need to do things after you are removed
    }
    public void onMoved() {
        // override if you are interested in move events
    }
    public void onResized() {
        // override if you are interested in size events
    }
    //====================================================================
    @Override public String toString() {
        var s =  String.format("%s[%s : %s %d children] parent:%s",
                             getClass().getSimpleName(),
                             pos, size, children.size(),
                             parent==null ? "null" :
                             parent.getClass().getSimpleName());
        for(var c : children) {
            s += "\n\t" + c.toString();
        }
        return s;
    }
}
