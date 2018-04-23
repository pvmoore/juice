package juice.components;

import juice.Frame;
import juice.Lambda;
import juice.types.Int2;

import java.util.ArrayList;
import java.util.List;

public class UIComponent {
    private Int2 pos  = Int2.ZERO;
    private Int2 size = Int2.ZERO;
    private List<UIComponent> children = new ArrayList<>();
    private UIComponent parent;
    //====================================================================
    /** Returns the sum of all relative positions. */
    public Int2 getAbsPos() {
        if(parent==null) return pos;
        return pos.add(parent.getAbsPos());
    }
    public Int2 getRelPos() {
        return pos;
    }
    public void setRelPos(Int2 p) {
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
    public UIComponent getParent() {
        return parent;
    }
    public List<UIComponent> getChildren() {
        return children;
    }
    public int countChildren(Lambda.AR<UIComponent,Boolean> f) {
        return (int)children.stream().filter(f::call).count();
    }
    public boolean enclosesPoint(Int2 p) {
        var pos    = getAbsPos();
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
        if(child.parent!=null) {
            if(child.parent==this) {
                // Child is already a child of this parent
                return;
            }
            // Detach first
            child.detach();
        }
        child.parent = this;
        children.add(child);

        // Call events
        child.onAdded();
        onChildAdded(child);
    }
    public void remove(UIComponent child) {
        child.parent = null;
        if(children.remove(child)) {
            // Call events of child was actually removed
            child.onRemoved();
            onChildRemoved(child);
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
    //====================================================================
    // Child events
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
    // Parent events
    //====================================================================
    public void onChildAdded(UIComponent child) {

    }
    public void onChildRemoved(UIComponent child) {

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
